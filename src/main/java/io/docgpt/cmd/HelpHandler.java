/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import org.jline.builtins.Completers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-05-20 14:44:00
 */
public class HelpHandler extends CmdHandler {

  public static final String HELP = "help";

  @Override
  public void parseOption(String[] args) {

  }

  @Override
  public String getCmd() {
    return HELP;
  }

  @Override
  public Map<String, String> getExpMap() {
    return Collections.emptyMap();
  }

  @Override
  public List<Completers.OptDesc> getOptDescList() {
    return Collections.emptyList();
  }

  @Override
  public void run() {
    handler();
  }

  private void handler() {
    StringBuilder info = new StringBuilder();
    for (CmdHandler cmdHandler : CommandFactory.getCmdHandlers()) {
      info.append(cmdHandler.getCmdHelp());
    }
    setInfoSignal(info.toString());
    setStopSignal();
  }
}
