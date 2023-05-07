/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd.signal;

/**
 * @author masaimu
 * @version 2023-05-07 20:31:00
 */
public class WarnSignal extends CmdSignal {
  public String message;

  public WarnSignal(String message) {
    this.message = message;
  }
}
