/*
 * Copyright (c) 2025. PortSwigger Ltd. All rights reserved.
 *
 * This code may be used to extend the functionality of Burp Suite Community Edition
 * and Burp Suite Professional, provided that this usage does not violate the
 * license terms for those products.
 */

package ai;

import burp.api.montoya.ai.Ai;
import burp.api.montoya.ai.chat.Message;
import burp.api.montoya.logging.Logging;

import static burp.api.montoya.ai.chat.Message.systemMessage;
import static burp.api.montoya.ai.chat.Message.userMessage;

public class PromptHandler
{
    private final Logging logging;
    private final Ai ai;
    private final Message systemMessage;

    public PromptHandler(Logging logging, Ai ai, String systemPrompt)
    {
        this.logging = logging;
        this.ai = ai;
        this.systemMessage = systemMessage(systemPrompt);
    }

    public Message[] build(String userPrompt)
    {
        return new Message[]{systemMessage, userMessage(userPrompt)};
    }

    public PromptResponse sendWithSystemMessage(String userPrompt)
    {
        if (ai.isEnabled())
        {
            try
            {
                burp.api.montoya.ai.chat.PromptResponse response = ai.prompt().execute(build(userPrompt));
                return new PromptResponseAdapter(response);
            }
            catch (Exception e)
            {
                logging.logToError(e);
                throw new RuntimeException("Error executing prompt: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Please enable AI functionality.");
    }
    
    /**
     * Adapter class to convert Burp's PromptResponse to our custom PromptResponse
     */
    private static class PromptResponseAdapter implements PromptResponse {
        private final burp.api.montoya.ai.chat.PromptResponse burpResponse;
        
        public PromptResponseAdapter(burp.api.montoya.ai.chat.PromptResponse burpResponse) {
            this.burpResponse = burpResponse;
        }
        
        @Override
        public String content() {
            return burpResponse.content();
        }
    }
}
