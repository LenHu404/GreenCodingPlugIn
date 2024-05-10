package com.example.plugintest;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class GreenCodingSurveillance extends AnAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (editor != null) {
            // Get the selected text
            String selectedText = editor.getSelectionModel().getSelectedText();

            if (selectedText != null && !selectedText.isEmpty()) {
                // If there's selected text replace the selected text with corrected Code
                String[] result = checkCode(selectedText);
                String correctedCode = result[0];
                String reason = result[1];                // Replace Code with corrected Code

                showPreviewDialog(project, selectedText, correctedCode, reason, () -> {
                    // Perform document modification within a WriteCommandAction
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        editor.getDocument().replaceString(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd(), correctedCode);
                    });
                });

            } else {
                // If no text is selected, get the entire file content
                String fileContent = editor.getDocument().getText();

                String[] result = checkCode(fileContent);
                String correctedCode = result[0];
                String reason = result[1];

                // Replace Code with corrected Code
                showPreviewDialog(project, fileContent, correctedCode, reason, () -> {
                    // Perform document modification within a WriteCommandAction
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        editor.getDocument().setText(correctedCode);
                    });
                });
            }
        }
    }


    private String[] checkCode(String codeInput) {
        // check codeInput and send corrected code back with a reason why
        return new String[]{codeInput + "\n 'beep boop, now better'", "Your code is sh@t >:) \n " +
                "skippidi boop" };
    }


    private void showPreviewDialog(Project project, String originalCode, String editedCode, String reason, Runnable confirmAction) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;

        JTextArea originalTextArea = new JTextArea(originalCode);
        JTextArea editedTextArea = new JTextArea(editedCode);
        JTextArea additionalTextArea = new JTextArea(reason);

        originalTextArea.setEditable(false);
        editedTextArea.setEditable(false);

        JScrollPane originalScrollPane = new JBScrollPane(originalTextArea);
        JScrollPane editedScrollPane = new JBScrollPane(editedTextArea);
        JScrollPane additionalScrollPane = new JBScrollPane(additionalTextArea);

        panel.add(originalScrollPane, gbc);

        gbc.gridx = 1;

        panel.add(editedScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;

        panel.add(additionalScrollPane, gbc);

        int userChoice = JOptionPane.showConfirmDialog(null, panel, "Preview", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (userChoice == JOptionPane.YES_OPTION) {
            // User accepted the changes, execute the confirm action
            confirmAction.run();
        }
    }

    private void showEditorTextField(Project project, String originalCode, String editedCode, String reason, Runnable confirmAction) {
        EditorTextField window = new EditorTextField();
    }



    /*private void showPreviewDialog(Project project, String originalCode, String editedCode, String reasonForChange, Runnable confirmAction) {
        StringBuilder previewMessage = new StringBuilder();
        previewMessage.append("<b> Original Code: <\b> \n\n");
        previewMessage.append(originalCode);
        previewMessage.append("\n\n <b> Edited Code: <\b> \n\n");
        previewMessage.append(editedCode);
        previewMessage.append("\n\n <b> Reason for the Change: <\b> \n\n");
        previewMessage.append(reasonForChange);

        int userChoice = Messages.showYesNoDialog(project, previewMessage.toString(), "Preview", "Accept", "Cancel", Messages.getQuestionIcon());

        if (userChoice == Messages.YES) {
            // User accepted the changes, execute the confirm action
            confirmAction.run();
        }
    }*/


}
