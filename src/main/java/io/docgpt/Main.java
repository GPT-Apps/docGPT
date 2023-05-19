/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt;

import io.docgpt.cmd.CommandFactory;
import io.docgpt.cmd.GenHandler;
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
      CommandFactory.registry(LoadHandler.LOAD, new LoadHandler());
      CommandFactory.registry(ListHandler.LIST, new ListHandler());
      CommandFactory.registry(GenHandler.GENERATE, new GenHandler());
      TerminalService.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
