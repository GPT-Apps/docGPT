/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.MethodPrompt;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author masaimu
 * @version 2023-04-16 21:15:00
 */
public class CodeParser {

  JavaParser javaParser;
  String dir;

  Set<String> sourceDirs = new HashSet<>();

  public Map<String /* fullName */, ClassPrompt> cache = new HashMap<>();
  public Map<String /* simpleName */, List<String /* fullName */>> nameCache = new HashMap<>();

  public CodeParser() {

  }

  public CodeParser(String dir) {
    this.dir = dir;
  }

  public void init() {
    // Create a symbol resolver using a ReflectionTypeSolver
    ReflectionTypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(reflectionTypeSolver);

    // Create a parser configuration with the symbol resolver
    ParserConfiguration parserConfiguration = new ParserConfiguration();
    parserConfiguration.setSymbolResolver(symbolSolver);

    javaParser = new JavaParser(parserConfiguration);

    if (StringUtils.isNotEmpty(dir)) {
      packageLoad(new File(dir));

      CombinedTypeSolver typeSolver = new CombinedTypeSolver();

      typeSolver.add(reflectionTypeSolver);
      for (String folder : this.sourceDirs) {
        typeSolver.add(new JavaParserTypeSolver(folder));
      }

      symbolSolver = new JavaSymbolSolver(typeSolver);
      parserConfiguration.setSymbolResolver(symbolSolver);

      javaParser = new JavaParser(parserConfiguration);
    }
  }

  public void preLoad(File dir) {
    if (dir.isFile()) {
      if (dir.getName().endsWith(".java")) {
        try {
          ClassVisitor classVisitor = new ClassVisitor();

          ParseResult<CompilationUnit> parseResult = javaParser.parse(dir);
          if (!parseResult.isSuccessful()) {
            System.err.println("fail to parse " + dir.getName());
          }
          CompilationUnit unit = parseResult.getResult().get();
          unit.accept(classVisitor, null);
          ClassPrompt classPrompt = classVisitor.classPrompt;
          if (!CollectionUtils.isEmpty(classPrompt.getClassAnnotations())) {
            for (MethodPrompt methodPrompt : classPrompt.getMethodCache().values()) {
              methodPrompt.addAnnotations(classPrompt.getClassAnnotations());
            }
          }
          cache.put(classPrompt.getFullyQualifiedName(), classPrompt);
          List<String> fullNames =
              nameCache.computeIfAbsent(classPrompt.getSimpleName(), k -> new ArrayList<>());
          fullNames.add(classPrompt.getFullyQualifiedName());
        } catch (Exception e) {
          System.err.println("fail to parse " + dir.getAbsolutePath() + " for " + e.getMessage());
          e.printStackTrace();
        }
      }
    } else {
      File[] files = dir.listFiles(pathname -> pathname.isDirectory()
          || (pathname.isFile() && pathname.getName().endsWith(".java")));
      for (File file : files) {
        preLoad(file);
      }
    }
  }

  public void packageLoad(File dir) {
    if (dir.isFile()) {
      if (dir.getName().endsWith(".java")) {
        try {
          PackageVisitor packageVisitor = new PackageVisitor();

          ParseResult<CompilationUnit> parseResult = javaParser.parse(dir);
          if (!parseResult.isSuccessful()) {
            System.err.println("fail to parse " + dir.getName());
          }
          CompilationUnit unit = parseResult.getResult().get();
          unit.accept(packageVisitor, null);
          String packageName = packageVisitor.packageName;
          String packagePath = packageName.replace(".", File.separator);
          String sourceDirectory = dir.getPath().split(packagePath)[0];
          // System.out.println(sourceDirectory);
          sourceDirs.add(sourceDirectory);
        } catch (Exception e) {
          System.err.println("fail to parse " + dir.getAbsolutePath() + " for " + e.getMessage());
          e.printStackTrace();
        }
      }
    } else {
      File[] files = dir.listFiles(pathname -> pathname.isDirectory()
          || (pathname.isFile() && pathname.getName().endsWith(".java")));
      for (File file : files) {
        packageLoad(file);
      }
    }
  }

  private static class PackageVisitor extends VoidVisitorAdapter<Void> {
    String packageName;

    @Override
    public void visit(PackageDeclaration pd, Void arg) {
      packageName = pd.getName().asString();
      super.visit(pd, arg);
    }
  }

  // 自定义访问者，用于访问方法
  private static class ClassVisitor extends VoidVisitorAdapter<Void> {
    ClassPrompt classPrompt = new ClassPrompt();

    @Override
    public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
      try {
        classPrompt.setSimpleName(cid.getNameAsString());
        classPrompt.setFullyQualifiedName(cid.getFullyQualifiedName().get());
        List<String> classAnnotationExprs = classPrompt.getClassAnnotations();
        List<AnnotationExpr> annotations = cid.getAnnotations();
        for (AnnotationExpr annotation : annotations) {
          classAnnotationExprs.add(annotation.toString());
        }
        if (!CollectionUtils.isEmpty(cid.getFields())) {
          for (FieldDeclaration fieldDeclaration : cid.getFields()) {
            if (fieldDeclaration.isAnnotationDeclaration()) {
              continue;
            }

            classPrompt.getFieldAnnotations().add(fieldDeclaration.toString());
          }
        }

      } finally {
        super.visit(cid, arg);
      }
    }

    @Override
    public void visit(MethodDeclaration md, Void arg) {
      try {

        if (CollectionUtils.isEmpty(md.getAnnotations())) {
          return;
        }
        if (!isApiAnnotations(md.getAnnotations())) {
          return;
        }
        MethodPrompt methodPrompt = new MethodPrompt();
        methodPrompt.setClassPrompt(classPrompt);
        methodPrompt.declaration = md.getDeclarationAsString();
        methodPrompt.simpleName = md.getNameAsString();

        // parse method parameters
        List<Parameter> parameters = md.getParameters();
        for (Parameter parameter : parameters) {
          String simpleName = parameter.getTypeAsString();
          Type type = parameter.getType();
          try {
            String fullyQualifiedName = type.resolve().asReferenceType().getQualifiedName();
            methodPrompt.parameters.put(simpleName, fullyQualifiedName);
          } catch (Exception e) {
            System.out.println("fail to get fullyQualifiedName of " + simpleName);
            continue;
          }
          if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
            if (classOrInterfaceType.getTypeArguments().isPresent()) {
              // Get the type argument
              Type typeArgument = classOrInterfaceType.getTypeArguments().get().get(0);

              // If the type argument is a type parameter, get the name of the type parameter
              if (typeArgument instanceof ClassOrInterfaceType) {
                String simpleName1 = ((ClassOrInterfaceType) typeArgument).getName().asString();
                String fullyQualifiedName1 =
                    typeArgument.resolve().asReferenceType().getQualifiedName();
                methodPrompt.parameters.put(simpleName1, fullyQualifiedName1);
              }
            }
          }
        }
        // 获取方法注释
        Optional<Comment> optional = md.getComment();
        optional.ifPresent(comment -> methodPrompt.comment = comment.getContent());

        List<AnnotationExpr> annotations = md.getAnnotations();
        for (AnnotationExpr annotation : annotations) {
          methodPrompt.annotations.add(annotation.toString());
        }

        String methodBody = md.getBody().get().toString();
        methodPrompt.code = methodBody;
        classPrompt.getMethodCache().put(methodPrompt.declaration, methodPrompt);
        List<String /* declaration */> declarations = classPrompt.getMethodNameCache()
            .computeIfAbsent(methodPrompt.simpleName, k -> new ArrayList<>());
        declarations.add(methodPrompt.declaration);
      } finally {
        super.visit(md, arg);
      }
    }

    private boolean isApiAnnotations(NodeList<AnnotationExpr> annotations) {
      if (CollectionUtils.isEmpty(annotations)) {
        return false;
      }
      for (AnnotationExpr annotationExpr : annotations) {
        String name = annotationExpr.getName().asString();
        if (name.contains("Mapping")) {
          return true;
        }
      }
      return false;
    }
  }
}
