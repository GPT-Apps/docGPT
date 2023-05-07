/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd.signal;

/**
 * @author masaimu
 * @version 2023-05-06 23:44:00
 */
public class ProgressSignal extends CmdSignal {
  public int maxProgress;
  public int currentProgress;

  public ProgressSignal(int maxProgress, int currentProgress) {
    this.maxProgress = maxProgress;
    this.currentProgress = currentProgress;
  }
}
