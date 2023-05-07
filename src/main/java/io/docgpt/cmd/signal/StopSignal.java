/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd.signal;

/**
 * @author masaimu
 * @version 2023-05-07 00:43:00
 */
public class StopSignal extends CmdSignal {
  public String reason;

  public StopSignal(String reason) {
    this.reason = reason;
  }

  public StopSignal() {}
}
