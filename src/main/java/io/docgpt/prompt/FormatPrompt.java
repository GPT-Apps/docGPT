/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-04-27 22:19:00
 */
public class FormatPrompt {
  public static String getInterfaceFormatPrompt(Map<String, ClassPrompt> cache,
      MethodPrompt methodPrompt) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "The document consists of four parts, the format and content of each part are shown as follows.\n") //
        .append("\"\"\"") //
        .append("## Description \n") //
        .append(
            "Description contains summarize the function and purpose of the method, and list the key actions in the method.\n") //
        .append("## Request \n") //
        .append(methodPrompt.getRequestFormat(cache)) //
        .append("## Response \n") //
        .append(methodPrompt.getResponseFormat(cache)) //
        .append("## Example \n") //
    ;
    prompt.append("Example consists of two parts: \n")//
        .append(
            "Part 1 demonstrates the interface invocation using the curl command in the Example.\n") //
        .append("Part 2 provides sample Java code for invoking the method.\n") //
        .append("\"\"\"\n");
    return prompt.toString();
  }

  public static String getCodePrompt(MethodPrompt methodPrompt) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("The code is as follows: \n").append("\"\"\" \n");

    prompt.append(getCode(methodPrompt)).append("\n \"\"\" \n");
    return prompt.toString();
  }

  protected static String getCode(MethodPrompt methodPrompt) {
    StringBuilder prompt = new StringBuilder();
    List<String> annotations = methodPrompt.annotations;
    if (!CollectionUtils.isEmpty(annotations)) {
      for (int i = 0; i < annotations.size(); i++) {
        String annotation = annotations.get(i);
        prompt.append(annotation).append("\n");
      }
    }

    prompt.append(methodPrompt.declaration).append(methodPrompt.code);
    return prompt.toString();
  }

  public static String getUmlActivityKeyInfoPrompt() {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "I want to generate several UML Activity Diagram based on the code below. Can you extract the key information for me to help me generate PlantUML code? The result should not include PlantUML code, we don't need it for now.\n");
    return prompt.toString();
  }

  public static String getUmlActivityFormat() {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "The content consists of 5 parts, the format and content of each part are shown as follows.\n") //
        .append("## class name \n") //
        .append("Class name. \n") //
        .append("## method name \n").append("Method name. \n") //
        .append("## Argument list \n") //
        .append(
            "The argument type and argument name list that the method receives, separated by commas, like: '''Object obj1, Map map2'''. \n")
        .append("## Dependency list \n") //
        .append(
            "The dependency type list that the method uses, separated by commas, like '''Object, Map, List'''. \n") //
        .append("## Actions \n") //
        .append(
            "Provide a concise summary of the main steps of method execution, without going into too much detail. \n");
    return prompt.toString();
  }

  public static String getUmlActivityPrompt(String keyInfo) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "Generate PlantUML code for an activity diagram based on the following information: class name, method name, argument list, dependency list, and actions. \n");
    prompt.append(keyInfo).append("\n");
    return prompt.toString();
  }

}
