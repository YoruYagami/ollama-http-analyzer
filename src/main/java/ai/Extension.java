/*
 * Copyright (c) 2025. PortSwigger Ltd. All rights reserved.
 *
 * This code may be used to extend the functionality of Burp Suite Community Edition
 * and Burp Suite Professional, provided that this usage does not violate the
 * license terms for those products.
 */

package ai;

import ai.ollama.OllamaClient;
import ai.ollama.OllamaConfig;
import ai.ollama.OllamaConfigPanel;
import ai.ollama.OllamaPromptHandler;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.EnhancedCapability;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;

import javax.swing.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static burp.api.montoya.EnhancedCapability.AI_FEATURES;
import static java.util.concurrent.Executors.newFixedThreadPool;

@SuppressWarnings("unused")
public class Extension implements BurpExtension {
    public static final String SYSTEM_MESSAGE = 
        "You are AI HTTP Analyzer, an advanced security analysis assistant integrated into Burp Suite. " +
        "Your role is to examine HTTP requests and responses for potential security vulnerabilities, " +
        "such as SQL injection, XSS, CSRF, and other threats. " +
        "Provide a focused technical analysis including: " +
        "1. Quick identification of detected vulnerabilities " +
        "2. Clear technical steps for exploitation " +
        "3. PoC examples and payloads where applicable " +
        "Keep responses concise and technical, focusing on exploitation methods. " + 
        "Avoid theoretical discussions or lengthy explanations. " +
        "Additionally, provide direct answers to any user questions or inputs related to security testing.";

    private OllamaConfig ollamaConfig;
    private OllamaClient ollamaClient;
    private BurpAITab.AIProvider aiProvider;

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("AI HTTP Analyzer");

        Logging logging = api.logging();
        
        // Initialize Ollama config - ensuring it reads from preferences
        this.ollamaConfig = new OllamaConfig();
        
        try {
            // Initialize Ollama directly (not trying to use Burp AI)
            initializeOllamaProvider(logging);
            
            // Enable Ollama 
            ollamaConfig.setEnabled(true);
            ollamaConfig.saveToPreferences();
            
            logging.logToOutput("Using Ollama with model: " + ollamaConfig.getModel());
        } catch (Exception e) {
            logging.logToError("Error initializing AI providers: " + e.getMessage());
            // Create a dummy provider that returns error messages
            this.aiProvider = userPrompt -> new SimplePromptResponse("AI functionality is not available. Error: " + e.getMessage());
        }

        // Create executor for async tasks
        ExecutorService executorService = newFixedThreadPool(5);
        
        // Initialize UI
        BurpAITab burpAITab = new BurpAITab(api.userInterface(), logging, aiProvider, executorService);

        // Register UI components
        api.userInterface().registerSuiteTab("AI HTTP Analyzer", burpAITab.getUiComponent());
        
        // Register settings UI in another tab
        OllamaConfigPanel configPanel = new OllamaConfigPanel(ollamaConfig, logging);
        api.userInterface().registerSuiteTab("Ollama Settings", configPanel);
        
        api.userInterface().registerContextMenuItemsProvider(new BurpAIContextMenu(burpAITab));
        api.extension().registerUnloadingHandler(executorService::shutdownNow);

        // Log custom success message with logToOutput
        logging.logToOutput("AI HTTP Analyzer extension loaded successfully.\nAuthor: ALPEREN ERGEL (@alpernae)\nVersion: 2025.1.0");
    }
    
    private void initializeOllamaProvider(Logging logging) {
        // Pass the ollamaConfig instance to the client instead of just the values
        this.ollamaClient = new OllamaClient(ollamaConfig, logging);
        
        OllamaPromptHandler ollamaPromptHandler = new OllamaPromptHandler(ollamaClient, logging, SYSTEM_MESSAGE);
        
        // Create an implementation of BurpAITab.AIProvider
        this.aiProvider = userPrompt -> ollamaPromptHandler.sendWithSystemMessage(userPrompt);
    }

    @Override
    public Set<EnhancedCapability> enhancedCapabilities() {
        return Set.of(AI_FEATURES);
    }
    
    /**
     * Simple implementation of PromptResponse for error messages.
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
