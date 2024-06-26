package com.example.plugintest.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.example.plugintest.PluginSettings",
        storages = {@Storage("PluginSettings.xml")}
)
public class PluginSettings implements PersistentStateComponent<PluginSettings.State> {

    public static class State {
        public String service = "OpenAI";
        public String endpoint = "";
        public String apiKey = "";
        public String mode = Mode.FEW_SHOT.getValue();
    }

    private State myState = new State();

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    @NotNull
    public static PluginSettings getInstance() {
        return ServiceManager.getService(PluginSettings.class);
    }

    public String getService() {
        return myState.service;
    }

    public void setService(String service) {
        myState.service = service;
    }

    public String getEndpoint() {
        return myState.endpoint;
    }

    public void setEndpoint(String endpointUrl) {
        myState.endpoint = endpointUrl;
    }

    public String getApiKey() {
        return myState.apiKey;
    }

    public void setApiKey(String apiKeyConfig) {
        myState.apiKey = apiKeyConfig;
    }

    public Mode getMode() {
        return Mode.fromString(myState.mode);
    }

    public void setMode(Mode mode) {
        myState.mode = mode.getValue();
    }
}
