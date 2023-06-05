/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-05-31 14:09:00
 */
public class ClassSummaryContext {
  public String className;
  public List<String> argumentList;

  public Map<String /* methodFullName */, MethodSummaryContext> methodSummaryContexts =
      new HashMap<>();
  public String description;
  public String declaration;

  public static String getFormatPrompt() {
    ClassSummaryContext context = new ClassSummaryContext();
    context.className = "Class name.";
    context.argumentList = Arrays.asList(
        "The field type and field name list that the class own, separated by commas, like: '''Object obj1, Map map2'''.");
    context.description = "Give a brief overview of the purpose and function of this code snippet.";
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    return gson.toJson(context);
  }
}
