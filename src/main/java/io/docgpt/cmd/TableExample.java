/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class TableExample {
  public static void main(String[] args) {
    AnsiConsole.systemInstall();

    // Table data
    String[] headers = {"Name", "Age", "Gender"};
    String[][] values =
        {{"John", "30", "Male"}, {"Sarah", "25", "Female"}, {"Michael", "40", "Male"}};

    // Table borders
    String horizontalBorder = "+-----------+------+--------+";
    String verticalBorder = "|";
    String headerSeparator = "+";

    // Print table headers
    System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(horizontalBorder));
    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(verticalBorder));
    for (String header : headers) {
      System.out.print(
          Ansi.ansi().fg(Ansi.Color.BLUE).a(String.format("%-11s", header)).a(verticalBorder));
      System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("|"));
      headerSeparator += "-----------+";
    }
    System.out.println();

    // Print table values
    System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(headerSeparator));
    for (String[] row : values) {
      System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(verticalBorder));
      for (String value : row) {
        System.out.print(
            Ansi.ansi().fg(Ansi.Color.WHITE).a(String.format("%-11s", value)).a(verticalBorder));
      }
      System.out.println();
      System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(horizontalBorder));
    }
    System.out.println(Ansi.ansi().reset());
  }
}
