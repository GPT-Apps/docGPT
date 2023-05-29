/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.parse;

import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author masaimu
 * @version 2023-05-29 11:45:00
 */
public class ResultContext {


  public static void generateUml(String plantUml) throws IOException {
    if (StringUtils.isEmpty(plantUml)) {
      return;
    }
    try {
      SourceStringReader reader = new SourceStringReader(plantUml);
      File file = new File("uml.png");
      FileOutputStream output = new FileOutputStream(file);
      reader.outputImage(output, 0);
      output.close();
    } catch (Exception e) {
      throw e;
    }
  }

  public static void main(String[] args) throws IOException {
    ResultContext.generateUml("@startuml\n" + "|Main|\n" + "start\n" + "\n"
        + ":create PeerId object;\n" + ":getLeader (groupId, conf, leaderId);\n"
        + "if (result is not okay?) then (no)\n" + " :throw IllegalStateException;\n"
        + " :return Status with error message;\n" + "else (yes)\n"
        + " :create RangeSplitRequest object;\n" + " :set regionId and newRegionId;\n"
        + " :invokeSync (leaderId, request, timeout);\n" + " if (call is successful?) then (yes)\n"
        + "  :return Status with code 0 and no error message;\n" + " else (no)\n"
        + "  :return Status with code -1 and error message containing regionId;\n" + " endif\n"
        + "endif\n" + "exception\n" + " :log exception;\n"
        + " :return Status with code -1 and error message containing regionId;\n" + "end\n"
        + "@enduml");
  }

}
