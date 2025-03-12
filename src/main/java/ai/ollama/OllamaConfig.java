package ai.ollama;

import java.util.prefs.Preferences;

/**
 * Configuration for the Ollama API.
 */
public class OllamaConfig {
    private static final String PREF_NODE = "ai.httpanal.ollama";
    private static final String PREF_ENABLED = "enabled";
    private static final String PREF_BASE_URL = "baseUrl";
    private static final String PREF_MODEL = "model";

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final String DEFAULT_MODEL = "llama3.2";

    private boolean enabled;
    private String baseUrl;
    private String model;

    /**
     * Creates a new Ollama configuration with default values.
     */
    public OllamaConfig() {
        this.enabled = false;
        this.baseUrl = DEFAULT_BASE_URL;
        this.model = DEFAULT_MODEL;
        loadFromPreferences();
    }

    /**
     * Creates a new Ollama configuration with the specified values.
     *
     * @param enabled Whether to use Ollama instead of Burp AI
     * @param baseUrl The base URL of the Ollama API
     * @param model   The model to use
     */
    public OllamaConfig(boolean enabled, String baseUrl, String model) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    /**
     * Loads the configuration from preferences.
     */
    public void loadFromPreferences() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        this.enabled = prefs.getBoolean(PREF_ENABLED, false);
        this.baseUrl = prefs.get(PREF_BASE_URL, DEFAULT_BASE_URL);
        this.model = prefs.get(PREF_MODEL, DEFAULT_MODEL);
    }

    /**
     * Saves the configuration to preferences.
     */
    public void saveToPreferences() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.putBoolean(PREF_ENABLED, enabled);
        prefs.put(PREF_BASE_URL, baseUrl);
        prefs.put(PREF_MODEL, model);
    }

    /**
     * Returns whether to use Ollama instead of Burp AI.
     *
     * @return True if Ollama should be used, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether to use Ollama instead of Burp AI.
     *
     * @param enabled True if Ollama should be used, false otherwise
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the base URL of the Ollama API.
     *
     * @return The base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL of the Ollama API.
     *
     * @param baseUrl The base URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the model to use.
     *
     * @return The model name
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model to use.
     *
     * @param model The model name
     */
    public void setModel(String model) {
        this.model = model;
    }
}
