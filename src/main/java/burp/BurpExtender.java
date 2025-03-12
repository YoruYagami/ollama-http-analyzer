package burp;

import ai.BurpAIContextMenu;
import ai.BurpAITab;
import ai.PromptResponse;
import ai.ollama.OllamaClient;
import ai.ollama.OllamaConfig;
import ai.ollama.OllamaConfigPanel;
import ai.ollama.OllamaPromptHandler;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the entry point class for the extension.
 * It implements the BurpExtension interface and initializes the Ollama integration.
 */
public class BurpExtender implements BurpExtension {
    private static final String SYSTEM_MESSAGE = 
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

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("AI HTTP Analyzer");
        
        Logging logging = api.logging();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        
        try {
            // Initialize Ollama config - ensuring it reads from preferences
            OllamaConfig ollamaConfig = new OllamaConfig();
            ollamaConfig.setEnabled(true);
            ollamaConfig.saveToPreferences();
            
            // Initialize Ollama client and pass the config instance
            OllamaClient ollamaClient = new OllamaClient(ollamaConfig, logging);
            
            // Initialize prompt handler
            OllamaPromptHandler ollamaPromptHandler = new OllamaPromptHandler(
                ollamaClient, 
                logging, 
                SYSTEM_MESSAGE
            );
            
            // Create AI Provider
            BurpAITab.AIProvider aiProvider = new BurpAITab.AIProvider() {
                @Override
                public PromptResponse sendWithSystemMessage(String userPrompt) {
                    return ollamaPromptHandler.sendWithSystemMessage(userPrompt);
                }
            };
            
            logging.logToOutput("Using Ollama with model: " + ollamaConfig.getModel());
                
            // Initialize UI components
            BurpAITab burpAITab = new BurpAITab(api.userInterface(), logging, aiProvider, executorService);
            
            // Register UI components
            api.userInterface().registerSuiteTab("AI HTTP Analyzer", burpAITab.getUiComponent());
            
            // Register settings UI
            OllamaConfigPanel configPanel = new OllamaConfigPanel(ollamaConfig, logging);
            api.userInterface().registerSuiteTab("Ollama Settings", configPanel);
            
            api.userInterface().registerContextMenuItemsProvider(new BurpAIContextMenu(burpAITab));
            
            // Register clean-up handler
            api.extension().registerUnloadingHandler(executorService::shutdownNow);
            
            // Log success message
            logging.logToOutput("AI HTTP Analyzer extension loaded successfully.\nAuthor: ALPEREN ERGEL (@alpernae)\nVersion: 2025.1.0");
            
        } catch (Exception e) {
            logging.logToError("Error initializing AI HTTP Analyzer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
