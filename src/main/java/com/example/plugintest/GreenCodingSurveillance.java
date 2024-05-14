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
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class GreenCodingSurveillance extends AnAction {
    String UrlGet = "https://httpbin.org/get"; // URL to tht REST Service to get the code checked

    // Marker to separate the parts in the response from the AI
    String[] codeMarker = new String[]{"++", "--"};
    String[] reasonMarker = new String[]{"#?#", "#!#"};
    String[] lineMarker = new String[]{"$?$", "$!$"};

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
            // Get the selected text

            String selectedText = editor.getSelectionModel().getSelectedText();


            if (selectedText != null && !selectedText.isEmpty()) {
                // If there's selected text get Lines and replace the selected text with corrected Code

                // Get Line where the cursor is
                CaretModel caretModel = editor.getCaretModel();
                Caret primaryCaret = caretModel.getPrimaryCaret();
                VisualPosition selectionStartPosition = primaryCaret.getSelectionStartPosition();
                int startLine = selectionStartPosition.getLine();


                String codeInputWithLines = addLineNumbers(selectedText, startLine);
                // System.out.println(codeInputWithLines);


                // Send Code to the inspection
                AiAnswer result = checkCode(codeInputWithLines);
                String correctedCode = result.codeOutput;
                String reason = result.reason;
                int[] correctedLines = result.lines;



                // Compare Code and display the reason for the change in a preview with highlighted lines
                showPreviewDialog(codeInputWithLines, correctedCode, reason, correctedLines, () -> {
                    // Perform document modification within a WriteCommandAction
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        // Replace Code with corrected Code
                        editor.getDocument().replaceString(
                                editor.getSelectionModel().getSelectionStart(),
                                editor.getSelectionModel().getSelectionEnd(),
                                // place corrected code with removed linenumber at the start of each line
                                removeLineNumbers(correctedCode));
                    });
                });


            } else {
                // If no text is selected, get the entire file content
                String fileContent = editor.getDocument().getText();


                String codeInputWithLines = addLineNumbers(fileContent, 0);
                //System.out.println(codeInputWithLines);


                // Send Code to the inspection
                AiAnswer result = checkCode(codeInputWithLines);
                String correctedCode = result.codeOutput;
                String reason = result.reason;
                int[] correctedLines = result.lines;

                // Compare Code and display the reason for the change in a preview with highlighted lines
                showPreviewDialog(codeInputWithLines, correctedCode, reason, correctedLines, () -> {
                    // Perform document modification within a WriteCommandAction
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        // Replace Code with corrected Code with removed linenumber at the start of each line
                        editor.getDocument().setText(removeLineNumbers(correctedCode));
                    });
                });

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

    private AiAnswer checkCode(String codeInput) {
        // check codeInput and get corrected code back with a reason why and which lines are changed
        String response;
        try {
            response = getCorrectCode(codeInput);
            System.out.println("Response= " + response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Split code to fit AiAnswer
        return StringSplitter(response);

        //Test Code
        /*String codeOutput = codeInput + "//  :)  ";
        String reason = "Methods shouldn't be called in the Loop initialisation.";
        int[] lines = new int[]{1};

        return new AiAnswer( codeOutput, reason, lines );
        */
    }

    private String getCorrectCode(String codeInput) throws IOException {
        // send code to AI to check and get the response via GET Request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            try {
                // Build the URI with query parameters
                URIBuilder builder = new URIBuilder(UrlGet);
                builder.setParameter("codeInput", codeInput);
                URI uri = builder.build();

                // Create the GET request
                HttpGet request = new HttpGet(uri);

                // Execute the request
                HttpResponse response = httpClient.execute(request);

                // Extract and print the response wit added \n to separate each line into a new one
                //System.out.println("ResponseBody: " + responseBody);
                return EntityUtils.toString(response.getEntity()).replaceAll("\\\\n", "\n");
            } catch (Exception e) {
                System.out.println("Couldn't get the code from AI");
                e.printStackTrace();
            }
        }
        System.out.println("failed to GET");
        return "failed";
    }

    private AiAnswer StringSplitter(String input) {
        //Split Code into different parts, marked by the markers (defined at the top)

        String code = StringUtils.substringBetween(input, codeMarker[0], codeMarker[1]);
        String reason = StringUtils.substringBetween(input, reasonMarker[0], reasonMarker[1]);
        String linesString = StringUtils.substringBetween(input, lineMarker[0], lineMarker[1]);

        // Convert the third part string into an array of integers
        String[] linesArray = linesString.split(",");
        int[] lines = new int[linesArray.length];
        for (int i = 0; i < linesArray.length; i++) {
            lines[i] = Integer.parseInt(linesArray[i].trim());
        }

        /*System.out.println("code: " + code);
        System.out.println("reason: " + reason);
        System.out.println("lines: " + linesString);*/

        return new AiAnswer(code, reason, lines);

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
        JScrollPane reasonScrollPane = new JBScrollPane(additionalTextArea);

        panel.add(originalScrollPane, gbc);

        gbc.gridx = 1;

        panel.add(editedScrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;

        panel.add(reasonScrollPane, gbc);

        int userChoice = JOptionPane.showConfirmDialog(null, panel, "Preview", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (userChoice == JOptionPane.YES_OPTION) {
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
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(JBColor.GREEN);

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
