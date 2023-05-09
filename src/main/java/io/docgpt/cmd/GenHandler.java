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

import java.util.List;

/**
 * @author masaimu
 * @version 2023-05-08 17:07:00
 */
public class GenHandler extends CmdHandler {

  static Options options = new Options();

  CommandLine commandLine;

  OpenAIService openAIService = new OpenAIService();

  static {
    options.addOption("m", "method", true, "simple java name");
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
      String method = StringUtils.EMPTY;

      if (commandLine.hasOption("m")) {
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
      setStopSignal(stopMsg);
    }
  }
}
