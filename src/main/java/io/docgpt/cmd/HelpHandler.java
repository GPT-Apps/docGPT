/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import org.jline.builtins.Completers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-05-20 14:44:00
 */
public class HelpHandler extends CmdHandler {

  public static final String HELP = "help";

  public static String banner() {
    StringBuilder banner = new StringBuilder();
    banner.append(readResource("banner.txt"));
    return banner.toString();
  }

  public static String welcome() {
    StringBuilder welcome = new StringBuilder();
    welcome.append(readResource("version.txt"));
    welcome.append(
        "DocGPT is a tool that uses OpenAI to generate documentation for code. You can first use the \"load\" command to load a Java project. You can use the \"help\" command to get assistance.\n");

    return welcome.toString();
  }

  private static String readResource(String file) {
    try {
      StringBuilder banner = new StringBuilder();
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(HelpHandler.class.getClassLoader().getResourceAsStream(file)));
      String str;
      while ((str = reader.readLine()) != null) {
        banner.append(str).append("\n");
      }
      reader.close();
      return banner.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

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
