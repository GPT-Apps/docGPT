/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.exception;

/**
 * @author masaimu
 * @version 2023-06-12 12:05:00
 */
public class CommandException extends RuntimeException {

  public CommandException(String message) {
    super(message);
  }

  public CommandException(String message, Throwable cause) {
    super(message, cause);
  }
}
