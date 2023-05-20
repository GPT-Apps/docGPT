/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import org.jline.reader.Candidate;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author masaimu
 * @version 2023-05-20 18:05:00
 */
public class ArgumentStringsCompleter extends StringsCompleter {

  public ArgumentStringsCompleter(String... strings) {
    this(Arrays.asList(strings));
  }

  public ArgumentStringsCompleter(Iterable<String> strings) {
    assert strings != null;
    this.candidates = new ArrayList<>();
    for (String string : strings) {
      String display = string;
      if (display.endsWith("=")) {
        display = display.substring(0, display.length() - 1);
      }
      candidates.add(
          new Candidate(AttributedString.stripAnsi(string), display, null, null, null, null, true));
    }
  }
}
