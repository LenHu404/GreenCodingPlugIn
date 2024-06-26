package com.example.plugintest.settings;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PluginSettingsComponent {
    private JPanel mainPanel;
    private JTextField endpoint;
    private JTextField apiKey;
    private JRadioButton fewShotRadioButton;
    private JRadioButton oneShotRadioButton;
    private ButtonGroup modeGroup;
    private ComboBox<String> serviceSelector;
    private JPanel openAIConfigPanel;

    public PluginSettingsComponent() {
        mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel serviceSelectorLabel = new JLabel("Service:");
        serviceSelector = new ComboBox<>(new String[]{"OpenAI", "Ollama"});
        serviceSelector.setPreferredSize(new Dimension(300, 24));
        serviceSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateConfigPanelVisibility();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        formPanel.add(serviceSelectorLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(serviceSelector, gbc);

        openAIConfigPanel = new JPanel(new GridBagLayout());
        JLabel endpointLabel = new JLabel("Endpoint:");
        endpoint = new JTextField();
        endpoint.setPreferredSize(new Dimension(300, 24));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        openAIConfigPanel.add(endpointLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        openAIConfigPanel.add(endpoint, gbc);

        JLabel apiKeyLabel = new JLabel("API Key:");
        apiKey = new JTextField();
        apiKey.setPreferredSize(new Dimension(300, 24));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        openAIConfigPanel.add(apiKeyLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        openAIConfigPanel.add(apiKey, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        openAIConfigPanel.add(Box.createVerticalStrut(10), gbc);

        JLabel modeLabel = new JLabel("Mode:");
        fewShotRadioButton = new JRadioButton("Few-Shot");
        oneShotRadioButton = new JRadioButton("One-Shot");
        modeGroup = new ButtonGroup();
        modeGroup.add(fewShotRadioButton);
        modeGroup.add(oneShotRadioButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(modeLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(fewShotRadioButton);
        radioPanel.add(oneShotRadioButton);
        formPanel.add(radioPanel, gbc);

        fewShotRadioButton.setSelected(true);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(openAIConfigPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        updateConfigPanelVisibility();
    }

    private void updateConfigPanelVisibility() {
        boolean isOpenAISelected = "OpenAI".equals(serviceSelector.getSelectedItem());
        openAIConfigPanel.setVisible(isOpenAISelected);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public String getEndpoint() {
        return endpoint.getText();
    }

    public void setEndpoint(String endpointUrl) {
        endpoint.setText(endpointUrl);
    }

    public String getApiKey() {
        return apiKey.getText();
    }

    public void setApiKey(String apiKeyConfig) {
        apiKey.setText(apiKeyConfig);
    }

    public Mode getMode() {
        if (fewShotRadioButton.isSelected()) {
            return Mode.FEW_SHOT;
        } else if (oneShotRadioButton.isSelected()) {
            return Mode.ONE_SHOT;
        }
        return null;
    }

    public void setMode(Mode mode) {
        if (mode == Mode.FEW_SHOT) {
            fewShotRadioButton.setSelected(true);
        } else if (mode == Mode.ONE_SHOT) {
            oneShotRadioButton.setSelected(true);
        }
    }

    public String getService() {
        return (String) serviceSelector.getSelectedItem();
    }

    public void setService(String service) {
        serviceSelector.setSelectedItem(service);
        updateConfigPanelVisibility();
    }
}
