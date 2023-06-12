/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.parse.CodeContext;
import io.docgpt.prompt.ClassPrompt;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;
import org.jline.builtins.Completers.OptDesc;
import org.jline.reader.impl.completer.NullCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.docgpt.cmd.TerminalService.terminal;

/**
 * @author masaimu
 * @version 2023-05-07 20:19:00
 */
public class ListHandler extends CmdHandler {

  static Options options = new Options();

  CommandLine commandLine;
  TableService tableService = new TableService(terminal());

  public static final String LIST = "ls";

  private static final List<OptDesc> optDescList = new ArrayList<>();

  private static final Map<String, String /* arg example */> expMap = new HashMap<>();

  static {
    OptDesc c =
        new OptDesc("-c", "--class", "Specify class to list methods", LoadHandler.completer);
    optDescList.add(c);
    options.addOption(shortOpt(c.shortOption()), longOpt(c.longOption()), true, c.description());

    OptDesc a = new OptDesc("-a", "--all", "List all class", NullCompleter.INSTANCE);
    optDescList.add(a);
    options.addOption(shortOpt(a.shortOption()), longOpt(a.longOption()), false, a.description());
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
  public void run() {
    handler();
  }

  public void handler() {
    try {
      String clazz = StringUtils.EMPTY;

      if (commandLine != null && commandLine.hasOption("c")) {
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
        if (CollectionUtils.isEmpty(fullNameList)) {
          setWarnSignal("Can not find any method of " + clazz);
          return;
        }
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
      setStopSignal();
    }
  }

  @Override
  public String getCmd() {
    return LIST;
  }

  @Override
  public Map<String, String> getExpMap() {
    return expMap;
  }

  @Override
  public List<Completers.OptDesc> getOptDescList() {
    return optDescList;
  }
}
