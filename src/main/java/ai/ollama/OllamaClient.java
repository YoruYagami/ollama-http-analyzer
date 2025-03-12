package ai.ollama;

import burp.api.montoya.logging.Logging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client for interacting with the Ollama API.
 */
public class OllamaClient {
    private final String baseUrl;
    private final OllamaConfig config;
    private final Logging logging;

    /**
     * Creates a new Ollama client.
     *
     * @param baseUrl The base URL of the Ollama API (e.g., "http://localhost:11434")
     * @param model   The default model to use (e.g., "llama3.2")
     * @param logging Logging service
     */
    public OllamaClient(String baseUrl, String model, Logging logging) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.config = new OllamaConfig(true, baseUrl, model);
        this.config.loadFromPreferences(); // Load the latest settings
        this.logging = logging;
    }
    
    /**
     * Creates a new Ollama client with direct config reference.
     *
     * @param config  The Ollama configuration
     * @param logging Logging service
     */
    public OllamaClient(OllamaConfig config, Logging logging) {
        this.baseUrl = config.getBaseUrl().endsWith("/") 
            ? config.getBaseUrl().substring(0, config.getBaseUrl().length() - 1) 
            : config.getBaseUrl();
        this.config = config;
        this.logging = logging;
    }

    /**
     * Gets the current model from configuration.
     * 
     * @return The current model name
     */
    private String getCurrentModel() {
        // Reload from preferences to get latest settings
        config.loadFromPreferences();
        return config.getModel();
    }

    /**
     * Generates a completion for the given prompt.
     *
     * @param prompt The prompt to generate a completion for
     * @param system The system message
     * @return The generated completion
     * @throws IOException If an error occurs during the API call
     */
    public String generateCompletion(String prompt, String system) throws IOException {
        URL url = new URL(baseUrl + "/api/generate");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Use getCurrentModel() to get the latest model from config
        String currentModel = getCurrentModel();
        
        // Create request body
        String requestBody = String.format(
                "{\"model\":\"%s\",\"prompt\":\"%s\",\"system\":\"%s\",\"stream\":false}",
                currentModel,
                escapeJson(prompt),
                escapeJson(system)
        );

        logging.logToOutput("Sending request to Ollama API: " + url + " with model: " + currentModel);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                String jsonResponse = scanner.hasNext() ? scanner.next() : "";
                
                // Simple JSON extraction of the response field
                // In a real implementation, you would use a proper JSON library
                if (jsonResponse.contains("\"response\":")) {
                    String response = jsonResponse.split("\"response\":")[1].split(",")[0];
                    // Remove quotes if present
                    if (response.startsWith("\"") && response.endsWith("\"")) {
                        response = response.substring(1, response.length() - 1);
                    }
                    
                    return response;
                }
                
                logging.logToOutput("Received response from Ollama API: " + jsonResponse);
                return "Error parsing response: " + jsonResponse;
            }
        } else {
            try (Scanner scanner = new Scanner(connection.getErrorStream(), StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                String errorResponse = scanner.hasNext() ? scanner.next() : "";
                logging.logToError("Ollama API error: " + errorResponse);
                return "Error from Ollama API: " + errorResponse;
            }
        }
    }

    /**
     * Generates a chat completion for the given messages.
     *
     * @param messages List of messages in the format: [{"role": "system", "content": "..."}, {"role": "user", "content": "..."}]
     * @return The generated chat completion
     * @throws IOException If an error occurs during the API call
     */
    public String generateChatCompletion(String messages) throws IOException {
        URL url = new URL(baseUrl + "/api/chat");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Use getCurrentModel() to get the latest model from config
        String currentModel = getCurrentModel();
        
        // Create request body
        String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":%s,\"stream\":false}",
                currentModel,
                messages
        );

        logging.logToOutput("Sending chat request to Ollama API: " + url + " with model: " + currentModel);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                String jsonResponse = scanner.hasNext() ? scanner.next() : "";
                
                // Simple JSON extraction of the content field
                if (jsonResponse.contains("\"content\":")) {
                    String content = jsonResponse.split("\"content\":")[1].split(",")[0];
                    // Remove quotes if present
                    if (content.startsWith("\"") && content.endsWith("\"")) {
                        content = content.substring(1, content.length() - 1);
                    }
                    
                    return content;
                }
                
                logging.logToOutput("Received chat response from Ollama API: " + jsonResponse);
                return "Error parsing response: " + jsonResponse;
            }
        } else {
            try (Scanner scanner = new Scanner(connection.getErrorStream(), StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                String errorResponse = scanner.hasNext() ? scanner.next() : "";
                logging.logToError("Ollama API error: " + errorResponse);
                return "Error from Ollama API: " + errorResponse;
            }
        }
    }

    /**
     * Tests the connection to the Ollama API.
     *
     * @return True if the connection is successful, false otherwise
     */
    public boolean testConnection() {
        try {
            URL url = new URL(baseUrl + "/api/version");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            logging.logToError("Error testing connection to Ollama API: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lists the available models from the Ollama API.
     *
     * @return Array of model names
     */
    public String[] listModels() {
        try {
            URL url = new URL(baseUrl + "/api/tags");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                    scanner.useDelimiter("\\A");
                    String jsonResponse = scanner.hasNext() ? scanner.next() : "";
                    
                    // Simple extraction of model names
                    // In a real implementation, you would use a proper JSON library
                    if (jsonResponse.contains("\"models\":")) {
                        String[] modelEntries = jsonResponse.split("\"name\":\"");
                        String[] models = new String[modelEntries.length - 1];
                        
                        for (int i = 1; i < modelEntries.length; i++) {
                            models[i - 1] = modelEntries[i].split("\"")[0];
                        }
                        
                        return models;
                    }
                    
                    return new String[0];
                }
            }
            
            return new String[0];
        } catch (Exception e) {
            logging.logToError("Error listing models from Ollama API: " + e.getMessage());
            return new String[0];
        }
    }
    
    /**
     * Escapes special characters in a string for inclusion in a JSON string.
     *
     * @param input The string to escape
     * @return The escaped string
     */
    public String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
