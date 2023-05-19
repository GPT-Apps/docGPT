/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.parse.CodeContext;
import io.docgpt.prompt.ClassPrompt;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;
import org.jline.builtins.Completers.OptDesc;
import org.jline.reader.impl.completer.NullCompleter;

import java.util.ArrayList;
import java.util.List;

import static io.docgpt.cmd.TerminalService.terminal;

/**
 * @author masaimu
 * @version 2023-05-07 20:19:00
 */
public class ListHandler extends CmdHandler {

  static Options options = new Options();

  static ClassCompleter completer = new ClassCompleter();

  CommandLine commandLine;
  TableService tableService = new TableService(terminal());

  public static final String LIST = "ls";

  private static final List<OptDesc> optDescList = new ArrayList<>();

  static {
    OptDesc c = new OptDesc("-c", "--class", "Java class name", completer);
    optDescList.add(c);
    options.addOption(shortOpt(c.shortOption()), longOpt(c.longOption()), true, c.description());

    OptDesc a = new OptDesc("-a", "--all", "List all class", NullCompleter.INSTANCE);
    optDescList.add(a);
    options.addOption(shortOpt(a.shortOption()), longOpt(a.longOption()), false, a.description());
  }

  @Override
  public void parseOption(String[] args) throws ParseException {
    this.commandLine = parser.parse(options, args);
  }

  @Override
  public void run() {
    handler();
  }

  public void handler() {
    String stopMsg = " ";
    try {
      String clazz = StringUtils.EMPTY;

      if (commandLine.hasOption("c")) {
        clazz = commandLine.getOptionValue("c");
      }
      boolean all = commandLine.hasOption("a");
      CodeContext codeContext = CommandFactory.getCodeContext();
      if (codeContext == null) {
        setWarnSignal("Please load Java project first.");
        return;
      }
      if (all) {
        List<ClassPrompt> classPrompts = codeContext.getAllClassPrompt();
        String list = tableService.getClassListStr(classPrompts);
        setInfoSignal(list);
      } else if (StringUtils.isNotBlank(clazz)) {
        List<String> fullNameList = codeContext.nameCache.get(clazz);
        String fullName = fullNameList.get(0);
        ClassPrompt classPrompt = codeContext.cache.get(fullName);
        codeContext.activeClassPrompt = classPrompt;
        String table = tableService.getMethodTableStr(classPrompt);
        setInfoSignal(table);
        GenHandler.methodCompleter
            .setMethodVars(new ArrayList<>(classPrompt.getMethodNameCache().keySet()));
      } else {
        List<ClassPrompt> classPrompts = codeContext.getNonemptyClassPrompt();
        String list = tableService.getClassListStr(classPrompts);
        setInfoSignal(list);
      }
    } finally {
      setStopSignal(stopMsg);
    }
  }

  public static List<Completers.OptDesc> getOptDescList() {
    return optDescList;
  }
}
