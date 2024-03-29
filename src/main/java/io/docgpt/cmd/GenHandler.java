/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.unfbx.chatgpt.entity.chat.ChatChoice;
import io.docgpt.parse.CodeContext;
import io.docgpt.parse.JsonUtil;
import io.docgpt.parse.ResultContext;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.ClassSummaryContext;
import io.docgpt.prompt.FormatPrompt;
import io.docgpt.prompt.MethodPrompt;
import io.docgpt.prompt.MethodSummaryContext;
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
    Completers.OptDesc s =
        new Completers.OptDesc("-s", "--stream", "Stream mode", NullCompleter.INSTANCE);
    expMap.put(m.shortOption(), "<Simple method name>");
    optDescList.add(m);
    optDescList.add(u);
    optDescList.add(s);
    options.addOption(shortOpt(m.shortOption()), longOpt(m.longOption()), true, m.description());
    options.addOption(shortOpt(u.shortOption()), longOpt(u.longOption()), false, u.description());
    options.addOption(shortOpt(s.shortOption()), longOpt(s.longOption()), false, s.description());

    Completers.OptDesc c = new Completers.OptDesc("-c", "--class",
        "Specify class to generate document", LoadHandler.completer);
    expMap.put(c.shortOption(), "<Simple class name>");
    optDescList.add(c);
    options.addOption(shortOpt(c.shortOption()), longOpt(c.longOption()), true, c.description());
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
      String clazz = StringUtils.EMPTY;

      if (commandLine == null) {
        setWarnSignal("Please specify method or class first.");
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
          generateMethodUml(methodPrompt, method, activeClassPrompt);
        } else {
          doGenerateDoc(methodPrompt, codeContext, activeClassPrompt, method,
              commandLine.hasOption("s"));
        }
      } else {
        ClassPrompt classPrompt = activeClassPrompt;
        if (StringUtils.isNotEmpty(clazz)) {
          List<String> fullNameList = codeContext.nameCache.get(clazz);
          String fullName = fullNameList.get(0);
          classPrompt = codeContext.cache.get(fullName);
        }
        if (classPrompt == null) {
          setWarnSignal("Please specify effective class first.");
          return;
        }
        if (needUml) {
          generateClassUml(classPrompt);
        }
      }
    } catch (Exception e) {
      setErrorSignal(e.getMessage());
    } finally {
      setStopSignal();
    }
  }

  private void doGenerateDoc(MethodPrompt methodPrompt, CodeContext codeContext,
      ClassPrompt activeClassPrompt, String method, boolean stream) throws InterruptedException {
    String prompt = methodPrompt.getPromptStr(codeContext.cache);
    setSystemSignal("Begin to invoke OpenAI API, prompt length is " + prompt.length());
    setSystemSignal("Prompt content as follows:  " + prompt);


    if (stream) {
      invokeWithStream(prompt);
    } else {
      List<ChatChoice> choices = invokeAndWait(prompt);

      for (ChatChoice choice : choices) {
        String content = choice.getMessage().getContent();
        if (StringUtils.isBlank(content)) {
          continue;
        }
        setInfoSignal(choice.getMessage().getContent());
        String filePath =
            output(activeClassPrompt.getFullyQualifiedName(), method + "__api.md", content);
        setSystemSignal("Document has been saved as " + filePath);
      }
    }

  }

  protected String classPath(String clazz) {
    ConfigHandler configHandler =
        (ConfigHandler) CommandFactory.getCmdHandler(ConfigHandler.CONFIG);
    Properties props = configHandler.getLocalProp();
    String home = USER_HOME;
    String output =
        props.getProperty("output", String.join(File.separator, home, "docgpt", "result"));
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

  private String output(String clazz, String fileName, String content) {
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
    return filePath;
  }

  private void generateClassUml(ClassPrompt classPrompt) throws InterruptedException, IOException {
    String keyInfo = doGenerateClassKeyInfo(classPrompt);
    if (StringUtils.isNotBlank(keyInfo)) {
      boolean success = false;
      int repeat = 0;
      do {
        String umlPrompt = FormatPrompt.getUmlClassPrompt(keyInfo);
        setSystemSignal(
            "Begin to invoke OpenAI API to uml, prompt length is " + umlPrompt.length());
        List<ChatChoice> choices = invokeAndWait(umlPrompt);
        String plantUmlCode = null;
        for (ChatChoice choice : choices) {
          setInfoSignal(choice.getMessage().getContent());
          plantUmlCode = choice.getMessage().getContent();
          String clazzPath = classPath(classPrompt.getFullyQualifiedName());
          setInfoSignal("Try to generate UML diagram.");
          success = ResultContext.generateUml(clazzPath, classPrompt.getSimpleName() + "__class",
              plantUmlCode);
          if (!success) {
            setWarnSignal("The PlantUML code generated by OpenAI is SyntaxError, retry.");
          } else {
            String path = clazzPath + File.separator + classPrompt.getSimpleName() + ".png";
            setInfoSignal("Generate UML diagram " + path);
          }
        }
      } while (!success && repeat++ < 4);
      if (!success && repeat >= 4) {
        setWarnSignal("The maximum number of retries has been reached. End this attempt.");
      }
    }
  }

  private String doGenerateClassKeyInfo(ClassPrompt classPrompt) throws InterruptedException {
    for (MethodPrompt methodPrompt : classPrompt.getMethodCache().values()) {
      doGenerateMethodKeyInfo(methodPrompt);
    }
    String classUmlKeyInfoPrompt = classPrompt.getUmlPromptStr();
    setSystemSignal(
        "Begin to invoke OpenAI API, prompt length is " + classUmlKeyInfoPrompt.length());
    setSystemSignal("Prompt content as follows:  " + classUmlKeyInfoPrompt);
    List<ChatChoice> choices = invokeAndWait(classUmlKeyInfoPrompt);
    String keyInfo = null;
    for (ChatChoice choice : choices) {
      keyInfo = choice.getMessage().getContent();
      if (StringUtils.isBlank(keyInfo)) {
        continue;
      }
      setInfoSignal(keyInfo);
      ClassSummaryContext classSummaryContext =
          JsonUtil.fromJson(keyInfo, ClassSummaryContext.class);
      ResultContext.getInstance().cacheClassKeyInfo(classPrompt, classSummaryContext);
      break;
    }
    return keyInfo;
  }

  private void generateMethodUml(MethodPrompt methodPrompt, String method,
      ClassPrompt activeClassPrompt) throws InterruptedException, IOException {
    String keyInfo = doGenerateMethodKeyInfo(methodPrompt);
    // String filePath =
    // output(activeClassPrompt.getFullyQualifiedName(), method + "__info.json", keyInfo);
    // setInfoSignal("The key information has been saved as " + filePath);
    if (StringUtils.isNotBlank(keyInfo)) {
      boolean success = false;
      int repeat = 0;
      do {
        String umlPrompt = FormatPrompt.getUmlActivityPrompt(keyInfo);
        setSystemSignal(
            "Begin to invoke OpenAI API to uml, prompt length is " + umlPrompt.length());
        List<ChatChoice> choices = invokeAndWait(umlPrompt);
        String plantUmlCode = null;
        for (ChatChoice choice : choices) {
          setInfoSignal(choice.getMessage().getContent());
          plantUmlCode = choice.getMessage().getContent();
          String clazzPath = classPath(activeClassPrompt.getFullyQualifiedName());
          setInfoSignal("Try to generate UML diagram.");
          success = ResultContext.generateUml(clazzPath, method + "__activity", plantUmlCode);
          if (!success) {
            setWarnSignal("The PlantUML code generated by OpenAI is SyntaxError, retry.");
          } else {
            String path = clazzPath + File.separator + method + ".png";
            setInfoSignal("Generate UML diagram " + path);
          }
        }
      } while (!success && repeat++ < 4);
      if (!success && repeat >= 4) {
        setWarnSignal("The maximum number of retries has been reached. End this attempt.");
      }
    }
  }

  private String doGenerateMethodKeyInfo(MethodPrompt methodPrompt) throws InterruptedException {
    String classFullyQualifiedName = methodPrompt.getClassPrompt().getFullyQualifiedName();
    ClassSummaryContext classSummaryContext =
        ResultContext.getInstance().getClassSummaryContext(classFullyQualifiedName);
    if (classSummaryContext != null) {
      setSystemSignal("Try to get method key info from class context.");
      MethodSummaryContext methodSummaryContext =
          classSummaryContext.methodSummaryContexts.get(methodPrompt.declaration);
      if (methodSummaryContext != null) {
        setSystemSignal("Get method key info from class context.");
        methodPrompt.summaryContext = methodSummaryContext;
        return JsonUtil.toJson(methodSummaryContext);
      }
    }
    String prompt = methodPrompt.getUmlPromptStr();
    setSystemSignal(
        "Begin to invoke OpenAI API to extra uml key, prompt length is " + prompt.length());
    setSystemSignal("Prompt content as follows:  " + prompt);
    String keyInfo = null;
    List<ChatChoice> choices = invokeAndWait(prompt);
    for (ChatChoice choice : choices) {
      keyInfo = choice.getMessage().getContent();
      if (StringUtils.isBlank(keyInfo)) {
        continue;
      }
      setInfoSignal(keyInfo);
      break;
    }
    if (StringUtils.isNotBlank(keyInfo) && JsonUtil.isValidJson(keyInfo)) {
      MethodSummaryContext methodSummaryContext =
          JsonUtil.fromJson(keyInfo, MethodSummaryContext.class);
      ResultContext.getInstance().cacheMethodKeyInfo(methodPrompt, methodSummaryContext);
      methodPrompt.summaryContext = methodSummaryContext;
    }
    return keyInfo;
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

  private List<ChatChoice> invokeAndWait(String prompt) throws InterruptedException {
    CountDownLatch invoke = new CountDownLatch(1);
    AtomicBoolean wait = await(invoke);
    List<ChatChoice> choices = openAIService.invoke(prompt);
    wait.set(false);
    invoke.await();
    return choices;
  }

  private void invokeWithStream(String prompt) throws InterruptedException {
    CountDownLatch invoke = new CountDownLatch(1);
    AtomicBoolean wait = streamAwait(invoke);
    this.openAIService.streamInvoke(this, prompt, wait);
    invoke.await();
  }

  private AtomicBoolean streamAwait(CountDownLatch invoke) {
    AtomicBoolean wait = new AtomicBoolean(true);
    new Thread(() -> {
      while (wait.get()) {
        try {
          Thread.sleep(100L);
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
