/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd.signal;

/**
 * @author masaimu
 * @version 2023-05-20 17:47:00
 */
public class ErrorSignal extends CmdSignal {
  public String message;

  public ErrorSignal(String message) {
    this.message = message;
  }
}
