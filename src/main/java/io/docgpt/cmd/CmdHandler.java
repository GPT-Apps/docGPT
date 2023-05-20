/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.cmd.signal.CmdSignal;
import io.docgpt.cmd.signal.ErrorSignal;
import io.docgpt.cmd.signal.InfoSignal;
import io.docgpt.cmd.signal.ProgressSignal;
import io.docgpt.cmd.signal.StopSignal;
import io.docgpt.cmd.signal.WarnSignal;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author masaimu
 * @version 2023-05-06 21:43:00
 */
public abstract class CmdHandler implements Runnable {
  static CommandLineParser parser = new DefaultParser();

  LinkedBlockingQueue<CmdSignal> queue = new LinkedBlockingQueue<CmdSignal>();

  public abstract void parseOption(String[] args);

  public abstract String getCmd();

  public abstract Map<String, String /* arg example */> getExpMap();

  public abstract List<Completers.OptDesc> getOptDescList();

  public CmdSignal getSignal() throws InterruptedException {
    return queue.take();
  }

  public boolean setWarnSignal(String warnSignal) {
    return queue.offer(new WarnSignal(warnSignal));
  }

  public boolean setErrorSignal(String errorSignal) {
    return queue.offer(new ErrorSignal(errorSignal));
  }

  public boolean setInfoSignal(String infoSignal) {
    return queue.offer(new InfoSignal(infoSignal));
  }

  public boolean setStopSignal() {
    return setStopSignal(" ");
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

  public String getCmdHelp() {
    int maxSpaceCount = 1;
    for (Completers.OptDesc optDesc : getOptDescList()) {
      int length = optDesc.longOption().length();
      String example = getExpMap().get(optDesc.shortOption());
      length += StringUtils.isBlank(example) ? 1 : example.length() + 1;
      maxSpaceCount = Math.max(maxSpaceCount, length);
    }
    StringBuilder info = new StringBuilder();
    info.append("Usage: ").append(getCmd()).append(" [options...]\n");
    for (Completers.OptDesc optDesc : getOptDescList()) {
      info.append(" ").append(optDesc.shortOption()).append(", ");
      StringBuilder longOpt = new StringBuilder();
      longOpt.append(optDesc.longOption());
      String example = getExpMap().get(optDesc.shortOption());
      if (StringUtils.isNotBlank(example)) {
        longOpt.append("=").append(example);
      }
      int spaceCount = maxSpaceCount - longOpt.length();
      for (int i = 0; i < spaceCount; i++) {
        longOpt.append(" ");
      }
      info.append(longOpt).append("\t").append(optDesc.description()).append("\n");
    }
    return info.toString();
  }
}
