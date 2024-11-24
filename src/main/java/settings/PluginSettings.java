package settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "PluginSettings", storages = @Storage("PluginSettings.xml"))
public class PluginSettings implements PersistentStateComponent<PluginSettings.State> {

    public static class State {
        public String apiKey = "";
        public int maxIterations = 5;
        public String LLM = "gpt-3.5-turbo";
    }

    private State myState = new State();

    public static PluginSettings getInstance() {
        return ServiceManager.getService(PluginSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public String getApiKey() {
        return myState.apiKey;
    }

    public void setApiKey(String apiKey) {
        myState.apiKey = apiKey;
    }

    public int getMaxIterations() {
        return myState.maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        myState.maxIterations = maxIterations;
    }

    public String getLLM() {
        return myState.LLM;
    }

    public void setLLM(String LLM) {
        myState.LLM = LLM;
    }
}