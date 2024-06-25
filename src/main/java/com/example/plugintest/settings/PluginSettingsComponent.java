package com.example.plugintest.settings;


import javax.swing.*;
import java.awt.*;

public class PluginSettingsComponent {
    private JPanel mainPanel;
    private JTextField endpoint;
    private JTextField apiKey;
    private JRadioButton fewShotRadioButton;
    private JRadioButton oneShotRadioButton;
    private ButtonGroup modeGroup;

    public PluginSettingsComponent() {
        mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel endpointLabel = new JLabel("Endpoint:");
        endpoint = new JTextField();
        endpoint.setPreferredSize(new Dimension(300, 24));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        formPanel.add(endpointLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(endpoint, gbc);

        JLabel apiKeyLabel = new JLabel("API Key:");
        apiKey = new JTextField();
        apiKey.setPreferredSize(new Dimension(300, 24));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(apiKeyLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(apiKey, gbc);

        JLabel modeLabel = new JLabel("Mode:");
        fewShotRadioButton = new JRadioButton("Few-Shot");
        oneShotRadioButton = new JRadioButton("One-Shot");
        modeGroup = new ButtonGroup();
        modeGroup.add(fewShotRadioButton);
        modeGroup.add(oneShotRadioButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
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

        mainPanel.add(formPanel, BorderLayout.NORTH);

        fewShotRadioButton.setSelected(true);
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
}