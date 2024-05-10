package com.example.plugintest;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.project.Project;

import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.contains;

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
                // If there's selected text get Lines and replace the selected text with corrected Code

                // Get Line where the curser is
                CaretModel caretModel = editor.getCaretModel();
                Caret primaryCaret = caretModel.getPrimaryCaret();
                VisualPosition selectionStartPosition = primaryCaret.getSelectionStartPosition();
                int startLine = selectionStartPosition.getLine();

                // Prepend line numbers to each line of selected text
                StringBuilder modifiedText = new StringBuilder();
                String[] lines = selectedText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    modifiedText.append(startLine + i + 1).append(": ").append(lines[i]);
                    if (i < lines.length - 1) {
                        modifiedText.append("\n");
                    }
                }

                // Send Code to the inspection
                AiAnswer result = checkCode(String.valueOf(modifiedText));
                String correctedCode = result.codeOutput;
                String reason = result.reason;
                int[] correctedLines = result.lines;

                // Replace Code with corrected Code
                showPreviewDialog(project, String.valueOf(modifiedText), correctedCode, reason, correctedLines, () -> {
                    // Perform document modification within a WriteCommandAction
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        editor.getDocument().replaceString(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd(), correctedCode);
                    });
                });

                String savedText = modifiedText.toString();
                System.out.println(savedText);

            } else {
                // If no text is selected, get the entire file content
                String fileContent = editor.getDocument().getText();

                // Prepend line numbers to each line of selected text
                StringBuilder modifiedText = new StringBuilder();
                String[] lines = fileContent.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    modifiedText.append(i + 1).append(": ").append(lines[i]);
                    if (i < lines.length - 1) {
                        modifiedText.append("\n");
                    }
                }

                // Send Code to the inspection
                AiAnswer result = checkCode(String.valueOf(modifiedText));
                String correctedCode = result.codeOutput;
                String reason = result.reason;
                int[] correctedLines = result.lines;

                // Replace Code with corrected Code
                showPreviewDialog(project, String.valueOf(modifiedText), correctedCode, reason, correctedLines, () -> {
                    // Perform document modification within a WriteCommandAction
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        editor.getDocument().setText(correctedCode);
                    });
                });

                String savedText = modifiedText.toString();
                System.out.println(savedText);
            }
        }
    }


    private AiAnswer checkCode(String codeInput) {
        // check codeInput and send corrected code back with a reason why and which lines are changed
        String codeOutput = codeInput + ":)";
        String reason = "Methods shouldn't be called in the Loop initialisation.";
        int[] lines = new int[]{1,2};

        return new AiAnswer( codeOutput, reason, lines );
    }


    private void showPreviewDialog(Project project, String originalCode, String editedCode, String reason, int[] lines, Runnable confirmAction) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2;
        gbc.weighty = 2;
        gbc.fill = GridBagConstraints.BOTH;

        JTextArea originalTextArea = new JTextArea(originalCode);
        JTextArea editedTextArea = highlightLines(new JTextArea(editedCode), lines);
        JTextArea additionalTextArea = new JTextArea(reason);

        int padding = 10; // Adjust padding as needed
        Border customBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.GRAY), // Outer border
                BorderFactory.createEmptyBorder(padding, padding, padding, padding) // Padding
        );

        // Set the custom border to the JTextArea
        originalTextArea.setBorder(customBorder);
        editedTextArea.setBorder(customBorder);
        additionalTextArea.setBorder(customBorder);

        originalTextArea.setEditable(false);
        editedTextArea.setEditable(true);

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

    private JTextArea highlightLines(JTextArea textArea, int[] linesToHighlight) {
        JTextArea highlightedTextArea = new JTextArea();
        highlightedTextArea.setEditable(false);
        highlightedTextArea.setText(textArea.getText());

        Highlighter highlighter = highlightedTextArea.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(JBColor.GREEN);

        String[] lines = textArea.getText().split("\n");

        for (int line : linesToHighlight) {
            int startOffset = 0;
            int endOffset = 0;
            for (int i = 0; i < line - 1; i++) {
                startOffset += lines[i].length() + 1; // Add 1 for the newline character
            }
            endOffset = startOffset + lines[line - 1].length();
            try {
                highlighter.addHighlight(startOffset, endOffset, painter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return highlightedTextArea;

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
