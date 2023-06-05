/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd.signal;

/**
 * @author masaimu
 * @version 2023-06-05 09:52:00
 */
public class SystemSignal extends CmdSignal {

  public String message;

  public SystemSignal(String message) {
    this.message = message;
  }
}
