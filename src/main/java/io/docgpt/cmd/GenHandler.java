/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import io.docgpt.parse.CodeContext;
import io.docgpt.parse.ResultContext;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.FormatPrompt;
import io.docgpt.prompt.MethodPrompt;
import io.docgpt.prompt.OpenAIService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;
import org.jline.reader.impl.completer.NullCompleter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.docgpt.cmd.LoadHandler.USER_HOME;

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
    Completers.OptDesc u = new Completers.OptDesc("-u", "--uml", "Specify method to generate UML",
        NullCompleter.INSTANCE);
    expMap.put(m.shortOption(), "<Simple java name>");
    optDescList.add(m);
    optDescList.add(u);
    options.addOption(shortOpt(m.shortOption()), longOpt(m.longOption()), true, m.description());
    options.addOption(shortOpt(u.shortOption()), longOpt(u.longOption()), false, u.description());
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

      if (commandLine == null) {
        setWarnSignal("Please specify method first.");
        return;
      }

      if (commandLine.hasOption("m")) {
        method = commandLine.getOptionValue("m");
      }
      boolean needUml = commandLine.hasOption("u");

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
        if (methodPrompt == null) {
          setWarnSignal("Can not find method " + method);
        } else if (needUml) {
          String prompt = methodPrompt.getUmlPromptStr();
          setInfoSignal(
              "Begin to invoke OpenAI API to extra uml key, prompt length is " + prompt.length());
          setInfoSignal("Prompt content as follows:  " + prompt);

          List<ChatCompletionChoice> choices = invokeAndWait(prompt);
          String umlPrompt = null;
          for (ChatCompletionChoice choice : choices) {
            String keyInfo = choice.getMessage().getContent();
            if (StringUtils.isBlank(keyInfo)) {
              continue;
            }
            setInfoSignal(keyInfo);
            umlPrompt = keyInfo;
            output(activeClassPrompt.getFullyQualifiedName(), method + "__info.txt", keyInfo);
          }
          if (StringUtils.isNotBlank(umlPrompt)) {
            umlPrompt = FormatPrompt.getUmlActivityPrompt(umlPrompt);
            setInfoSignal(
                "Begin to invoke OpenAI API to uml, prompt length is " + umlPrompt.length());
            choices = invokeAndWait(umlPrompt);
            String plantUmlCode = null;
            for (ChatCompletionChoice choice : choices) {
              setInfoSignal(choice.getMessage().getContent());
              plantUmlCode = choice.getMessage().getContent();
              String clazzPath = classPath(activeClassPrompt.getFullyQualifiedName());
              ResultContext.generateUml(clazzPath, method + "__activity", plantUmlCode);
            }
          }
        } else {
          String prompt = methodPrompt.getPromptStr(codeContext.cache);
          setInfoSignal("Begin to invoke OpenAI API, prompt length is " + prompt.length());
          setInfoSignal("Prompt content as follows:  " + prompt);

          List<ChatCompletionChoice> choices = invokeAndWait(prompt);

          for (ChatCompletionChoice choice : choices) {
            String content = choice.getMessage().getContent();
            if (StringUtils.isBlank(content)) {
              continue;
            }
            setInfoSignal(choice.getMessage().getContent());
            output(activeClassPrompt.getFullyQualifiedName(), method + "__api.md", content);
          }
        }
      }
    } catch (Exception e) {
      setErrorSignal(e.getMessage());
    } finally {
      setStopSignal();
    }
  }

  protected String classPath(String clazz) {
    ConfigHandler configHandler =
        (ConfigHandler) CommandFactory.getCmdHandler(ConfigHandler.CONFIG);
    Properties props = configHandler.getLocalProp();
    String home = USER_HOME;
    String output = props.getProperty("output", home + File.separator + "docgpt");
    if (output.startsWith("~" + File.separator)) {
      output = USER_HOME + output.substring(1);
    }
    File dir = new File(output);
    if (!dir.exists()) {
      dir.mkdirs();
    } else if (!dir.isDirectory()) {
      throw new RuntimeException(output + " is not directory.");
    }

    String classPath = clazz.replace(".", "_");
    String classAbsolutePath = output + File.separator + classPath;
    File classDir = new File(classAbsolutePath);
    if (!classDir.exists()) {
      classDir.mkdir();
    }
    return classDir.getAbsolutePath();
  }

  private void output(String clazz, String fileName, String content) {
    String filePath = classPath(clazz) + File.separator + fileName;

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(filePath);
      fos.write(content.getBytes(StandardCharsets.UTF_8));
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

  private List<ChatCompletionChoice> invokeAndWait(String prompt) throws InterruptedException {
    CountDownLatch invoke = new CountDownLatch(1);
    AtomicBoolean wait = await(invoke);
    List<ChatCompletionChoice> choices = openAIService.invoke(prompt);
    wait.set(false);
    invoke.await();
    return choices;
  }

  private AtomicBoolean await(CountDownLatch invoke) {
    AtomicBoolean wait = new AtomicBoolean(true);
    new Thread(() -> {
      int i = 0;
      while (wait.get()) {
        try {
          setWaitSignal("Please wait for OpenAI response", i++, 4);
          Thread.sleep(500L);
        } catch (Exception e) {
          break;
        }
      }
      if (invoke != null) {
        invoke.countDown();
      }
    }).start();
    return wait;
  }
}
