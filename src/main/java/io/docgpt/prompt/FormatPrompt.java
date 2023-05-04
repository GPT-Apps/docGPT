/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

/**
 * @author masaimu
 * @version 2023-04-27 22:19:00
 */
public class FormatPrompt {
  public static String getRequestPrompt() {
    return "The Request chapter includes http method and url, the Description chapter includes description for the api, the Parameters chapter includes contains a table showing all parameters, parameter types, and parameter descriptions.";
  }
}
