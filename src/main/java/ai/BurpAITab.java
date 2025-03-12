package ai;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class BurpAITab {
    private final JPanel mainPanel;
    private final JTabbedPane tabbedPane;
    private final Map<Component, HttpRequestResponse> tabRequests;
    private int tabCounter = 1;
    private final Logging logging;
    private final AIProvider aiProvider;
    private final ExecutorService executorService;
    private final UserInterface userInterface;

    public BurpAITab(UserInterface userInterface, Logging logging, AIProvider aiProvider, ExecutorService executorService) {
        this.userInterface = userInterface;
        this.logging = logging;
        this.aiProvider = aiProvider;
        this.executorService = executorService;

        tabRequests = new HashMap<>();

        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        // Add initial empty tab
        createNewTab("Default", null);
        
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    public void sendNewRequestToTab(HttpRequestResponse requestResponse) {
        String tabTitle = "Request " + tabCounter++;
        createNewTab(tabTitle, requestResponse);
    }

    private void createNewTab(String title, HttpRequestResponse requestResponse) {
        Component tabContent = createTabContent(requestResponse);
        tabbedPane.addTab(title, tabContent);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, createTabComponent(title));

        if (requestResponse != null) {
            logging.logToOutput("Creating new tab with request: " + requestResponse.request().toString());
            tabRequests.put(tabContent, requestResponse);
        }

        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    private Component createTabContent(HttpRequestResponse requestResponse) {
        return new BurpAiRequestTab(logging, userInterface, executorService, aiProvider, requestResponse);
    }

    private Component createTabComponent(String title) {
        JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        tabComponent.setOpaque(false);

        // Create label instead of text field
        JLabel titleLabel = new JLabel(title);
        titleLabel.setPreferredSize(new Dimension(70, 30));

        // Create text field for editing (initially invisible)
        JTextField titleField = new JTextField(title);
        titleField.setPreferredSize(new Dimension(100, 20));
        titleField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        titleField.setVisible(false);

        // Add mouse listener to the entire tab component for selection
        tabComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Select this tab when clicked
                int index = tabbedPane.indexOfTabComponent(tabComponent);
                if (index != -1) {
                    tabbedPane.setSelectedIndex(index);
                }
            }
        });

        // Handle double click on label
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    titleLabel.setVisible(false);
                    titleField.setText(titleLabel.getText());
                    titleField.setVisible(true);
                    titleField.requestFocus();
                } else if (e.getClickCount() == 1) {
                    // Select this tab on single click too
                    int index = tabbedPane.indexOfTabComponent(tabComponent);
                    if (index != -1) {
                        tabbedPane.setSelectedIndex(index);
                    }
                }
            }
        });

        // Handle editing complete
        titleField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finishEditing(titleLabel, titleField);
            }
        });

        titleField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    finishEditing(titleLabel, titleField);
                }
            }
        });

        // Improved close button styling
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font(closeButton.getFont().getName(), Font.PLAIN, 12));
        closeButton.setPreferredSize(new Dimension(12, 12));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> {
            int index = tabbedPane.indexOfTabComponent(tabComponent);
            if (index != -1 && tabbedPane.getTabCount() > 1) { // Prevent closing last tab
                Component content = tabbedPane.getComponentAt(index);
                tabRequests.remove(content);
                tabbedPane.remove(index);
            }
        });

        tabComponent.add(titleLabel);
        tabComponent.add(titleField);
        tabComponent.add(closeButton);
        return tabComponent;
    }

    private void finishEditing(JLabel label, JTextField textField) {
        label.setText(textField.getText());
        label.setVisible(true);
        textField.setVisible(false);
    }

    public Component getUiComponent() {
        return mainPanel;
    }
    
    /**
     * Interface for an AI provider that can send prompts.
     */
    public interface AIProvider {
        /**
         * Sends a prompt to the AI provider.
         *
         * @param userPrompt The user prompt
         * @return The response from the AI provider
         */
        PromptResponse sendWithSystemMessage(String userPrompt);
    }
}
