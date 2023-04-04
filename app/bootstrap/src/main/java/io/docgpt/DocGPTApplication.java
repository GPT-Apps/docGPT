/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */

package io.docgpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DocGPTApplication {

  public static void main(String[] args) {
    SpringApplication.run(DocGPTApplication.class, args);
  }

}
