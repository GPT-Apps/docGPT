/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import io.docgpt.prompt.MethodPrompt;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author masaimu
 * @version 2023-05-04 16:41:00
 */
public class MethodParser {

  public static void parseAnnotation(MethodDeclaration md, MethodPrompt methodPrompt) {
    if (CollectionUtils.isEmpty(md.getAnnotations())) {
      return;
    }
    List<AnnotationExpr> annotations = md.getAnnotations();
    for (AnnotationExpr annotation : annotations) {
      methodPrompt.annotations.add(annotation.toString());
    }
  }

  public static void parseComment(MethodDeclaration md, MethodPrompt methodPrompt) {
    Optional<Comment> optional = md.getComment();
    if (!optional.isPresent()) {
      return;
    }
    methodPrompt.comment = optional.get().getContent();
  }

  public static void parseCode(MethodDeclaration md, MethodPrompt methodPrompt) {
    Optional<BlockStmt> optional = md.getBody();
    if (!optional.isPresent()) {
      return;
    }
    methodPrompt.code = optional.get().toString();
  }

  public static void parseParameter(MethodDeclaration md, MethodPrompt methodPrompt) {
    List<Parameter> parameters = md.getParameters();
    if (CollectionUtils.isEmpty(parameters)) {
      return;
    }
    for (Parameter parameter : parameters) {
      String simpleName = parameter.getTypeAsString();
      Type type = parameter.getType();
      parseTypeName(type, 3, methodPrompt.parameters, simpleName);
    }
  }

  public static void parseDeclaration(MethodDeclaration md, MethodPrompt methodPrompt) {
    methodPrompt.declaration = md.getDeclarationAsString();
  }

  public static void parseSimpleName(MethodDeclaration md, MethodPrompt methodPrompt) {
    methodPrompt.simpleName = md.getNameAsString();
  }

  public static void parseResponse(MethodDeclaration md, MethodPrompt methodPrompt) {
    Type returnType = md.getType();
    parseTypeName(returnType, 3, methodPrompt.responses, returnType.asString());
  }

  protected static void parseTypeName(Type type, int step, Map<String, String> typeName,
      String simpleName) {
    if (step < 0 || type == null) {
      return;
    }

    String fullyQualifiedName = tryGetQualifiedName(type, simpleName);
    if (StringUtils.isNotEmpty(fullyQualifiedName)) {
      typeName.put(simpleName, fullyQualifiedName);
    }

    if (type instanceof ClassOrInterfaceType) {
      ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
      Optional<NodeList<Type>> optional = classOrInterfaceType.getTypeArguments();
      if (optional.isPresent() && !CollectionUtils.isEmpty(optional.get())) {
        List<Type> subTypes = optional.get();
        for (Type subType : subTypes) {
          parseTypeName(subType, step - 1, typeName, subType.asString());
        }
      }
    }
  }

  private static String tryGetQualifiedName(Type type, String name) {
    String fullyQualifiedName = null;
    try {
      fullyQualifiedName = type.resolve().asReferenceType().getQualifiedName();
    } catch (Exception e) {
      System.out.println("fail to get fullyQualifiedName of " + name);
      e.printStackTrace();
    }
    return fullyQualifiedName;
  }

}
