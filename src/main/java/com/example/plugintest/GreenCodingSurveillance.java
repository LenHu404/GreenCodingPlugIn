package com.example.plugintest;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.GotItTooltip;
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

    String[] codeMarker = new String[]{"-!-", "-!-"};
    String[] reasonMarker = new String[]{"#?#", "#?#"};
    String[] lineMarker = new String[]{"$!$", "$!$"};

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(AnActionEvent e) {
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
                processCodeAsync(codeInputWithLines, project, editor, startLine);
            } else {
                String fileContent = editor.getDocument().getText();

                String codeInputWithLines = addLineNumbers(fileContent, 0);
                processCodeAsync(codeInputWithLines, project, editor, 0);
            }
        }
    }

    private String addLineNumbers(String input, int startLine) {
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
        return input.replaceAll("\\d+:", "");
    }

    private void processCodeAsync(String codeInputWithLines, Project project, Editor editor, int startLine) {
        JFrame parentFrame = WindowManager.getInstance().getFrame(project);
        LoadingDialog loadingDialog = new LoadingDialog(parentFrame);
        loadingDialog.showDialog();

        CompletableFuture.supplyAsync(() -> {
            String response = null;
            try {
                response = getAIAnswerAsync(codeInputWithLines).get();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Can't Connect.");
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("Load Error")
                        .createNotification("Connection error","It seems like the plugIn can't connect to the AI. \n Please check if you are conected to the internet.", NotificationType.ERROR)
                        .notify(project);
            }
            return response;
        }).thenAccept(response -> {
            loadingDialog.hideDialog();
            if (loadingDialog.isCancelled() || response == null) {
                return;
            }
            AiAnswer result = StringSplitter(response);
            String correctedCode = result.codeOutput;
            String reason = result.reason;
            int[] correctedLines = result.lines;

            SwingUtilities.invokeLater(() -> {
                showPreviewDialog(codeInputWithLines, addLineNumbers(removeLineNumbers(correctedCode), startLine), reason, correctedLines, startLine, () -> {
                    // Ensure that the write action is executed on the EDT
                    ApplicationManager.getApplication().invokeLater(() -> {
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            editor.getDocument().replaceString(
                                    editor.getSelectionModel().getSelectionStart(),
                                    editor.getSelectionModel().getSelectionEnd(),
                                    removeLineNumbers(correctedCode));
                        });
                    });
                });
            });
        });
    }



    private AiAnswer StringSplitter(String input) {
        String code = StringUtils.substringBetween(input, codeMarker[0], codeMarker[1]).trim();
        String reason = StringUtils.substringBetween(input, reasonMarker[0], reasonMarker[1]).trim();
        String linesString = StringUtils.substringBetween(input, lineMarker[0], lineMarker[1]);

        String[] linesArray = linesString.split(",");
        ArrayList<Integer> lines = new ArrayList<>();
        for (String s : linesArray) {
            if (s.contains("-")) {
                String[] range = s.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        for (int number = start; number <= end; number++) {
                            lines.add(number);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println(e);
                        System.err.println("Invalid range format:" + s);
                    }
                }
            } else if (!s.isEmpty()) {
                try {
                    lines.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    System.err.println(e);
                    System.err.println("Invalid number format:" + s);
                }
            }
        }
        int[] linesFromList = lines.stream().mapToInt(i -> i).toArray();

        return new AiAnswer(code, reason, linesFromList);
    }

    private void showPreviewDialog(String originalCode, String editedCode, String reason, int[] lines, int startLine, Runnable confirmAction) {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2;
        gbc.weighty = 2;
        gbc.fill = GridBagConstraints.BOTH;

        JTextArea originalTextArea = highlightLines(new JTextArea(originalCode), lines, startLine, JBColor.RED);
        JTextArea editedTextArea = new JTextArea(editedCode);
        JTextArea additionalTextArea = new JTextArea(reason);
        additionalTextArea.setLineWrap(true);
        additionalTextArea.setWrapStyleWord(true);

        int padding = 10;
        Border customBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.GRAY),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)
        );

        originalTextArea.setBorder(customBorder);
        editedTextArea.setBorder(customBorder);
        additionalTextArea.setBorder(customBorder);

        originalTextArea.setEditable(true);
        editedTextArea.setEditable(true);

        JScrollPane originalScrollPane = new JBScrollPane(originalTextArea);
        JScrollPane editedScrollPane = new JBScrollPane(editedTextArea);
        JScrollPane reasonScrollPane = new JBScrollPane(additionalTextArea);

        // Set preferred and minimum size for the scroll panes
        Dimension minSize = new Dimension(400, 200); // Set your desired minimum size
        Dimension preferredSize = new Dimension(600, 400); // Set your desired preferred size

        originalScrollPane.setMinimumSize(minSize);
        originalScrollPane.setPreferredSize(preferredSize);

        editedScrollPane.setMinimumSize(minSize);
        editedScrollPane.setPreferredSize(preferredSize);

        reasonScrollPane.setMinimumSize(new Dimension(400, 40)); // Set minimum size for the reason area
        reasonScrollPane.setPreferredSize(new Dimension(600, 60)); // Set preferred size for the reason area

        panel.add(originalScrollPane, gbc);

        gbc.gridx = 1;
        panel.add(editedScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(reasonScrollPane, gbc);


        int userChoice = JOptionPane.showConfirmDialog(null, panel, "Green-Coding-Inspector: Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (userChoice == JOptionPane.OK_OPTION) {
            confirmAction.run();
        }
    }

    private JTextArea highlightLines(JTextArea textArea, int[] linesToHighlight, int startLine, JBColor color) {
        JTextArea highlightedTextArea = new JTextArea();
        highlightedTextArea.setEditable(false);
        highlightedTextArea.setText(textArea.getText());

        Highlighter highlighter = highlightedTextArea.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(color);

        String[] lines = textArea.getText().split("\n");

        for (int line : linesToHighlight) {
            line -= startLine;
            int startOffset = 0;
            int endOffset;
            if (line < lines.length) {
                for (int i = 0; i < line - 1; i++) {
                    startOffset += lines[i].length() + 1;
                }
                endOffset = startOffset + lines[line - 1].length();
                try {
                    highlighter.addHighlight(startOffset, endOffset, painter);
                    System.out.println("Added highlighter for line: " + (line + startLine));
                } catch (Exception ex) {
                    System.out.println("Couldn't highlight lines because: ");
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Didn't highlight because of: line = " + line + " < lines.length = " + lines.length);
            }
        }

        return highlightedTextArea;
    }
}
