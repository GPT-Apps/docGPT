/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * @author masaimu
 * @version 2023-05-31 13:35:00
 */
public class MethodSummaryContext {

  public String className;
  public String methodName;
  public List<String> argumentList;
  public List<String> dependencyList;
  public List<String> actions;
  public String description;
  public String declaration;

  public static String getFormatPrompt() {
    MethodSummaryContext context = new MethodSummaryContext();
    context.className = "Class name.";
    context.methodName = "Method name.";
    context.argumentList = Arrays.asList(
        "The argument type and argument name list that the method receives, separated by commas, like: '''Object obj1, Map map2'''.");
    context.dependencyList = Arrays.asList(
        "The dependency type list that the method uses, separated by commas, like '''Object, Map, List'''.");
    context.actions = Arrays.asList(
        "Provide a concise summary of the main steps of method execution, without going into too much detail.");
    context.description = "Give a brief overview of the purpose and function of this code snippet.";

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    return gson.toJson(context);
  }
}
