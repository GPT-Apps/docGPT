/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;
import org.jline.reader.impl.completer.NullCompleter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author masaimu
 * @version 2023-05-21 14:44:00
 */
public class ConfigHandler extends CmdHandler {

  static Options options = new Options();

  CommandLine commandLine;

  public static final String CONFIG = "conf";
  public static final String OPENAI_API_KEY = "OPENAI_API_KEY";

  private static final List<Completers.OptDesc> optDescList = new ArrayList<>();

  private static final Map<String, String /* arg example */> expMap = new HashMap<>();

  static {
    Completers.OptDesc t =
        new Completers.OptDesc("-t", "--token", "Specify OpenAI api key", NullCompleter.INSTANCE);
    expMap.put(t.shortOption(), "<OPENAI_API_KEY>");
    optDescList.add(t);
    options.addOption(shortOpt(t.shortOption()), longOpt(t.longOption()), true, t.description());

    Completers.OptDesc v =
        new Completers.OptDesc("-v", "--view", "Display DocGPT config", NullCompleter.INSTANCE);
    optDescList.add(v);
    options.addOption(shortOpt(v.shortOption()), longOpt(v.longOption()), false, v.description());
  }

  @Override
  public void parseOption(String[] args) {
    try {
      this.commandLine = parser.parse(options, args);
    } catch (ParseException e) {
      setErrorSignal(e.getMessage());
      setStopSignal();
    }
  }

  @Override
  public String getCmd() {
    return CONFIG;
  }

  @Override
  public Map<String, String> getExpMap() {
    return expMap;
  }

  @Override
  public List<Completers.OptDesc> getOptDescList() {
    return optDescList;
  }

  @Override
  public void run() {
    handler();
  }

  private void handler() {
    try {
      Properties properties = getLocalProp();
      if (commandLine == null) {
        return;
      }
      if (commandLine.hasOption("t")) {
        String token = commandLine.getOptionValue("t");
        if (StringUtils.isNotBlank(token)) {
          properties.setProperty("OPENAI_API_KEY", token);
          storeProp(properties);
        }
      } else {
        StringBuilder msg = new StringBuilder();
        for (String name : properties.stringPropertyNames()) {
          msg.append(name).append("=").append(properties.getProperty(name)).append("\n");
        }
        setInfoSignal(msg.toString());
      }
    } finally {
      setStopSignal();
    }
  }

  public void storeProp(Properties properties) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(path());
      properties.store(fos, new Date().toString());
    } catch (Exception e) {
      setErrorSignal(e.getMessage());
    } finally {
      if (fos != null) {
        try {
          fos.flush();
          fos.close();
        } catch (IOException e) {
          setErrorSignal(e.getMessage());
        }
      }
    }
  }

  private String path() {
    String usrHome = System.getProperty("user.home");
    return usrHome + "/.config/docgpt/config.properties";
  }

  public Properties getLocalProp() {
    Properties props = new Properties();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(path());
      props.load(fis);
    } catch (IOException e) {
      setErrorSignal(e.getMessage());
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          setErrorSignal(e.getMessage());
        }
      }
    }
    return props;
  }

  public String checkConfig() {
    String token = System.getenv(OPENAI_API_KEY);
    if (StringUtils.isBlank(token)) {
      Properties props = getLocalProp();
      token = props.getProperty(OPENAI_API_KEY);
    }
    if (StringUtils.isBlank(token)) {
      return "OpenAI api key has not been set yet, you can use 'export OPENAI_API_KEY=sk-xxxxxxx' in shell or store your API key by 'config -t=sk-xxxxxxx' in docgpt";
    }
    return "OpenAI api key has been set";
  }

}
