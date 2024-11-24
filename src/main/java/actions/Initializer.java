package actions;

import com.intellij.openapi.vfs.VirtualFile;
import models.ExecutionResult;
import services.CodeExecutor;
import services.OpenAIService;
import services.CodeExecutorImprover;
import settings.PluginSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class Initializer extends AnAction {
    private final CodeExecutor codeExecutor;
    private CodeExecutorImprover codeExecutorImprover;

    public Initializer() {
        this.codeExecutor = new CodeExecutor();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("No active project found.", "Error");
            return;
        }

        // Get the latest API key from PluginSettings
        String apiKey = PluginSettings.getInstance().getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showErrorDialog("Please set your OpenAI API Key in the settings.", "API Key Missing");
            });
            throw new IllegalStateException("API Key is missing");
        }

        // Create OpenAIService with the current API key
        OpenAIService openAiService = new OpenAIService(apiKey);

        var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            Messages.showErrorDialog("No active editor found.", "Error");
            return;
        }

        // Get the current file's extension
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (currentFile == null) {
            Messages.showErrorDialog("No file associated with the current editor.", "Error");
            return;
        }

        String fileExtension = currentFile.getExtension();
        if (fileExtension == null) {
            Messages.showErrorDialog("The current file has no extension.", "Error");
            return;
        }

        boolean isKotlin = fileExtension.equalsIgnoreCase("kt");

        Document document = editor.getDocument();
        String userCode = document.getText();

        // Initialize CodeExecutorImprover with the latest openAiService instance
        this.codeExecutorImprover = new CodeExecutorImprover(codeExecutor, project, openAiService, isKotlin);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Executing and improving code", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("Preparing execution environment...");
                    indicator.setFraction(0.1);

                    ExecutionResult result = codeExecutorImprover.executeAndImproveCode(userCode, indicator);

                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!result.isSuccess()) {
                            showError("Final execution failed: " + result.getErrors());
                        }
                    });
                } catch (Exception ex) {
                    ApplicationManager.getApplication().invokeLater(() -> showError("Execution failed: " + ex.getMessage()));
                }
            }
        });
    }

    private void showError(String message) {
        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showErrorDialog(message, "Error")
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        var editor = project != null ?
                FileEditorManager.getInstance(project).getSelectedTextEditor() :
                null;
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }
}
