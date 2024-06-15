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
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static com.example.plugintest.KIConnect.getAIAnswerAsync;

public class GreenCodingSurveillance extends AnAction {

    // Marker to separate the parts in the response from the AI
    String[] codeMarker = new String[]{"-!-", "-!-"};
    String[] reasonMarker = new String[]{"#?#", "#?#"};
    String[] lineMarker = new String[]{"$!$", "$!$"};

    // code between ++ and --
    // reason between #?# and #!#
    // lines between $?$ and $!

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
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (selectedText != null && !selectedText.isEmpty()) {
                CaretModel caretModel = editor.getCaretModel();
                Caret primaryCaret = caretModel.getPrimaryCaret();
                VisualPosition selectionStartPosition = primaryCaret.getSelectionStartPosition();
                int startLine = selectionStartPosition.getLine();

                String codeInputWithLines = addLineNumbers(selectedText, startLine);
                processCodeAsync(codeInputWithLines, project, editor);
            } else {
                String fileContent = editor.getDocument().getText();

                String codeInputWithLines = addLineNumbers(fileContent, 0);
                processCodeAsync(codeInputWithLines, project, editor);
            }
        }
    }



    private String addLineNumbers(String input, int startLine) {
        // add line numbers at the start of each line
        StringBuilder modifiedText = new StringBuilder();
        String[] lines = input.split("\n");
        for (int i = 0; i < lines.length; i++) {
            modifiedText.append(startLine + i + 1).append(": ").append(lines[i]);
            if (i < lines.length - 1) {
                modifiedText.append("\n");
            }
        }
        return modifiedText.toString();
    }
    private String removeLineNumbers(String input) {
        // remove Line numbers at the start of each line
        return input.replaceAll("\\d+:", "");
    }

    private void processCodeAsync(String codeInputWithLines, Project project, Editor editor) {
        JFrame parentFrame = WindowManager.getInstance().getFrame(project);
        LoadingDialog loadingDialog = new LoadingDialog(parentFrame);
        loadingDialog.showDialog();

        CompletableFuture.supplyAsync(() -> {
            String response = null;
            try {
                response = getAIAnswerAsync(codeInputWithLines).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }).thenAccept(response -> {
            loadingDialog.hideDialog();
            AiAnswer result = StringSplitter(response);
            String correctedCode = result.codeOutput;
            String reason = result.reason;
            int[] correctedLines = result.lines;

            SwingUtilities.invokeLater(() -> {
                showPreviewDialog(codeInputWithLines, correctedCode, reason, correctedLines, () -> {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        editor.getDocument().replaceString(
                                editor.getSelectionModel().getSelectionStart(),
                                editor.getSelectionModel().getSelectionEnd(),
                                removeLineNumbers(correctedCode));
                    });
                });
            });
        });
    }

    private AiAnswer StringSplitter(String input) {
        //Split Code into different parts, marked by the markers (defined at the top)

        String code = StringUtils.substringBetween(input, codeMarker[0], codeMarker[1]);
        String reason = StringUtils.substringBetween(input, reasonMarker[0], reasonMarker[1]);
        String linesString = StringUtils.substringBetween(input, lineMarker[0], lineMarker[1]);

        // Convert the third part string into an array of integers
        String[] linesArray = linesString.split(",");
        ArrayList<Integer> lines = new ArrayList<>();
        for (String s : linesArray) {
            if (s.contains("-")) {
                String[] range = s.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        for (int number = start; number <= end; number++) {
                            lines.add(number);
                        }
                    } catch (NumberFormatException e) {
                        // Handle error for malformed range
                        System.err.println("Invalid range format: " + s);
                    }
                }
            } else if (!s.isEmpty()) {
                try {
                    lines.add(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    // Handle error for malformed number
                    System.err.println("Invalid number format: " + s);
                }
            }
        }
        /*System.out.println("code: " + code);
        System.out.println("reason: " + reason);
        System.out.println("lines: " + linesString);*/
        int[] linesFromList = lines.stream().mapToInt(i -> i).toArray();


        return new AiAnswer(code, reason,linesFromList);

    }


    private void showPreviewDialog(String originalCode, String editedCode, String reason, int[] lines, Runnable confirmAction) {
        //Show the original Code, the Code modified by Ai and the reason for the change in three windows in a Preview,
        //the changed lines by the AI are highlighted
        // user can decide to accept the new code or not

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2;
        gbc.weighty = 2;
        gbc.fill = GridBagConstraints.BOTH;

        JTextArea originalTextArea = highlightLines(new JTextArea(originalCode),lines);
        JTextArea editedTextArea = new JTextArea(editedCode);
        JTextArea additionalTextArea = new JTextArea(reason);
        additionalTextArea.setLineWrap(true);
        additionalTextArea.setWrapStyleWord(true);

        int padding = 10; // Adjust padding as needed
        Border customBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.GRAY), // Outer border
                BorderFactory.createEmptyBorder(padding, padding, padding, padding) // Padding
        );

        // Set the custom border to the JTextArea
        originalTextArea.setBorder(customBorder);
        editedTextArea.setBorder(customBorder);
        additionalTextArea.setBorder(customBorder);

        originalTextArea.setEditable(true);
        editedTextArea.setEditable(true);

        JScrollPane originalScrollPane = new JBScrollPane(originalTextArea);
        JScrollPane editedScrollPane = new JBScrollPane(editedTextArea);
        JScrollPane reasonScrollPane = new JBScrollPane(additionalTextArea);

        panel.add(originalScrollPane, gbc);

        gbc.gridx = 1;

        panel.add(editedScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;

        panel.add(reasonScrollPane, gbc);

        int userChoice = JOptionPane.showConfirmDialog(null, panel, "Green-Coding-Inspector: Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (userChoice == JOptionPane.OK_OPTION) {
            // User accepted the changes, execute the confirmation action
            confirmAction.run();
        }
    }

    private JTextArea highlightLines(JTextArea textArea, int[] linesToHighlight) {
        // Highlight the lines changed by the AI in the modified Code preview
        JTextArea highlightedTextArea = new JTextArea();
        highlightedTextArea.setEditable(false);
        highlightedTextArea.setText(textArea.getText());

        Highlighter highlighter = highlightedTextArea.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(JBColor.RED);

        String[] lines = textArea.getText().split("\n");

        for (int line : linesToHighlight) {
            int startOffset = 0;
            int endOffset;
            if (line < lines.length) {
                for (int i = 0; i < line - 1; i++) {
                    startOffset += lines[i].length() + 1; // Add 1 for the newline character
                }
                endOffset = startOffset + lines[line - 1].length();
                try {
                    highlighter.addHighlight(startOffset, endOffset, painter);
                } catch (Exception ex) {
                    System.out.println("Couldn't highlight lines because: ");
                    ex.printStackTrace();
                }
            }

        }

        return highlightedTextArea;
    }
}
