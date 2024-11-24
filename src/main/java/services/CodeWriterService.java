package services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CodeWriterService {
    private final Project project;

    public CodeWriterService(@NotNull Project project) {
        this.project = project;
    }

    public void updateCodeWithImprovement(@NotNull String improvedCode) {
        ApplicationManager.getApplication().invokeLater(() -> {
            var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (editor == null) {
                showError();
                return;
            }

            Document document = editor.getDocument();
            String originalCode = document.getText();

            if (originalCode.equals(improvedCode)) {
                showSuccess("No improvements needed - code is already correct!");
                return;
            }

            showPreviewInToolWindow(originalCode, improvedCode);
        });
    }

    private void showPreviewInToolWindow(String originalCode, String improvedCode) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("RRAgent");
        if (toolWindow == null) {
            Icon customIcon = IconLoader.getIcon("/icons/debug.svg", getClass());

            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(
                    RegisterToolWindowTask.closable(
                            "RRAgent",
                            customIcon,
                            ToolWindowAnchor.LEFT
                    )
            );
        }

        final ToolWindow finalToolWindow = toolWindow;

        JPanel panel = new JPanel(new BorderLayout());
        
        Document improvedDocument = EditorFactory.getInstance().createDocument(improvedCode);

        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(improvedDocument, project, FileTypeManager.getInstance().getFileTypeByExtension("java"), false);

        EditorSettings editorSettings = editor.getSettings();
        editorSettings.setLineNumbersShown(true);
        editorSettings.setFoldingOutlineShown(true);
        editorSettings.setLineMarkerAreaShown(true);
        editorSettings.setIndentGuidesShown(true);

        highlightDifferences(editor, originalCode, improvedCode);

        panel.add(editor.getComponent(), BorderLayout.CENTER);

        JButton discardButton = new JButton("Discard Changes");
        discardButton.setBorderPainted(false);
        discardButton.setOpaque(false);
        discardButton.setBackground(Color.white);
        discardButton.addActionListener( e -> {
                finalToolWindow.hide(null);
                finalToolWindow.getContentManager().removeAllContents(true);
                finalToolWindow.remove();
                showSuccess("Changes discarded!");
            }
        );

        JButton applyButton = new JButton("Apply Changes");
        applyButton.setBorderPainted(false);
        applyButton.setOpaque(false);
        applyButton.addActionListener(e -> {
            Document finalChangesDocument = editor.getDocument();
            finalToolWindow.hide(null);
            finalToolWindow.getContentManager().removeAllContents(true);
            ApplicationManager.getApplication().invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    var selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    if (selectedEditor != null) {
                        String updatedCode = finalChangesDocument.getText();
                        selectedEditor.getDocument().setText(updatedCode);
                        PsiDocumentManager.getInstance(project).commitDocument(selectedEditor.getDocument());
                    }
                });
                finalToolWindow.remove();
                showSuccess("Code successfully updated!");
            });
        });

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftButtonPanel.add(discardButton);
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightButtonPanel.add(applyButton);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        finalToolWindow.getContentManager().removeAllContents(true);
        finalToolWindow.getContentManager().addContent(content);
        finalToolWindow.show(null);
    }

    private void highlightDifferences(Editor editor, String originalCode, String improvedCode) {
        String[] originalLines = originalCode.split("\n");
        String[] improvedLines = improvedCode.split("\n");

        List<Integer> changedLines = new ArrayList<>();

        int maxLines = Math.max(originalLines.length, improvedLines.length);
        for (int i = 0; i < maxLines; i++) {
            String originalLine = i < originalLines.length ? originalLines[i] : "";
            String improvedLine = i < improvedLines.length ? improvedLines[i] : "";

            if (!originalLine.equals(improvedLine)) {
                changedLines.add(i);
            }
        }

        Color highlightColor = new Color(0x38, 0x9F, 0xD6, 26);
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setBackgroundColor(highlightColor);

        for (int line : changedLines) {
            editor.getMarkupModel().addLineHighlighter(line, HighlighterLayer.WARNING, textAttributes);
        }
    }

    private void showError() {
        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showErrorDialog(project, "No active editor found.", "Error")
        );
    }

    private void showSuccess(String message) {
        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showInfoMessage(project, message, "Success")
        );
    }
}