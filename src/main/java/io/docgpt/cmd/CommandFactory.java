/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.parse.CodeContext;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author masaimu
 * @version 2023-05-06 21:42:00
 */
public class CommandFactory {

  private static Map<String /* cmd */, CmdHandler> handlerMap = new HashMap<>();

  private static CodeContext codeContext;
  private static final ThreadPoolExecutor executorService =
      new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
          new BasicThreadFactory.Builder().namingPattern("command-handler").daemon(true).build());

  public static CmdHandler getCmdHandler(String cmd) {
    return handlerMap.get(cmd);
  }

  public static void execute(CmdHandler handler) {
    executorService.execute(handler);
  }

  public synchronized static void setCodeContext(CodeContext codeContext) {
    CommandFactory.codeContext = codeContext;
  }

  public synchronized static CodeContext getCodeContext() {
    return CommandFactory.codeContext;
  }

  public static void registry(String cmd, CmdHandler handler) {
    handlerMap.put(cmd, handler);
  }
}
