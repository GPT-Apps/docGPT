/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.parse;

import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.ClassSummaryContext;
import io.docgpt.prompt.MethodPrompt;
import io.docgpt.prompt.MethodSummaryContext;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.docgpt.cmd.LoadHandler.USER_HOME;

/**
 * @author masaimu
 * @version 2023-05-29 11:45:00
 */
public class ResultContext {

  private static final String prefix = "class__";
  private static ResultContext resultContext = new ResultContext();

  Map<String /* _className */, ClassSummaryContext> contextMap = new HashMap<>();

  public static ResultContext getInstance() {
    return resultContext;
  }

  public ClassSummaryContext getClassSummaryContext(String className) {
    String _className = className.replace(".", "_");
    return contextMap.get(_className);
  }

  public void loadCache() {
    String resultPath = USER_HOME + File.separator + "docgpt" + File.separator + "cache";
    File dir = new File(resultPath);
    if (!dir.isDirectory()) {
      return;
    }
    File[] files =
        dir.listFiles((dir1, name) -> StringUtils.isNotBlank(name) && name.startsWith(prefix));
    if (files == null) {
      return;
    }
    for (File classDir : files) {
      if (!classDir.isDirectory()) {
        continue;
      }
      String dirName = classDir.getName();
      String className = dirName.substring(dirName.indexOf(prefix) + prefix.length());
      File[] keyInfoFiles = classDir.listFiles(
          (dir1, name) -> StringUtils.isNotBlank(name) && name.endsWith("class__info.json"));
      if (keyInfoFiles == null) {
        continue;
      }
      FileInputStream fileInputStream = null;
      BufferedReader reader = null;
      try {
        File classInfo = keyInfoFiles[0];
        fileInputStream = new FileInputStream(classInfo);
        reader = new BufferedReader(new InputStreamReader(fileInputStream));
        String str = null;
        StringBuilder json = new StringBuilder();
        while ((str = reader.readLine()) != null) {
          json.append(str);
        }
        ClassSummaryContext classSummaryContext =
            JsonUtil.fromJson(json.toString(), ClassSummaryContext.class);
        contextMap.put(className, classSummaryContext);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        try {
          if (fileInputStream != null) {
            fileInputStream.close();
          }
          if (reader != null) {
            reader.close();;
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public void flush() {
    String resultPath = USER_HOME + File.separator + "docgpt" + File.separator + "cache";
    File dir = new File(resultPath);
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new RuntimeException("fail to flush ResultContext.");
      }
    }
    for (Map.Entry<String /* _className */, ClassSummaryContext> entry : this.contextMap
        .entrySet()) {
      String _className = entry.getKey();
      ClassSummaryContext classSummaryContext = entry.getValue();
      String classDirPath = resultPath + File.separator + prefix + _className;
      File classDir = new File(classDirPath);
      if (!classDir.exists()) {
        boolean success = classDir.mkdir();
        if (!success) {
          throw new RuntimeException("fail to flush mkdir " + classDirPath);
        }
      }
      String filePath = classDirPath + File.separator + "class__info.json";
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(filePath, false);
        fos.write(JsonUtil.toJson(classSummaryContext).getBytes(StandardCharsets.UTF_8));
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        if (fos != null) {
          try {
            fos.flush();
            fos.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

  }

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

  public void cacheMethodKeyInfo(MethodPrompt methodPrompt,
      MethodSummaryContext methodSummaryContext) {
    String _className = methodPrompt.getClassPrompt().getFullyQualifiedName().replace(".", "_");
    ClassSummaryContext classSummaryContext = getClassSummaryContext(_className);
    if (classSummaryContext == null) {
      classSummaryContext = new ClassSummaryContext();
      classSummaryContext.className = methodPrompt.getClassPrompt().getSimpleName();
      classSummaryContext.declaration = methodPrompt.getClassPrompt().getDeclaration();
    }
    classSummaryContext.methodSummaryContexts.put(methodPrompt.declaration, methodSummaryContext);
    contextMap.put(_className, classSummaryContext);
  }

  public void cacheClassKeyInfo(ClassPrompt classPrompt, ClassSummaryContext classSummaryContext) {
    ClassSummaryContext value = getClassSummaryContext(classPrompt.getFullyQualifiedName());
    if (value == null) {
      String _className = classPrompt.getFullyQualifiedName().replace(".", "_");
      contextMap.put(_className, classSummaryContext);
    } else {
      value.argumentList = classSummaryContext.argumentList;
      value.description = classSummaryContext.description;
    }
  }
}
