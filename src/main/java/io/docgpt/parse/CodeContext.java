/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.parse;

import io.docgpt.prompt.ClassPrompt;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author masaimu
 * @version 2023-04-16 21:15:00
 */
public class CodeContext {
  String dir;

  public Set<String> sourceDirs = new HashSet<>();

  public Map<String /* fullName */, ClassPrompt> cache = new HashMap<>();
  public Map<String /* simpleName */, List<String /* fullName */>> nameCache = new HashMap<>();

  public List<File> javaFiles = new ArrayList<>();

  public ClassPrompt activeClassPrompt;

  public CodeContext() {

  }

  public CodeContext(String dir) {
    this.dir = dir;
  }

  public void loadProjects(File dir) {
    if (dir.isFile()) {
      if (dir.getName().endsWith(".java")) {
        this.javaFiles.add(dir);
      }
    } else {
      File[] files = dir.listFiles(pathname -> pathname.isDirectory()
          || (pathname.isFile() && pathname.getName().endsWith(".java")));
      if (files == null) {
        return;
      }
      for (File file : files) {
        loadProjects(file);
      }
    }
  }

  public ClassPrompt getClassPrompt(String fullyQualifiedName) {
    return this.cache.get(fullyQualifiedName);
  }

  public List<ClassPrompt> getClassPromptBySimpleName(String simpleName) {
    List<String> fullyQualifiedNames = this.nameCache.get(simpleName);
    if (CollectionUtils.isEmpty(fullyQualifiedNames)) {
      return Collections.emptyList();
    }
    List<ClassPrompt> classPrompts = new ArrayList<>();
    for (String fullyQualifiedName : fullyQualifiedNames) {
      ClassPrompt classPrompt = this.cache.get(fullyQualifiedName);
      if (classPrompt != null) {
        classPrompts.add(classPrompt);
      }
    }
    return classPrompts;
  }

  public List<ClassPrompt> getAllClassPrompt() {
    return new ArrayList<>(this.cache.values());
  }

  public List<ClassPrompt> getNonemptyClassPrompt() {
    List<ClassPrompt> list = new ArrayList<>();
    for (ClassPrompt classPrompt : this.cache.values()) {
      if (classPrompt.getMethodCache().size() > 0) {
        list.add(classPrompt);
      }
    }
    list.sort((o1, o2) -> Integer.compare(o2.getMethodCache().size(), o1.getMethodCache().size()));
    return list;
  }

  public int getFileSize() {
    return this.javaFiles.size();
  }


}
