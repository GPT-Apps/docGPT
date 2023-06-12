/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.exception.CommandException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author masaimu
 * @version 2023-04-15 22:04:00
 */
public class OptionService {
  static CommandLineParser parser = new DefaultParser();
  static Options options = new Options();

  // options.addOption(Option.builder("f")
  // .longOpt("file")
  // .hasArg(true)
  // .argName("failPath")
  // .required(false)
  // .desc("文件的路径")
  // .optionalArg(false)
  // .numberOfArgs(3)
  // .valueSeparator(',')
  // .type(String.class)
  // .build()
  // );

  static {
    options.addOption("t", "target", true, "target:openai(default)");
    options.addOption("f", "file", true, "java file.");
    options.addOption("d", "directory", true, "java file directory.");
    options.addOption("g", "generate", false, "generate document.");
    options.addOption("m", "method", true, "simple method name.");
  }

  public static File parseFolder(String[] args) {
    try {

      String directory = StringUtils.EMPTY;
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("d")) {
        directory = line.getOptionValue("d");
      }

      return new File(directory);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static String parseFileName(String[] args) {
    try {
      String javaClassName = StringUtils.EMPTY;
      CommandLine line = parser.parse(options, args);
      if (line.hasOption("f")) {
        javaClassName = line.getOptionValue("f");
      }
      return javaClassName;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean callTarget(String[] args) {
    try {
      CommandLine line = parser.parse(options, args);
      return line.hasOption("g");
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static String parseMethodName(String[] args) {
    try {
      String method = StringUtils.EMPTY;
      CommandLine line = parser.parse(options, args);
      if (line.hasOption("m")) {
        method = line.getOptionValue("m");
      }
      return method;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public CmdHandler parseAndExecute(String[] args) {
    try {
      String cmd = args[0];
      CmdHandler cmdHandler = CommandFactory.getCmdHandler(cmd);
      if (cmdHandler != null) {
        cmdHandler.parseOption(args);
        CommandFactory.execute(cmdHandler);
      } else {
        throw new CommandException(
            "command not found: " + cmd + ", you can find all available command by 'help'");
      }
      return cmdHandler;
    } catch (CancelTask e) {
      // ignore
      return null;
    }
  }
}
