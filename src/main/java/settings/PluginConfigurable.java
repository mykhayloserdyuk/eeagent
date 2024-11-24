package settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PluginConfigurable implements Configurable {

    private JPanel settingsPanel;
    private JTextField apiKeyField;
    private JSlider iterationsSlider;
    private JComboBox<String> modeComboBox;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "EEAgent";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        apiKeyField = new JTextField(PluginSettings.getInstance().getApiKey(), 20);

        iterationsSlider = new JSlider(1, 10, PluginSettings.getInstance().getMaxIterations());
        iterationsSlider.setMajorTickSpacing(1);
        iterationsSlider.setPaintTicks(true);
        iterationsSlider.setPaintLabels(true);

        String[] modes = {"gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo"};
        modeComboBox = new JComboBox<>(modes);
        modeComboBox.setSelectedItem(PluginSettings.getInstance().getLLM());

        settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("OpenAI API Key:"), gbc);

        gbc.gridx = 1;
        settingsPanel.add(apiKeyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        settingsPanel.add(new JLabel("Max Iterations:"), gbc);

        gbc.gridx = 1;
        settingsPanel.add(iterationsSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        settingsPanel.add(new JLabel("LLM:"), gbc);

        gbc.gridx = 1;
        settingsPanel.add(modeComboBox, gbc);

        return settingsPanel;
    }

    @Override
    public boolean isModified() {
        return !apiKeyField.getText().equals(PluginSettings.getInstance().getApiKey()) ||
                iterationsSlider.getValue() != PluginSettings.getInstance().getMaxIterations() ||
                !modeComboBox.getSelectedItem().equals(PluginSettings.getInstance().getLLM());
    }

    @Override
    public void apply() {
        PluginSettings.getInstance().setApiKey(apiKeyField.getText());
        PluginSettings.getInstance().setMaxIterations(iterationsSlider.getValue());
        PluginSettings.getInstance().setLLM((String) modeComboBox.getSelectedItem());
    }

    @Override
    public void reset() {
        apiKeyField.setText(PluginSettings.getInstance().getApiKey());
        iterationsSlider.setValue(PluginSettings.getInstance().getMaxIterations());
        modeComboBox.setSelectedItem(PluginSettings.getInstance().getLLM());
    }
}