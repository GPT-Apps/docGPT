/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd.signal;

/**
 * @author masaimu
 * @version 2023-05-21 01:39:00
 */
public class WaitSignal extends CmdSignal {

  public final String message;

  public final int currentProgress;

  public final int limit;

  public WaitSignal(String message, int currentProgress, int limit) {
    this.message = message;
    this.currentProgress = currentProgress;
    this.limit = limit;
  }
}
