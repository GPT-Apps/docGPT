/*
 * Copyright 2023 DocGPT Project Authors. Licensed under Apache-2.0.
 */
package io.docgpt.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatChoice;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import io.docgpt.cmd.CmdHandler;
import io.docgpt.cmd.CommandFactory;
import io.docgpt.cmd.ConfigHandler;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static io.docgpt.cmd.ConfigHandler.OPENAI_API_KEY;

/**
 * @author masaimu
 * @version 2023-04-21 18:48:00
 */
public class OpenAIService {

  public List<ChatChoice> invoke(String prompt) {
    Message sysMsg = Message.builder().role(Message.Role.SYSTEM).content(
        "Act as a Java programmer, proficient in reading and analyzing code, capable of writing clear and detailed interface documentation based on the code, and able to create UML diagrams.")
        .build();
    Message userMsg = Message.builder().role(Message.Role.USER).content(prompt).build();
    ChatCompletion chatCompletion =
        ChatCompletion.builder().model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
            .temperature(0.2).maxTokens(2048).messages(Arrays.asList(sysMsg, userMsg)).build();
    ChatCompletionResponse chatCompletionResponse = client.chatCompletion(chatCompletion);

    return chatCompletionResponse.getChoices();
  }

  private String getLocalToken() {
    String token = System.getenv(OPENAI_API_KEY);
    if (StringUtils.isEmpty(token)) {
      Properties props =
          ((ConfigHandler) CommandFactory.getCmdHandler(ConfigHandler.CONFIG)).getLocalProp();
      token = props.getProperty(OPENAI_API_KEY);
    }
    return token;
  }

  private OpenAiStreamClient streamClient;
  private OpenAiClient client;

  public OpenAIService() {
    String token = getLocalToken();
    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    OkHttpClient okHttpClient = new OkHttpClient //
        .Builder() //
            .addInterceptor(httpLoggingInterceptor) //
            .connectTimeout(60, TimeUnit.SECONDS) //
            .writeTimeout(60, TimeUnit.SECONDS) //
            .readTimeout(60, TimeUnit.SECONDS) //
            .build();
    streamClient = OpenAiStreamClient.builder() //
        .apiKey(Collections.singletonList(token)) //
        .okHttpClient(okHttpClient) //
        .apiHost("https://openai.doc-gpt.net/") //
        .build();
    client = OpenAiClient.builder() //
        .apiKey(Collections.singletonList(token)) //
        .okHttpClient(okHttpClient) //
        .apiHost("https://openai.doc-gpt.net/") //
        .build();
  }

  public void streamInvoke(CmdHandler cmdHandler, String prompt, AtomicBoolean wait) {
    Message sysMsg = Message.builder().role(Message.Role.SYSTEM).content(
        "Act as a Java programmer, proficient in reading and analyzing code, capable of writing clear and detailed interface documentation based on the code, and able to create UML diagrams.")
        .build();
    Message userMsg = Message.builder().role(Message.Role.USER).content(prompt).build();
    ChatCompletion chatCompletion = ChatCompletion.builder()
        .model(ChatCompletion.Model.GPT_3_5_TURBO.getName()).temperature(0.2).maxTokens(2048)
        .messages(Arrays.asList(sysMsg, userMsg)).stream(true).build();
    DefaultEventSourceListener eventSourceListener =
        new DefaultEventSourceListener(cmdHandler, wait);
    streamClient.streamChatCompletion(chatCompletion, eventSourceListener);
  }
}
