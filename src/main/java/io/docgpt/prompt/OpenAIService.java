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
import io.docgpt.cmd.CommandFactory;
import io.docgpt.cmd.ConfigHandler;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static io.docgpt.cmd.ConfigHandler.OPENAI_API_KEY;

/**
 * @author masaimu
 * @version 2023-04-21 18:48:00
 */
public class OpenAIService {

  public List<ChatCompletionChoice> invoke(String prompt) {
    String token = getLocalToken();
    ObjectMapper mapper = defaultObjectMapper();
    OkHttpClient client = defaultClient(token, Duration.ofSeconds(120)).newBuilder().build();
    Retrofit retrofit = (new Retrofit.Builder()).baseUrl("https://openai.doc-gpt.net/")
        .client(client).addConverterFactory(JacksonConverterFactory.create(mapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
    OpenAiApi api = retrofit.create(OpenAiApi.class);
    OpenAiService service = new OpenAiService(api);

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo")
        .messages(Arrays.asList(new ChatMessage("system",
            "Act as a Java programmer, proficient in reading and analyzing code, capable of writing clear and detailed interface documentation based on the code, and able to create UML diagrams."),
            new ChatMessage("user", prompt)

        )).build();
    List<ChatCompletionChoice> choices =
        service.createChatCompletion(chatCompletionRequest).getChoices();
    return choices;
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
}
