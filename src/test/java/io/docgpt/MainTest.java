/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author masaimu
 * @version 2023-04-23 20:50:00
 */
class MainTest {
  @Test
  void test_parse_method() {
    String usrHome = System.getProperty("user.home");
    String[] args = new String[] {"-d",
        usrHome + "/IdeaProjects/holoinsight-enterprise/opensource/server/home/", "-f",
        "AlarmHistoryDetailFacadeImpl", "-m", "queryCountTrend"};

    Main.main(args);
  }

  @Test
  void test_invoke() {
    String usrHome = System.getProperty("user.home");
    String[] args = new String[] {"-d",
        usrHome + "/IdeaProjects/holoinsight-enterprise/opensource/server/home/", "-f",
        "AlarmHistoryDetailFacadeImpl", "-m", "queryCountTrend", "-g"};

    Main.main(args);
  }
}
