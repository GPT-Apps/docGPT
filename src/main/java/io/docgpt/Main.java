/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import io.docgpt.cmd.CommandFactory;
import io.docgpt.cmd.ListHandler;
import io.docgpt.cmd.LoadHandler;
import io.docgpt.cmd.TableService;
import io.docgpt.cmd.TerminalService;
import io.docgpt.parse.CodeContext;
import io.docgpt.prompt.OpenAIService;
import io.docgpt.cmd.OptionService;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.MethodPrompt;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
      TerminalService.start();

      // String fileName = OptionService.parseFileName(args);
      // List<String> fullNameList = codeContext.nameCache.get(fileName);
      // String fullName = fullNameList.get(0);
      // ClassPrompt classPrompt = codeContext.cache.get(fullName);
      // String methodName = OptionService.parseMethodName(args);
      // MethodPrompt methodPrompt = classPrompt.getMethodPrompt(methodName);
      //
      // TableService tableService = new TableService(terminal);
      // System.out.println(tableService.listMethodTableBySimpleName(fileName, codeContext, null));
      //
      // if (OptionService.callTarget(args)) {
      // OpenAIService openAIService = new OpenAIService();
      // List<ChatCompletionChoice> choices =
      // openAIService.invoke(methodPrompt.getPromptStr(codeContext.cache));
      // for (ChatCompletionChoice choice : choices) {
      // System.out.println(choice.getMessage().getContent());
      // }
      // } else {
      // System.out.println(methodPrompt.getPromptStr(codeContext.cache));
      // }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
