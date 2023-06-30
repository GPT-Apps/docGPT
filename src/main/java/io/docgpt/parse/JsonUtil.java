/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.parse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

/**
 * @author masaimu
 * @version 2023-06-02 12:43:00
 */
public class JsonUtil {

  /** Constant <code>gs</code> */
  public static ThreadLocal<Gson> gs = new ThreadLocal<Gson>();

  /**
   * <p>
   * get.
   * </p>
   */
  public static Gson get() {
    Gson gson = gs.get();
    if (gson == null) {
      gson = new GsonBuilder().disableHtmlEscaping().create();
      gs.set(gson);
    }
    return gson;
  }

  /**
   * <p>
   * toJson.
   * </p>
   */
  public static String toJson(Object obj) {
    return get().toJson(obj);
  }

  /**
   * <p>
   * fromJson.
   * </p>
   */
  public static <T> T fromJson(String json, Type type) {
    return get().fromJson(json, type);
  }

  public static Map<String, Object> toMap(String json) {
    Type t = new TypeToken<Map<String, Object>>() {}.getType();
    Map<String, Object> whereMap = fromJson(json, t);
    if (whereMap == null) {
      return Collections.emptyMap();
    }
    return whereMap;
  }

  public static boolean isValidJson(String jsonString) {
    try {
      JsonParser parser = new JsonParser();
      parser.parse(jsonString);
      return true;
    } catch (JsonSyntaxException jse) {
      return false;
    }
  }
}
