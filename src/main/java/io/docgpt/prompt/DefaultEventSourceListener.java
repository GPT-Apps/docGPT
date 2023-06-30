/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import io.docgpt.cmd.CmdHandler;
import io.docgpt.parse.JsonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author masaimu
 * @version 2023-06-27 01:12:00
 */
@Slf4j
public class DefaultEventSourceListener extends EventSourceListener {

  private CmdHandler cmdHandler;
  private AtomicBoolean wait;

  public DefaultEventSourceListener(CmdHandler cmdHandler, AtomicBoolean wait) {
    this.cmdHandler = cmdHandler;
    this.wait = wait;
  }

  @Override
  public void onOpen(EventSource eventSource, Response response) {
    log.info("OpenAI connecting...");
  }

  @Override
  public void onEvent(EventSource eventSource, String id, String type, String data) {
    if (data.equals("[DONE]")) {
      this.cmdHandler.setInfoSignal(" ");
      this.wait.set(false);
      return;
    }
    try {
      Map<String, Object> result = JsonUtil.toMap(data);
      if (result == null || result.size() == 0) {
        return;
      }
      List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
      if (CollectionUtils.isEmpty(choices)) {
        return;
      }
      Map<String, Object> choice = choices.get(0);
      Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
      if (delta == null || delta.size() == 0) {
        return;
      }
      this.cmdHandler.setStreamInfoSignal((String) delta.get("content"));
    } catch (Exception e) {
      log.error("Fail in onEvent {}", data, e);
      this.wait.set(false);
    }
  }

  @Override
  public void onClosed(EventSource eventSource) {
    log.info("OpenAI close connecting.");
  }

  @SneakyThrows
  @Override
  public void onFailure(EventSource eventSource, Throwable t, Response response) {
    if (Objects.isNull(response)) {
      log.error("OpenAI  sse connection exception:{}", t);
      eventSource.cancel();
      return;
    }
    ResponseBody body = response.body();
    if (Objects.nonNull(body)) {
      log.error("OpenAI  sse connection exception data：{}", body.string(), t);
    } else {
      log.error("OpenAI  sse connection exception data：{}", response, t);
    }
    eventSource.cancel();
  }
}
