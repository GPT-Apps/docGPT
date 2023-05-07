/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.parse;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.docgpt.prompt.ClassPrompt;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static io.docgpt.parse.MethodParser.parseTypeName;

/**
 * @author masaimu
 * @version 2023-05-04 16:57:00
 */
public class ClassParser {

  public static void parseSimpleName(ClassOrInterfaceDeclaration cid, ClassPrompt classPrompt) {
    classPrompt.setSimpleName(cid.getNameAsString());
  }

  public static void parseFullyQualifiedName(ClassOrInterfaceDeclaration cid,
      ClassPrompt classPrompt) {
    Optional<String> optional = cid.getFullyQualifiedName();
    optional.ifPresent(classPrompt::setFullyQualifiedName);
  }

  public static void parseAnnotation(ClassOrInterfaceDeclaration cid, ClassPrompt classPrompt) {
    List<AnnotationExpr> annotations = cid.getAnnotations();
    if (CollectionUtils.isEmpty(annotations)) {
      return;
    }
    List<String> classAnnotationExprs = classPrompt.getClassAnnotations();
    for (AnnotationExpr annotation : annotations) {
      classAnnotationExprs.add(annotation.toString());
    }
  }

  public static void parseField(ClassOrInterfaceDeclaration cid, ClassPrompt classPrompt) {
    if (!CollectionUtils.isEmpty(cid.getFields())) {
      for (FieldDeclaration fieldDeclaration : cid.getFields()) {
        if (fieldDeclaration.isAnnotationDeclaration()) {
          continue;
        }
        classPrompt.getFieldDeclarations().add(fieldDeclaration.toString());
        Type type = fieldDeclaration.getElementType();
        parseTypeName(type, 1, classPrompt.fields, type.asString());
      }
    }
  }
}
