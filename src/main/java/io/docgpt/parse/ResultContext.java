/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.parse;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author masaimu
 * @version 2023-05-29 11:45:00
 */
public class ResultContext {

  public static boolean generateUml(String clazz, String method, String plantUml)
      throws IOException {
    if (StringUtils.isEmpty(plantUml)) {
      return true;
    }
    try {
      SyntaxResult syntaxResult = SyntaxChecker.checkSyntax(plantUml);
      if (syntaxResult.isError()) {
        return false;
      }

      String path = clazz + File.separator + method + ".png";
      SourceStringReader reader = new SourceStringReader(plantUml);
      File file = new File(path);
      FileOutputStream output = new FileOutputStream(file);
      reader.outputImage(output, 0);
      output.close();
    } catch (Exception e) {
      throw e;
    }
    return true;
  }
}
