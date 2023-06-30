/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd.signal;

/**
 * @author masaimu
 * @version 2023-05-06 23:45:00
 */
public class StreamInfoSignal extends CmdSignal {

  public String message;

  public StreamInfoSignal(String message) {
    this.message = message;
  }
}
