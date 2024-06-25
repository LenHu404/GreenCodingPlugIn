package com.example.plugintest.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PluginConfigurable implements Configurable {

    private PluginSettingsComponent settingsComponent;

    @Nls
    @Override
    public String getDisplayName() {
        return "My Plugin Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new PluginSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        PluginSettings settings = PluginSettings.getInstance();
        return !settingsComponent.getEndpoint().equals(settings.getEndpoint()) ||
                !settingsComponent.getApiKey().equals(settings.getApiKey()) ||
                settingsComponent.getMode() != settings.getMode();
    }

    @Override
    public void apply() {
        PluginSettings settings = PluginSettings.getInstance();
        settings.setEndpoint(settingsComponent.getEndpoint());
        settings.setApiKey(settingsComponent.getApiKey());
        settings.setMode(settingsComponent.getMode());
    }

    @Override
    public void reset() {
        PluginSettings settings = PluginSettings.getInstance();
        settingsComponent.setEndpoint(settings.getEndpoint());
        settingsComponent.setApiKey(settings.getApiKey());
        settingsComponent.setMode(settings.getMode());
    }
}