/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import io.docgpt.cmd.CodeParser;
import io.docgpt.cmd.OpenAIService;
import io.docgpt.cmd.OptionService;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.MethodPrompt;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author masaimu
 * @version ${YEAR}-${MONTH}-${DAY} ${HOUR}:${MINUTE}:00
 */
public class Main {
  public static void main(String[] args) {
    System.out.println(Arrays.asList(args));

    try {
      CodeParser codeParser;
      File dir = OptionService.parseFolder(args);
      if (dir.isDirectory()) {
        codeParser = new CodeParser(dir.getAbsolutePath());
      } else {
        codeParser = new CodeParser();
      }
      codeParser.init();
      codeParser.preLoad(dir);

      List<String> fullNameList = codeParser.nameCache.get(OptionService.parseFileName(args));
      String fullName = fullNameList.get(0);
      ClassPrompt classPrompt = codeParser.cache.get(fullName);
      MethodPrompt methodPrompt = classPrompt.getMethodPrompt(OptionService.parseMethodName(args));

      if (OptionService.callTarget(args)) {
        OpenAIService openAIService = new OpenAIService();
        List<ChatCompletionChoice> choices =
            openAIService.invoke(methodPrompt.getPromptStr(codeParser.cache));
        for (ChatCompletionChoice choice : choices) {
          System.out.println(choice.getMessage().getContent());
        }
      } else {
        System.out.println(methodPrompt.getPromptStr(codeParser.cache));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
