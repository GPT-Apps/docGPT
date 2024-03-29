/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.cmd.signal.CmdSignal;
import io.docgpt.cmd.signal.ErrorSignal;
import io.docgpt.cmd.signal.InfoSignal;
import io.docgpt.cmd.signal.ProgressSignal;
import io.docgpt.cmd.signal.StopSignal;
import io.docgpt.cmd.signal.StreamInfoSignal;
import io.docgpt.cmd.signal.SystemSignal;
import io.docgpt.cmd.signal.WaitSignal;
import io.docgpt.cmd.signal.WarnSignal;
import io.docgpt.exception.CommandException;
import io.docgpt.parse.CodeContext;
import io.docgpt.parse.ResultContext;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author masaimu
 * @version 2023-05-06 21:04:00
 */
public class TerminalService {
  private static Terminal terminal;
  private static LineReader lineReader;

  private static OptionService optionService = new OptionService();

  static {
    try {
      terminal = TerminalBuilder.builder().system(true).build();
    } catch (IOException e) {
      System.err.println("Fail to start terminal for " + e.getMessage());
      throw new RuntimeException(e);
    }
    lineReader =
        LineReaderBuilder.builder().completer(new OptionsCompleters()).terminal(terminal).build();
  }

  public static void start() {
    String prompt = "docgpt> ";
    printBanner(HelpHandler.banner(), true);
    printInfo(HelpHandler.welcome(), false);
    printInfo(((ConfigHandler) CommandFactory.getCmdHandler(ConfigHandler.CONFIG)).checkConfig(),
        false);
    while (true) {
      String line;
      try {
        line = lineReader.readLine(prompt);
        CmdHandler cmdHandler = handleCommand(line);
        if (cmdHandler == null) {
          continue;
        }
        printSignal(cmdHandler);
      } catch (CommandException commandException) {
        printWarn(commandException.getMessage());
      } catch (UserInterruptException e) {
        // Do nothing
      } catch (EndOfFileException e) {
        System.out.println("\nBegin to flush cache...");
        ResultContext.getInstance().flush();
        System.out.println("\nBye.");
        break;
      }
    }
  }

  private static void printSignal(CmdHandler cmdHandler) {
    boolean lastProgress = false;
    while (true) {
      try {
        CmdSignal signal = cmdHandler.getSignal();
        if (signal instanceof StopSignal) {
          printInfo(((StopSignal) signal).reason, lastProgress);
          lastProgress = false;
          break;
        } else if (signal instanceof InfoSignal) {
          printInfo(((InfoSignal) signal).message, lastProgress);
          lastProgress = false;
        } else if (signal instanceof StreamInfoSignal) {
          printStreamInfo(((StreamInfoSignal) signal).message, false);
          lastProgress = false;
        } else if (signal instanceof WarnSignal) {
          printWarn(((WarnSignal) signal).message, lastProgress);
          lastProgress = false;
        } else if (signal instanceof ProgressSignal) {
          printProgress(((ProgressSignal) signal).currentProgress,
              ((ProgressSignal) signal).maxProgress);
          lastProgress = true;
        } else if (signal instanceof WaitSignal) {
          printWait(((WaitSignal) signal).message, ((WaitSignal) signal).currentProgress,
              ((WaitSignal) signal).limit);
          lastProgress = true;
        } else if (signal instanceof ErrorSignal) {
          printError(((ErrorSignal) signal).message, lastProgress);
          lastProgress = false;
          break;
        } else if (signal instanceof SystemSignal) {
          printSystem(((SystemSignal) signal).message, lastProgress);
          lastProgress = false;
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
        break;
      }
    }
  }

  private static void printWait(String message, int currentProgress, int limit) {
    String ansiCode = "[0G";
    StringBuilder sb = new StringBuilder();
    sb.append(ansiCode).append(message);
    int count = currentProgress % limit;
    for (int i = 0; i < count; i++) {
      sb.append(".");
    }
    terminal.writer().print(Ansi.ansi().bg(Ansi.Color.BLACK).fg(Ansi.Color.BLUE).a(sb));
    terminal.flush();
  }

  private static void printProgress(int currentProgress, int maxProgress) {
    String ansiCode = "[0G";
    int width = terminal.getWidth() / 3 * 2;
    int progress = currentProgress * width / maxProgress;
    StringBuilder sb = new StringBuilder();
    sb.append(ansiCode);
    sb.append('[');
    for (int j = 0; j < width; j++) {
      if (j < progress) {
        sb.append('=');
      } else if (j == progress) {
        sb.append('>');
      } else {
        sb.append(' ');
      }
    }
    sb.append(']');
    sb.append(String.format(" %d%%", (currentProgress * 100 / maxProgress)));
    // terminal.getStringCapability(InfoCmp.Capability.cursor_address);
    terminal.writer().print(Ansi.ansi().bg(Ansi.Color.BLACK).fg(Ansi.Color.GREEN).a(sb));
    terminal.flush();
  }

  private static void printSystem(String message, boolean newline) {
    print(message, newline, Ansi.Color.WHITE);
  }

  private static void printInfo(String message, boolean newline) {
    print(message, newline, Ansi.Color.GREEN);
  }

  private static void printStreamInfo(String message, boolean newline) {
    streamPrint(message, newline, Ansi.Color.GREEN);
  }

  private static void printBanner(String message, boolean newline) {
    print(message, newline, Ansi.Color.CYAN);
  }

  private static void print(String message, boolean newline, Ansi.Color color) {
    if (StringUtils.isNotEmpty(message)) {
      Ansi ansi = Ansi.ansi().bg(Ansi.Color.BLACK).fg(color);
      if (newline) {
        ansi.newline();
      }
      ansi.a(message).newline().reset();
      terminal.writer().print(ansi);
      terminal.flush();
    }
  }

  private static void streamPrint(String message, boolean newline, Ansi.Color color) {
    if (StringUtils.isNotEmpty(message)) {
      Ansi ansi = Ansi.ansi().bg(Ansi.Color.BLACK).fg(color);
      if (newline) {
        ansi.newline();
      }
      ansi.a(message).reset();
      terminal.writer().print(ansi);
      terminal.flush();
    }
  }

  private static void printWarn(String message) {
    printWarn(message, false);
  }

  private static void printWarn(String message, boolean newline) {
    if (StringUtils.isNotEmpty(message)) {
      Ansi ansi = Ansi.ansi().bg(Ansi.Color.BLACK).fg(Ansi.Color.YELLOW);
      if (newline) {
        ansi.newline();
      }
      ansi.a(message).newline().reset();
      terminal.writer().print(ansi);
      terminal.flush();
    }
  }

  private static void printError(String message, boolean newline) {
    if (StringUtils.isNotEmpty(message)) {
      Ansi ansi = Ansi.ansi().bg(Ansi.Color.BLACK).fg(Ansi.Color.RED);
      if (newline) {
        ansi.newline();
      }
      ansi.a(message).newline().reset();
      terminal.writer().print(ansi);
      terminal.flush();
    }
  }

  private static CmdHandler handleCommand(String line) {
    if (StringUtils.isBlank(line)) {
      return null;
    }
    String[] arr = line.split(" ");
    List<String> argList = new ArrayList<>();
    for (String arg : arr) {
      if (StringUtils.isBlank(arg)) {
        continue;
      }
      argList.add(arg.trim());
    }
    String[] args = argList.toArray(new String[] {});
    return optionService.parseAndExecute(args);
  }

  public static Terminal terminal() {
    return terminal;
  }

  public static CodeContext load(File dir) {
    return new CodeContext(dir.getAbsolutePath());
  }

  public static void main(String[] args) {
    TerminalService.start();
  }
}
