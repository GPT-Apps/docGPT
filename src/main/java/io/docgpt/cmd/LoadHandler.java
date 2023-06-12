/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.cmd;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.docgpt.parse.ClassParser;
import io.docgpt.parse.CodeContext;
import io.docgpt.parse.MethodParser;
import io.docgpt.prompt.ClassPrompt;
import io.docgpt.prompt.MethodPrompt;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers.FileNameCompleter;
import org.jline.builtins.Completers.OptDesc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;


/**
 * @author masaimu
 * @version 2023-05-06 21:46:00
 */
public class LoadHandler extends CmdHandler {
  static Options options = new Options();

  public static final String USER_HOME = System.getProperty("user.home");

  public static final String LOAD = "load";

  CommandLine commandLine;

  JavaParser javaParser;

  private static final List<OptDesc> optDescList = new ArrayList<>();

  private static final Map<String, String /* arg example */> expMap = new HashMap<>();

  static ClassCompleter completer = new ClassCompleter();

  static {
    OptDesc d = new OptDesc("-d", "--directory", "Specify Java file directory path to load class",
        new FileNameCompleter());
    expMap.put(d.shortOption(), "<Java file directory>");
    optDescList.add(d);
    options.addOption(shortOpt(d.shortOption()), longOpt(d.longOption()), true, d.description());
  }

  public void parseOption(String[] args) {
    try {
      this.commandLine = parser.parse(options, args);
    } catch (ParseException e) {
      setErrorSignal(e.getMessage());
      setStopSignal();
    }
  }

  @Override
  public void run() {
    handler();
  }

  public void handler() {
    String stopMsg = " ";
    try {
      String directory = StringUtils.EMPTY;
      ConfigHandler configHandler =
          (ConfigHandler) CommandFactory.getCmdHandler(ConfigHandler.CONFIG);
      Properties props = configHandler.getLocalProp();
      if (commandLine != null) {
        if (commandLine.hasOption("d")) {
          directory = commandLine.getOptionValue("d");
        } else {
          directory = props.getProperty("dir", StringUtils.EMPTY);
        }
      }
      if (directory.startsWith("~" + File.separator)) {
        directory = USER_HOME + directory.substring(1);
      }
      File dir = new File(directory);
      if (!dir.isDirectory()) {
        setWarnSignal(directory + " is not directory");
        return;
      }
      props.put("dir", directory);
      configHandler.storeProp(props);
      CodeContext codeContext = TerminalService.load(dir);
      setInfoSignal("Begin to load project files...");
      codeContext.loadProjects(dir);
      setInfoSignal(
          String.format("There are a total of %d Java files, and parsing is now starting...",
              codeContext.getFileSize()));
      init(codeContext);
      setInfoSignal("Begin to parse Java files...");
      preLoad(codeContext);
      stopMsg = "The Java code parsing has been completed.";
      CommandFactory.setCodeContext(codeContext);
      completer.setClazzVars(new ArrayList<>(codeContext.nameCache.keySet()));
    } finally {
      setStopSignal(stopMsg);
    }
  }

  public void init(CodeContext codeContext) {
    // Create a symbol resolver using a ReflectionTypeSolver
    ReflectionTypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(reflectionTypeSolver);

    // Create a parser configuration with the symbol resolver
    ParserConfiguration parserConfiguration = new ParserConfiguration();
    parserConfiguration.setSymbolResolver(symbolSolver);

    javaParser = new JavaParser(parserConfiguration);

    setInfoSignal("Begin to parse packages...");
    packageLoad(codeContext);

    CombinedTypeSolver typeSolver = new CombinedTypeSolver();

    typeSolver.add(reflectionTypeSolver);
    for (String folder : codeContext.sourceDirs) {
      typeSolver.add(new JavaParserTypeSolver(folder));
    }

    symbolSolver = new JavaSymbolSolver(typeSolver);
    parserConfiguration.setSymbolResolver(symbolSolver);

    javaParser = new JavaParser(parserConfiguration);
  }

  public void packageLoad(CodeContext context) {
    int size = context.getFileSize();
    for (int i = 0; i < size; i++) {
      File javaFile = context.javaFiles.get(i);
      try {
        PackageVisitor packageVisitor = new PackageVisitor();
        ParseResult<CompilationUnit> parseResult = javaParser.parse(javaFile);
        if (!parseResult.isSuccessful()) {
          setWarnSignal("fail to parse " + javaFile.getName());
          continue;
        }
        CompilationUnit unit = parseResult.getResult().get();
        unit.accept(packageVisitor, null);
        String packageName = packageVisitor.packageName;
        String sourceDirectory;
        if (StringUtils.isNotBlank(packageName)) {
          String packagePath = packageName.replace(".", File.separator);
          sourceDirectory = javaFile.getPath().split(packagePath)[0];
        } else {
          sourceDirectory = javaFile.getParent();
        }
        context.sourceDirs.add(sourceDirectory);
        setProgressSignal(size, i);
      } catch (Exception e) {
        setErrorSignal("fail to parse " + javaFile.getAbsolutePath() + " for " + e.getMessage());
      }
    }
  }

  public void preLoad(CodeContext context) {
    int size = context.getFileSize();
    for (int i = 0; i < size; i++) {
      File javaFile = context.javaFiles.get(i);
      try {
        ClassVisitor classVisitor = new ClassVisitor();

        ParseResult<CompilationUnit> parseResult = javaParser.parse(javaFile);
        if (!parseResult.isSuccessful()) {
          setWarnSignal("fail to parse " + javaFile.getName());
          continue;
        }
        CompilationUnit unit = parseResult.getResult().get();
        unit.accept(classVisitor, null);
        List<ClassPrompt> classPrompts = new ArrayList<>(classVisitor.classPrompts.values());
        for (ClassPrompt classPrompt : classPrompts) {
          if (!CollectionUtils.isEmpty(classPrompt.getClassAnnotations())) {
            for (MethodPrompt methodPrompt : classPrompt.getMethodCache().values()) {
              methodPrompt.addAnnotations(classPrompt.getClassAnnotations());
            }
          }
          if (StringUtils.isEmpty(classPrompt.getSimpleName())) {
            continue;
          }
          context.cache.put(classPrompt.getFullyQualifiedName(), classPrompt);
          List<String> fullNames = context.nameCache.computeIfAbsent(classPrompt.getSimpleName(),
              k -> new ArrayList<>());
          fullNames.add(classPrompt.getFullyQualifiedName());
        }
        setProgressSignal(size, i);
      } catch (Exception e) {
        setErrorSignal("fail to parse " + javaFile.getAbsolutePath() + " for " + e.getMessage());
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
    Map<String /* class name */, ClassPrompt> classPrompts = new HashMap<>();

    @Override
    public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
      try {
        ClassPrompt classPrompt = new ClassPrompt();
        ClassParser.parseSimpleName(cid, classPrompt);
        ClassParser.parseFullyQualifiedName(cid, classPrompt);
        ClassParser.parseAnnotation(cid, classPrompt);
        ClassParser.parseField(cid, classPrompt);
        ClassParser.parseDeclaratioin(cid, classPrompt);
        classPrompts.put(classPrompt.getSimpleName(), classPrompt);
      } finally {
        super.visit(cid, arg);
      }
    }

    @Override
    public void visit(MethodDeclaration md, Void arg) {
      try {
        Optional<ClassOrInterfaceDeclaration> optional =
            md.findAncestor(ClassOrInterfaceDeclaration.class);
        if (!optional.isPresent()) {
          return;
        }
        String className = optional.get().getNameAsString();
        ClassPrompt classPrompt = classPrompts.get(className);
        if (classPrompt == null) {
          System.err.println("can not find classPrompt by " + className);
        }
        MethodPrompt methodPrompt = new MethodPrompt();
        methodPrompt.setClassPrompt(classPrompt);
        MethodParser.parseAccessSpecifier(md, methodPrompt);
        MethodParser.parseDeclaration(md, methodPrompt);
        MethodParser.parseSimpleName(md, methodPrompt);
        MethodParser.parseParameter(md, methodPrompt);
        MethodParser.parseComment(md, methodPrompt);
        MethodParser.parseAnnotation(md, methodPrompt);
        MethodParser.parseCode(md, methodPrompt);
        MethodParser.parseResponse(md, methodPrompt);
        classPrompt.cacheMethod(methodPrompt);
      } finally {
        super.visit(md, arg);
      }
    }

  }

  @Override
  public String getCmd() {
    return LOAD;
  }

  @Override
  public Map<String, String> getExpMap() {
    return expMap;
  }

  @Override
  public List<OptDesc> getOptDescList() {
    return optDescList;
  }
}
