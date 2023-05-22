/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;
import org.jline.builtins.Completers.OptionCompleter;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author masaimu
 * @version 2023-05-18 22:28:00
 */
public class OptionsCompleters implements Completer {

  private AggregateCompleter aggregateCompleter;

  public OptionsCompleters() {
    init();
  }

  private void init() {
    OptionCompleter loadOption = new OptionCompleter(
        Arrays.asList(new ArgumentStringsCompleter("-d=")), this::commandOptions, 1);
    ArgumentCompleter load = new ArgumentCompleter(new StringsCompleter(LoadHandler.LOAD),
        loadOption, NullCompleter.INSTANCE);

    OptionCompleter lsOption = new OptionCompleter(
        Arrays.asList(new ArgumentStringsCompleter("-c=", "-a")), this::commandOptions, 1);
    ArgumentCompleter ls = new ArgumentCompleter(new StringsCompleter(ListHandler.LIST), lsOption,
        NullCompleter.INSTANCE);

    OptionCompleter genOption = new OptionCompleter(
        Arrays.asList(new ArgumentStringsCompleter("-m=")), this::commandOptions, 1);
    ArgumentCompleter gen = new ArgumentCompleter(new StringsCompleter(GenHandler.GENERATE),
        genOption, NullCompleter.INSTANCE);

    ArgumentCompleter help =
        new ArgumentCompleter(new StringsCompleter(HelpHandler.HELP), NullCompleter.INSTANCE);

    OptionCompleter configOption = new OptionCompleter(
        Arrays.asList(new ArgumentStringsCompleter("-t=", "-v")), this::commandOptions, 1);
    ArgumentCompleter config = new ArgumentCompleter(new StringsCompleter(ConfigHandler.CONFIG),
        configOption, NullCompleter.INSTANCE);

    aggregateCompleter = new AggregateCompleter(load, ls, gen, config, help);
  }

  public List<Completers.OptDesc> commandOptions(String command) {
    if (StringUtils.isEmpty(command)) {
      return Collections.emptyList();
    }
    CmdHandler cmdHandler = CommandFactory.getCmdHandler(command);
    if (cmdHandler != null) {
      return cmdHandler.getOptDescList();
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void complete(LineReader reader, final ParsedLine commandLine,
      List<Candidate> candidates) {
    aggregateCompleter.complete(reader, commandLine, candidates);
  }
}
