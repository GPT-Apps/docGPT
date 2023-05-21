/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt;

import io.docgpt.cmd.CommandFactory;
import io.docgpt.cmd.ConfigHandler;
import io.docgpt.cmd.GenHandler;
import io.docgpt.cmd.HelpHandler;
import io.docgpt.cmd.ListHandler;
import io.docgpt.cmd.LoadHandler;
import io.docgpt.cmd.TerminalService;

/**
 * @author masaimu
 * @version 0.1
 */
public class Main {
  public static void main(String[] args) {
    try {
      CommandFactory.registry(HelpHandler.HELP, new HelpHandler());
      CommandFactory.registry(LoadHandler.LOAD, new LoadHandler());
      CommandFactory.registry(ListHandler.LIST, new ListHandler());
      CommandFactory.registry(GenHandler.GENERATE, new GenHandler());
      CommandFactory.registry(ConfigHandler.CONFIG, new ConfigHandler());
      TerminalService.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
