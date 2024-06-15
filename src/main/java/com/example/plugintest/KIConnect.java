package com.example.plugintest;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KIConnect {


    public static CompletableFuture<String> getAIAnswerAsync(String codeInput) {
        String endpoint = "https://greencoding-ai.openai.azure.com/";
        String azureOpenaiKey = "eb7709e19cb14584862d1a78fb1122ba";
        String deploymentOrModelId = "gpt-4";
        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildAsyncClient();
        System.out.println(client);

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM).setContent("""
                You are a green coding expert. When provided with a piece of code, your task is to:

                Analyze the code for potential improvements or corrections.
                Provide a very short explanation of what was corrected or improved, surrounded by #?#.
                Present the corrected code, surrounded by -!-.
                Name the lines where the changes where made, each line must be named separately, separated by a comma, surrounded by $!$.
                Use the following format:


                #?# Explanation of the corrections or improvements #?#

                -!- Corrected code -!-

                $!$ line numbers where corrections were made $!$


                Here is an example input and output to demonstrate the format:

                Input:

                public void add_numbers(int a, int b) {
                        int result = a + b;
                        System.out.println(result);
                }
                Output:


                #?# Improved the function to return the result instead of printing it for better reusability and testability #?#

                -!-\s
                public int add_numbers(int a, int b) {
                        return a + b;
                }
                -!-

                $!$\s
                2,3
                $!$

                Please apply this format to all code corrections."""));
        chatMessages.add(new ChatMessage(ChatRole.USER).setContent(codeInput));

        final ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
        options.setMaxTokens(1000);
        options.setTemperature(0.70);
        options.setFrequencyPenalty(0.0);
        options.setPresencePenalty(0.0);
        options.setTopP(0.95);
        options.setStop(List.of());
        options.setStream(false);
        return client.getChatCompletions(deploymentOrModelId, options).toFuture().thenApply(chatCompletions -> {
            for (ChatChoice choice : chatCompletions.getChoices()) {
                ChatMessage message = choice.getMessage();
                System.out.println("Message from " + message.getRole() + ":");
                System.out.println(message.getContent());
                return message.getContent();
            }
            return "Missed Answer";
        });
    }
}
