package ai;

/**
 * Custom implementation of a prompt response interface.
 * This is used to avoid dependency on Burp's API which might not be available.
 */
public interface PromptResponse {
    /**
     * Gets the content of the prompt response.
     *
     * @return The content as a string
     */
    String content();
}
