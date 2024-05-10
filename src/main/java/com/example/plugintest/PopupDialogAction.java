package com.example.plugintest;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.actionSystem.PlatformDataKeys;


public class PopupDialogAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Using the event, evaluate the context,
        // and enable or disable the action.
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (editor != null) {
            // Get the selected text
            String selectedText = editor.getSelectionModel().getSelectedText();

            if (selectedText != null && !selectedText.isEmpty()) {
                // If there's selected text, display it in a dialog for editing
                String editedText = Messages.showInputDialog(project, "Edit selected text:", "Edit Code", Messages.getQuestionIcon(), selectedText, null);

                if (editedText != null) {
                    // If the user provided edited text, replace the selected text with it
                    editor.getDocument().replaceString(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd(), editedText);
                }
            } else {
                // If no text is selected, get the entire file content
                String fileContent = editor.getDocument().getText();
                // Display the file content in a dialog for editing
                String editedText = Messages.showInputDialog(project, "Edit file content:", "Edit Code", Messages.getQuestionIcon(), fileContent, null);

                if (editedText != null) {
                    // If the user provided edited text, replace the entire file content with it
                    editor.getDocument().setText(editedText);
                }
            }
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}
