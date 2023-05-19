/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.List;

/**
 * @author masaimu
 * @version 2023-05-19 12:28:00
 */
public class ClassCompleter implements Completer {

  Completer clazzCompleter;

  public ClassCompleter() {
    this.clazzCompleter = new StringsCompleter();
  }

  @Override
  public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
    this.clazzCompleter.complete(reader, line, candidates);
  }

  public void setClazzVars(List<String> clazzList) {
    this.clazzCompleter = new StringsCompleter(clazzList);
  }
}
