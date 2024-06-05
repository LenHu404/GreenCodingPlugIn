package com.example.plugintest;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class KIConnect {


    public static String getAIAnswer(String codeInput) {
        String endpoint = "https://greencoding-ai.openai.azure.com/";
        String azureOpenaiKey = "eb7709e19cb14584862d1a78fb1122ba";
        String deploymentOrModelId = "gpt-4";
        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();
        System.out.println(client);

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM).setContent("You are a green coding expert. When provided with a piece of code, your task is to:\n" +
                "\n" +
                "Analyze the code for potential improvements or corrections.\n" +
                "Provide a very short explanation of what was corrected or improved, surrounded by #?#.\n" +
                "Present the corrected code, surrounded by -!-.\n" +
                "Name the lines where the changes where made, each line must be named separately, separated by a comma, surrounded by $!$.\n" +
                "Use the following format:\n" +
                "\n" +
                "\n" +
                "#?# Explanation of the corrections or improvements #?#\n" +
                "\n" +
                "-!- Corrected code -!-\n" +
                "\n" +
                "$!$ line numbers where corrections were made $!$\n" +
                "\n" +
                "\n" +
                "Here is an example input and output to demonstrate the format:\n" +
                "\n" +
                "Input:\n" +
                "\n" +
                "public void add_numbers(int a, int b) {\n" +
                "        int result = a + b;\n" +
                "        System.out.println(result);\n" +
                "}\n" +
                "Output:\n" +
                "\n" +
                "\n" +
                "#?# Improved the function to return the result instead of printing it for better reusability and testability #?#\n" +
                "\n" +
                "-!- \n" +
                "public int add_numbers(int a, int b) {\n" +
                "        return a + b;\n" +
                "}\n" +
                "-!-\n" +
                "\n" +
                "$!$ \n" +
                "2,3\n" +
                "$!$\n" +
                "\n" +
                "Please apply this format to all code corrections."));
        chatMessages.add(new ChatMessage(ChatRole.USER).setContent(codeInput));

        final ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
        options.setMaxTokens(1000);
        options.setTemperature(0.70);
        options.setFrequencyPenalty(0.0);
        options.setPresencePenalty(0.0);
        options.setTopP(0.95);
        options.setStop(Arrays.asList());
        options.setStream(false);
        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, options);

        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatMessage message = choice.getMessage();
            System.out.println("Message from " + message.getRole() + ":");
            System.out.println(message.getContent());
            return message.getContent();
        }
        return "Missed answer";
    }
    /*public static void main(String[] args) {
        String endpoint = "https://greencoding-ai.openai.azure.com/";
        String azureOpenaiKey = "eb7709e19cb14584862d1a78fb1122ba";
        String deploymentOrModelId = "gpt-4";

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM).setContent("You are a green coding expert. When provided with a piece of code, your task is to:\n" +
                "\n" +
                "Analyze the code for potential improvements or corrections.\n" +
                "Provide a short explanation of what was corrected or improved, surrounded by #?#.\n" +
                "Present the corrected code, surrounded by -!-.\n" +
                "Highlight the specific lines where changes were made, surrounded by $!$.\n" +
                "Use the following format:\n" +
                "\n" +
                "\n" +
                "#?# Explanation of the corrections or improvements #?#\n" +
                "\n" +
                "-!- Corrected code -!-\n" +
                "\n" +
                "$!$ Specific lines where corrections were made $!$\n" +
                "\n" +
                "\n" +
                "Here is an example input and output to demonstrate the format:\n" +
                "\n" +
                "Input:\n" +
                "\n" +
                "public void add_numbers(int a, int b) {\n" +
                "        int result = a + b;\n" +
                "        System.out.println(result);\n" +
                "}\n" +
                "Output:\n" +
                "\n" +
                "\n" +
                "#?# Improved the function to return the result instead of printing it for better reusability and testability #?#\n" +
                "\n" +
                "-!- \n" +
                "public int add_numbers(int a, int b) {\n" +
                "        return a + b;\n" +
                "}\n" +
                "-!-\n" +
                "\n" +
                "$!$ \n" +
                "2\n" +
                "$!$\n" +
                "\n" +
                "Please apply this format to all code corrections."));
        chatMessages.add(new ChatMessage(ChatRole.USER).setContent(""));

        final ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
        options.setMaxTokens(1000);
        options.setTemperature(0.70);
        options.setFrequencyPenalty(0.0);
        options.setPresencePenalty(0.0);
        options.setTopP(0.95);
        options.setStop(Arrays.asList());
        ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, options);

        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatMessage message = choice.getMessage();
            System.out.println("Message:");
            System.out.println(message.getContent());
        }
    }*/
}
