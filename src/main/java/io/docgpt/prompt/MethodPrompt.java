/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-04-20 19:15:00
 */
@Data
public class MethodPrompt {

  private ClassPrompt classPrompt;
  private String accessSpecifier;
  public String declaration;
  public String simpleName;
  public List<String> annotations = new ArrayList<>();

  public String code;

  public String comment;

  public MethodSummaryContext summaryContext;

  public Map<String /* simpleName */, String /* fullName */> parameters = new HashMap<>();

  public Map<String /* simpleName */, String /* fullName */> responses = new HashMap<>();

  public String getRequestFormat(Map<String, ClassPrompt> cache) {
    StringBuilder prompt = new StringBuilder();
    if (parameters.size() == 0) {
      prompt.append(
          "The Request section does not need to contain any content, it can be left blank.\n");
    } else {
      prompt.append(
          "The Request section include a markdown-formatted table that lists the name, type, description, and example of each parameter field.\n");
      prompt.append(getField(parameters, cache, "Request")).append("\n");
    }
    return prompt.toString();
  }

  public String getResponseFormat(Map<String, ClassPrompt> cache) {
    StringBuilder prompt = new StringBuilder();
    if (responses.size() == 0) {
      prompt.append(
          "The Response section does not need to contain any content, it can be left blank.\n");
    } else {
      prompt.append(
          "The Response section include a markdown-formatted table that lists the name, type, description, and example of each response field.");
      prompt.append(getField(responses, cache, "Response"));
    }
    return prompt.toString();
  }

  public String getField(Map<String /* simpleName */, String /* fullName */> nameMap,
      Map<String, ClassPrompt> cache, String sourceName) {
    StringBuilder prompt = new StringBuilder();
    Map<String /* simpleTypeName */, List<String>> fieldAnnotations = new HashMap<>();
    for (Map.Entry<String /* simpleName */, String /* fullName */> entry : nameMap.entrySet()) {
      String simpleName = entry.getKey();
      String fullName = entry.getValue();
      ClassPrompt paraClassPrompt = cache.get(fullName);
      if (paraClassPrompt != null) {
        fieldAnnotations.put(simpleName, paraClassPrompt.fieldDeclarations);
        if (!paraClassPrompt.fields.isEmpty()) {
          for (Map.Entry<String /* simpleName */, String /* fullName */> fieldClassEntry : paraClassPrompt.fields
              .entrySet()) {
            String fieldSimpleName = fieldClassEntry.getKey();
            String fieldFullName = fieldClassEntry.getValue();
            ClassPrompt fieldClassPrompt = cache.get(fieldFullName);
            if (fieldClassPrompt != null) {
              fieldAnnotations.put(fieldSimpleName, fieldClassPrompt.fieldDeclarations);
            }
          }
        }
      }
    }
    if (fieldAnnotations.size() > 0) {
      prompt.append("The ").append(sourceName).append(" parameters are as follows: ");
      for (Map.Entry<String /* simpleTypeName */, List<String>> entry : fieldAnnotations
          .entrySet()) {
        String simpleTypeName = entry.getKey();
        prompt.append("public class ").append(simpleTypeName).append(" {");
        for (String fieldDeclaration : entry.getValue()) {
          prompt.append(fieldDeclaration).append("\n");
        }
        prompt.append("}\n");
      }
    }
    return prompt.toString();
  }

  public String docBasicPrompt() {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "Based on the rest interface code I gave you, write an markdown interface document. ");
    return prompt.toString();
  }

  public String getPromptStr(Map<String, ClassPrompt> cache) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(docBasicPrompt());
    prompt.append(FormatPrompt.getInterfaceFormatPrompt(cache, this));
    prompt.append(FormatPrompt.getCodePrompt(this));
    return prompt.toString();
  }

  public String getUmlPromptStr() {
    StringBuilder prompt = new StringBuilder();
    prompt.append(FormatPrompt.getUmlActivityKeyInfoPrompt());
    prompt.append("\"\"\" \n");
    prompt.append("public class ").append(classPrompt.getSimpleName()).append("\n");
    prompt.append(FormatPrompt.getCode(this)).append("\"\"\" \n");
    prompt.append(FormatPrompt.getMethodUmlActivityFormat());
    return prompt.toString();
  }

  public void addAnnotations(List<String> classAnnotationExprs) {
    annotations.addAll(classAnnotationExprs);
  }
}
