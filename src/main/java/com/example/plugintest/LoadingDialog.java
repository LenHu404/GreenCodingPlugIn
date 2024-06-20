package com.example.plugintest;

import com.intellij.ui.AnimatedIcon;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

import static com.intellij.ide.plugins.newui.PluginPriceService.cancel;

public class LoadingDialog {
    private JDialog dialog;
    private volatile boolean cancelled = false;

    private String[] energyFacts = {
            "Meta, Amazon, Microsoft and Google as much energy in 2021 as Iceland.",
            "By 2030, up to 21% of electricity demand could be generated solely by the IT sector.",
            "Machine learning can help reduce cooling requirements in data centers.",
            "Green Architecture focuses on optimizing the fundamental structure of applications, like implementing automatic shutdown mechanisms.",
            "Green platforms focus on the hardware to ensure optimal server utilization, as infrastructure is as crucial for energy efficiency as the code itself.",
            "Green methodology focuses on making the software development process more sustainable.",
            "Green logic in Green Coding involves making decisions and optimizations to enhance software efficiency, minimize resource usage, and reduce environmental impact.",
            "Avoid For-each loops if not all elements are accessed. Use while loops instead.",
            "If a variable is changed, a primitive data type should always be used instead of a boxed/wrapper data type.",
            "Do not create Map, List or Stack without initial size.",
            "Do not use the Java split method.",
            "Avoid function calls in loop headers.",
            "Avoid shared static collections.",
            "Avoid the asterisk symbol (*) in SQL queries.",
            "Avoid executing SQL queries in a loop.",
            "Utilizing 'try with resources' for handling 'AutoCloseable' interfaces, promoting efficient resource management and minimizing environmental impact.",
            "Training GPT models is estimated to use around 1,300 megawatt hours (MWh) of electricity; about as much power as consumed annually by 130 US homes.",
            "You should only use this Plugin if you intend to run the code for a longer period of time.",
            "Watching 1,625,000 hours of Netflix consumes the same amount of energy as training a gpt-3 model.",
            "'With ChatGPT we don’t know how big it is, we don’t know how many parameters the underlying model has, we don’t know where it’s running … It could be three raccoons in a trench coat because you just don’t know what’s under the hood' - Sasha Luccioni, a researcher at Hugging Face"
    };

    public LoadingDialog(JFrame parentFrame) {
        dialog = new JDialog(parentFrame, "Loading", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        // Randomly select an energy fact index
        int randomIndex = new Random().nextInt(energyFacts.length);

        // Create a content panel to hold the components
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Create and center the animated icon
        JLabel animatedIcon = new JLabel(new AnimatedIcon.Default());
        animatedIcon.setHorizontalAlignment(SwingConstants.CENTER);

        // Create and center the information text
        JLabel infoText = new JLabel(
                "<html><div style='text-align: center;'><i>Getting the answer from the AI...<br>This can take up to 15s depending on your Network.</i><br><br><br><b>GreenCoding Fact:</b><br>" + energyFacts[randomIndex] + "</div></html>"
        );
        infoText.setHorizontalAlignment(SwingConstants.CENTER);

        // Add the components to the content panel
        contentPanel.add(animatedIcon, BorderLayout.NORTH);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Add padding
        contentPanel.add(infoText, BorderLayout.CENTER);

        // Add the content panel to the dialog
        dialog.add(contentPanel, BorderLayout.CENTER);


        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> cancel());
        dialog.add(cancelButton, BorderLayout.SOUTH);
        dialog.setSize(550,300);

        dialog.setLocationRelativeTo(parentFrame);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cancel();
            }
        });
    }

    public void showDialog() {
        cancelled = false;
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    public void hideDialog() {
        SwingUtilities.invokeLater(() -> dialog.setVisible(false));
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void cancel() {
        cancelled = true;
        hideDialog();
    }
}
