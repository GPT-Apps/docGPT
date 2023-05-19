/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.cmd.signal.CmdSignal;
import io.docgpt.cmd.signal.InfoSignal;
import io.docgpt.cmd.signal.ProgressSignal;
import io.docgpt.cmd.signal.StopSignal;
import io.docgpt.cmd.signal.WarnSignal;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author masaimu
 * @version 2023-05-06 21:43:00
 */
public abstract class CmdHandler implements Runnable {
  static CommandLineParser parser = new DefaultParser();

  LinkedBlockingQueue<CmdSignal> queue = new LinkedBlockingQueue<CmdSignal>();

  public abstract void parseOption(String[] args) throws ParseException;

  public CmdSignal getSignal() throws InterruptedException {
    return queue.take();
  }

  public boolean setWarnSignal(String warnSignal) {
    return queue.offer(new WarnSignal(warnSignal));
  }

  public boolean setInfoSignal(String infoSignal) {
    return queue.offer(new InfoSignal(infoSignal));
  }

  public boolean setStopSignal(String reason) {
    return queue.offer(new StopSignal(reason));
  }

  public boolean setProgressSignal(int maxProgress, int currentProgress) {
    return queue.offer(new ProgressSignal(maxProgress, currentProgress));
  }

  protected static String longOpt(String longOption) {
    if (StringUtils.isNotEmpty(longOption) && longOption.startsWith("--")) {
      return longOption.substring(2);
    }
    return longOption;
  }

  protected static String shortOpt(String shortOption) {
    if (StringUtils.isNotEmpty(shortOption) && shortOption.startsWith("-")) {
      return shortOption.substring(1);
    }
    return shortOption;
  }
}
