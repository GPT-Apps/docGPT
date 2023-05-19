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
 * @version 2023-05-19 17:54:00
 */
public class MethodCompleter implements Completer {
  Completer methodCompleter;

  public MethodCompleter() {
    this.methodCompleter = new StringsCompleter();
  }

  @Override
  public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
    this.methodCompleter.complete(reader, line, candidates);
  }

  public void setMethodVars(List<String> methodList) {
    this.methodCompleter = new StringsCompleter(methodList);
  }
}
