package ai.ollama;

import ai.PromptResponse;
import burp.api.montoya.logging.Logging;

import java.io.IOException;

/**
 * Handler for sending prompts to Ollama API.
 */
public class OllamaPromptHandler {
    private final OllamaClient ollamaClient;
    private final Logging logging;
    private final String systemPrompt;

    /**
     * Creates a new Ollama prompt handler.
     *
     * @param ollamaClient The Ollama client
     * @param logging      Logging service
     * @param systemPrompt The system prompt
     */
    public OllamaPromptHandler(OllamaClient ollamaClient, Logging logging, String systemPrompt) {
        this.ollamaClient = ollamaClient;
        this.logging = logging;
        this.systemPrompt = systemPrompt;
    }

    /**
     * Sends a prompt to the Ollama API.
     *
     * @param userPrompt The user prompt
     * @return The response from the Ollama API
     */
    public PromptResponse sendWithSystemMessage(String userPrompt) {
        try {
            // Build JSON for chat API
            String messages = String.format(
                    "[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}]",
                    ollamaClient.escapeJson(systemPrompt),
                    ollamaClient.escapeJson(userPrompt)
            );
            
            // Try to use the chat API first (better for models that support it)
            String response = ollamaClient.generateChatCompletion(messages);
            return new SimplePromptResponse(response);
        } catch (IOException e) {
            try {
                // Fall back to the generate API if chat fails
                String response = ollamaClient.generateCompletion(userPrompt, systemPrompt);
                return new SimplePromptResponse(response);
            } catch (IOException ex) {
                logging.logToError("Error sending prompt to Ollama API: " + ex.getMessage());
                return new SimplePromptResponse("Error: " + ex.getMessage());
            }
        }
    }

    /**
     * Simple implementation of PromptResponse for Ollama.
     */
    private static class SimplePromptResponse implements PromptResponse {
        private final String content;

        public SimplePromptResponse(String content) {
            this.content = content;
        }

        @Override
        public String content() {
            return content;
        }
    }
}
