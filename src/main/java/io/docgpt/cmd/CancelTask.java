/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

/**
 * @author masaimu
 * @version 2023-05-20 17:44:00
 */
public class CancelTask extends RuntimeException {
  public CancelTask() {}

  public CancelTask(String message) {
    super(message);
  }
}
