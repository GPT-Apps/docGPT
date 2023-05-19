/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import io.docgpt.parse.CodeContext;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.MethodPrompt;
import org.apache.commons.collections.CollectionUtils;
import org.fusesource.jansi.Ansi;
import org.jline.terminal.Terminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author masaimu
 * @version 2023-05-06 09:51:00
 */
public class TableService {

  private Terminal terminal;

  public TableService(Terminal terminal) {
    this.terminal = terminal;
  }

  public String listMethodTableBySimpleName(String simpleName, CodeContext codeContext,
      Long promptToken) {
    StringBuilder msg = new StringBuilder();
    List<ClassPrompt> classPrompts = codeContext.getClassPromptBySimpleName(simpleName);
    if (CollectionUtils.isEmpty(classPrompts)) {
      return msg.toString();
    }
    for (ClassPrompt classPrompt : classPrompts) {
      msg.append(getMethodTableStr(classPrompt));
    }
    return msg.toString();
  }

  public String listMethodTable(String fullyQualifiedName, CodeContext codeContext,
      Long promptToken) {
    StringBuilder msg = new StringBuilder();
    ClassPrompt classPrompt = codeContext.getClassPrompt(fullyQualifiedName);
    msg.append(getMethodTableStr(classPrompt));
    return msg.toString();
  }

  public String getClassListStr(List<ClassPrompt> classPrompts) {
    StringBuilder msg = new StringBuilder();
    if (CollectionUtils.isEmpty(classPrompts)) {
      return msg.toString();
    }
    List<String> header = Arrays.asList("SimpleName", "FullyName", "Count of methods");
    List<Integer> widthRatio = Arrays.asList(2, 7, 1);
    List<List<String>> rows = new ArrayList<>();

    for (ClassPrompt classPrompt : classPrompts) {
      List<String> row = Arrays.asList(classPrompt.getSimpleName(),
          classPrompt.getFullyQualifiedName(), String.valueOf(classPrompt.getMethodCache().size()));
      rows.add(row);
    }
    msg.append(drawTable(header, rows, widthRatio, terminal.getWidth()));
    return msg.toString();
  }

  public String getMethodTableStr(ClassPrompt classPrompt) {
    StringBuilder msg = new StringBuilder();
    if (classPrompt == null) {
      return msg.toString();
    }
    List<String> header = Arrays.asList("ID", "METHOD", "DECLARATION");
    List<Integer> widthRatio = Arrays.asList(1, 3, 6);
    List<List<String>> rows = new ArrayList<>();

    int index = 0;
    for (MethodPrompt methodPrompt : classPrompt.getMethodCache().values()) {
      String simpleName = methodPrompt.simpleName;
      String declaration = methodPrompt.declaration;
      List<String> row = Arrays.asList(String.valueOf(index++), simpleName, declaration);
      rows.add(row);
    }
    msg.append(drawTable(header, rows, widthRatio, terminal.getWidth()));
    return msg.toString();
  }

  /**
   * 1:3:6 id:method:declaration
   */
  private String drawTable(List<String> header, List<List<String>> rows, List<Integer> widthRatio,
      int size) {
    int sum = 0;
    for (Integer ratio : widthRatio) {
      sum += ratio;
    }
    List<Integer> widthList = new ArrayList<>();
    for (Integer ratio : widthRatio) {
      widthList.add(ratio * size / sum);
    }
    List<String> borderList = new ArrayList<>();
    for (Integer width : widthList) {
      StringBuilder columnBorder = new StringBuilder("+");
      for (int i = 1; i < width; i++) {
        columnBorder.append("-");
      }
      borderList.add(columnBorder.toString());
    }
    StringBuilder table = new StringBuilder();
    drawBorder(borderList, table);
    drawContent(header, widthList, table);
    for (List<String> row : rows) {
      drawBorder(borderList, table);
      drawContent(row, widthList, table);
    }
    drawBorder(borderList, table);
    return table.toString();
  }

  private void drawContent(List<String> contents, List<Integer> widthList, StringBuilder table) {
    StringBuilder line = new StringBuilder("|");
    for (int i = 0; i < contents.size(); i++) {
      String content = contents.get(i);
      Integer width = widthList.get(i);
      line.append(Ansi.ansi().bg(Ansi.Color.BLACK).fg(Ansi.Color.GREEN)
          .a(String.format(getFormat(width), content)).a("|"));
    }
    line.append("\n");
    table.append(line);
  }

  private String getFormat(Integer width) {
    return "%-" + (width - 1) + "s";
  }

  private void drawBorder(List<String> borderList, StringBuilder table) {
    StringBuilder line = new StringBuilder();
    for (String border : borderList) {
      line.append(border);
    }
    line.append("+").append("\n");
    table.append(Ansi.ansi().bg(Ansi.Color.BLACK).fg(Ansi.Color.GREEN).a(line).reset().toString());
  }
}
