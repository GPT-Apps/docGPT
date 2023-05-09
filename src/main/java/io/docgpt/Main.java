/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt;

import io.docgpt.cmd.CommandFactory;
import io.docgpt.cmd.GenHandler;
import io.docgpt.cmd.ListHandler;
import io.docgpt.cmd.LoadHandler;
import io.docgpt.cmd.TerminalService;

import java.util.Arrays;

/**
 * @author masaimu
 * @version 0.1
 */
public class Main {
  public static void main(String[] args) {
    System.out.println(Arrays.asList(args));

    try {
      CommandFactory.registry("load", new LoadHandler());
      CommandFactory.registry("ls", new ListHandler());
      CommandFactory.registry("gen", new GenHandler());
      TerminalService.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
