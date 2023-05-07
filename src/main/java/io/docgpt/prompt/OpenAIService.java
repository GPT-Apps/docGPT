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
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;

/**
 * @author masaimu
 * @version 2023-04-21 18:48:00
 */
public class OpenAIService {

  public List<ChatCompletionChoice> invoke(String prompt) {
    Properties properties = getLocalToken();
    String token = properties.getProperty("token");
    ObjectMapper mapper = defaultObjectMapper();
    OkHttpClient client = defaultClient(token, Duration.ofSeconds(120)).newBuilder().build();
    Retrofit retrofit = (new Retrofit.Builder()).baseUrl("https://openai.doc-gpt.net/")
        .client(client).addConverterFactory(JacksonConverterFactory.create(mapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
    OpenAiApi api = retrofit.create(OpenAiApi.class);
    OpenAiService service = new OpenAiService(api);

    ChatCompletionRequest chatCompletionRequest =
        ChatCompletionRequest.builder().model("gpt-3.5-turbo")
            .messages(Arrays.asList(
                new ChatMessage("system",
                    "You're a java coder, and you're good at writing interface documentation."),
                new ChatMessage("user", prompt)

            )).build();
    List<ChatCompletionChoice> choices =
        service.createChatCompletion(chatCompletionRequest).getChoices();
    return choices;
  }

  private Properties getLocalToken() {
    Properties props = new Properties();
    FileInputStream fis = null;
    String usrHome = System.getProperty("user.home");
    try {
      fis = new FileInputStream(usrHome + "/.config/docgpt/config.properties");
      props.load(fis);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return props;
  }
}
