/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

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
  public String declaration;
  public String simpleName;
  public List<String> annotations = new ArrayList<>();

  public String code;

  public String comment;

  public Map<String /* simpleName */, String /* fullName */> parameters = new HashMap<>();

  public String getPromptStr(Map<String, ClassPrompt> cache) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "Based on the rest interface code I gave you, write an interface document with an example of calling the interface with the curl command. ");
    prompt.append("The method declaration is `").append(declaration).append("`. ");
    if (!CollectionUtils.isEmpty(annotations)) {
      prompt.append("The method has the following annotations: ");
      for (int i = 0; i < annotations.size() - 1; i++) {
        String annotation = annotations.get(i);
        prompt.append(annotation).append(",");
      }
      prompt.append(annotations.get(annotations.size() - 1)).append(". ");
    }
    prompt.append("The code is as follows: ").append(code).append("\n");

    if (parameters.size() > 0) {
      Map<String /* simpleTypeName */, List<String>> fieldAnnotations = new HashMap<>();
      for (Map.Entry<String /* simpleName */, String /* fullName */> entry : parameters
          .entrySet()) {
        String simpleName = entry.getKey();
        String fullName = entry.getValue();
        ClassPrompt paraClassPrompt = cache.get(fullName);
        if (paraClassPrompt != null) {
          fieldAnnotations.put(simpleName, paraClassPrompt.fieldAnnotations);
        }
      }
      if (fieldAnnotations.size() > 0) {
        prompt.append(
            "The method parameter contains the following fields, which you can display in a list: ");
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
    }

    return prompt.toString();
  }

  public void addAnnotations(List<String> classAnnotationExprs) {
    annotations.addAll(classAnnotationExprs);
  }
}
