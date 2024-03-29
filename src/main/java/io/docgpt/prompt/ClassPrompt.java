/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-04-23 21:29:00
 */
@Data
public class ClassPrompt {
  String fullyQualifiedName;

  String simpleName;
  String declaration;

  List<String> parentClass = new ArrayList<>();
  List<String> interfaces = new ArrayList<>();
  List<String> classAnnotations = new ArrayList<>();
  List<String> fieldDeclarations = new ArrayList<>();
  Map<String /* declaration */, MethodPrompt> methodCache = new HashMap<>();
  Map<String /* methodName */, List<String /* declaration */>> methodNameCache = new HashMap<>();
  public Map<String /* simpleName */, String /* fullName */> fields = new HashMap<>();

  public String classDescription;
  public Map<String /* field name */, ClassPrompt> dependencySet = new HashMap<>();

  ClassSummaryContext classSummaryContext;

  public MethodPrompt getMethodPrompt(String methodName) {
    List<String /* declaration */> declarations = methodNameCache.get(methodName);
    if (CollectionUtils.isEmpty(declarations)) {
      return new MethodPrompt();
    } else {
      String declaration = declarations.get(0);
      return methodCache.get(declaration);
    }
  }

  public void cacheMethod(MethodPrompt methodPrompt) {
    this.methodCache.put(methodPrompt.declaration, methodPrompt);
    List<String /* declaration */> declarations =
        this.methodNameCache.computeIfAbsent(methodPrompt.simpleName, k -> new ArrayList<>());
    declarations.add(methodPrompt.declaration);
  }

  public String getUmlPromptStr() {
    StringBuilder prompt = new StringBuilder();
    prompt.append(FormatPrompt.getUmlClassKeyInfoPrompt());
    prompt.append("\"\"\" \n");
    prompt.append(FormatPrompt.getClassCode(this)).append("\"\"\" \n");
    prompt.append(FormatPrompt.getClassUmlActivityFormat());
    return prompt.toString();
  }
}
