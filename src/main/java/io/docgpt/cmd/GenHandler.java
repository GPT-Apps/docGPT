/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import io.docgpt.parse.CodeContext;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.MethodPrompt;
import io.docgpt.prompt.OpenAIService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-05-08 17:07:00
 */
public class GenHandler extends CmdHandler {

  static Options options = new Options();

  static MethodCompleter methodCompleter = new MethodCompleter();

  CommandLine commandLine;

  public static final String GENERATE = "gen";

  private static final List<Completers.OptDesc> optDescList = new ArrayList<>();

  private static final Map<String, String /* arg example */> expMap = new HashMap<>();

  OpenAIService openAIService = new OpenAIService();

  static {
    Completers.OptDesc m = new Completers.OptDesc("-m", "--method",
        "Specify method to generate document", methodCompleter);
    expMap.put(m.shortOption(), "<Simple java name>");
    optDescList.add(m);
    options.addOption(shortOpt(m.shortOption()), longOpt(m.longOption()), true, m.description());
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
      String method = StringUtils.EMPTY;

      if (commandLine != null && commandLine.hasOption("m")) {
        method = commandLine.getOptionValue("m");
      }

      CodeContext codeContext = CommandFactory.getCodeContext();
      if (codeContext == null) {
        setWarnSignal("Please load Java project first.");
        return;
      }
      if (codeContext.activeClassPrompt == null) {
        setWarnSignal("Please choose Java class first.");
        return;
      }
      ClassPrompt activeClassPrompt = codeContext.activeClassPrompt;
      if (StringUtils.isNotBlank(method)) {
        MethodPrompt methodPrompt = activeClassPrompt.getMethodPrompt(method);
        List<ChatCompletionChoice> choices =
            openAIService.invoke(methodPrompt.getPromptStr(codeContext.cache));
        for (ChatCompletionChoice choice : choices) {
          setInfoSignal(choice.getMessage().getContent());
        }
      }
    } finally {
      setStopSignal();
    }
  }

  @Override
  public String getCmd() {
    return GENERATE;
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
