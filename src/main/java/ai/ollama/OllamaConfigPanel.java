package ai.ollama;

import burp.api.montoya.logging.Logging;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Configuration panel for Ollama settings.
 */
public class OllamaConfigPanel extends JPanel {
    private final OllamaConfig config;
    private final OllamaClient client;
    private final Logging logging;

    private JCheckBox enabledCheckbox;
    private JTextField baseUrlField;
    private JComboBox<String> modelComboBox;
    private JButton testConnectionButton;
    private JButton refreshModelsButton;
    private JLabel statusLabel;

    /**
     * Creates a new Ollama configuration panel.
     *
     * @param config  The Ollama configuration
     * @param logging Logging service
     */
    public OllamaConfigPanel(OllamaConfig config, Logging logging) {
        this.config = config;
        this.logging = logging;
        this.client = new OllamaClient(config.getBaseUrl(), config.getModel(), logging);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadConfig();
        updateComponentsState();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Ollama Integration Settings");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        
        JLabel descLabel = new JLabel("Use your local Ollama models instead of cloud-based AI.");
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Enabled checkbox
        enabledCheckbox = new JCheckBox("Enable Ollama Integration");
        enabledCheckbox.addActionListener(e -> updateComponentsState());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(enabledCheckbox, gbc);
        
        // Base URL field
        JLabel baseUrlLabel = new JLabel("Ollama API URL:");
        baseUrlField = new JTextField(30);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(baseUrlLabel, gbc);
        gbc.gridx = 1;
        panel.add(baseUrlField, gbc);
        
        // Model dropdown
        JLabel modelLabel = new JLabel("Model:");
        modelComboBox = new JComboBox<>();
        modelComboBox.setEditable(true);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(modelLabel, gbc);
        gbc.gridx = 1;
        panel.add(modelComboBox, gbc);
        
        // Status label
        statusLabel = new JLabel(" ");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(statusLabel, gbc);
        
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Test connection button
        testConnectionButton = new JButton("Test Connection");
        testConnectionButton.addActionListener(e -> testConnection());
        
        // Refresh models button
        refreshModelsButton = new JButton("Refresh Models");
        refreshModelsButton.addActionListener(e -> refreshModels());
        
        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveConfig());
        
        panel.add(testConnectionButton);
        panel.add(refreshModelsButton);
        panel.add(saveButton);
        
        return panel;
    }

    private void loadConfig() {
        enabledCheckbox.setSelected(config.isEnabled());
        baseUrlField.setText(config.getBaseUrl());
        
        // Add default model
        modelComboBox.addItem(config.getModel());
        
        // Try to fetch models
        refreshModels();
    }

    private void saveConfig() {
        config.setEnabled(enabledCheckbox.isSelected());
        config.setBaseUrl(baseUrlField.getText().trim());
        config.setModel(modelComboBox.getSelectedItem().toString());
        config.saveToPreferences();
        
        statusLabel.setText("Configuration saved successfully.");
        statusLabel.setForeground(Color.GREEN.darker());
    }

    private void testConnection() {
        statusLabel.setText("Testing connection...");
        statusLabel.setForeground(Color.BLACK);
        
        OllamaClient testClient = new OllamaClient(
                baseUrlField.getText().trim(),
                modelComboBox.getSelectedItem().toString(),
                logging
        );
        
        new Thread(() -> {
            boolean success = testClient.testConnection();
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    statusLabel.setText("Connection successful!");
                    statusLabel.setForeground(Color.GREEN.darker());
                } else {
                    statusLabel.setText("Connection failed. Check URL and ensure Ollama is running.");
                    statusLabel.setForeground(Color.RED);
                }
            });
        }).start();
    }

    private void refreshModels() {
        modelComboBox.removeAllItems();
        statusLabel.setText("Fetching models...");
        statusLabel.setForeground(Color.BLACK);
        
        OllamaClient testClient = new OllamaClient(
                baseUrlField.getText().trim(),
                config.getModel(),
                logging
        );
        
        new Thread(() -> {
            try {
                String[] models = testClient.listModels();
                SwingUtilities.invokeLater(() -> {
                    if (models.length > 0) {
                        Arrays.stream(models).forEach(modelComboBox::addItem);
                        modelComboBox.setSelectedItem(config.getModel());
                        statusLabel.setText("Found " + models.length + " models.");
                        statusLabel.setForeground(Color.GREEN.darker());
                    } else {
                        // Add default model if none found
                        modelComboBox.addItem(config.getModel());
                        statusLabel.setText("No models found. Make sure you've pulled models in Ollama.");
                        statusLabel.setForeground(Color.ORANGE.darker());
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    // Add default model on error
                    modelComboBox.addItem(config.getModel());
                    statusLabel.setText("Error fetching models: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                });
            }
        }).start();
    }

    private void updateComponentsState() {
        boolean enabled = enabledCheckbox.isSelected();
        
        baseUrlField.setEnabled(enabled);
        modelComboBox.setEnabled(enabled);
        testConnectionButton.setEnabled(enabled);
        refreshModelsButton.setEnabled(enabled);
    }
}
