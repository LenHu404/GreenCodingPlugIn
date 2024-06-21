package com.example.plugintest;

import com.intellij.ui.AnimatedIcon;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class LoadingDialog {
    private final JDialog dialog;
    private final JLabel elapsedTimeLabel;
    private volatile boolean cancelled = false;
    private long startTime;
    private Timer timer;
    private final String[] energyFacts = {
            "Meta, Amazon, Microsoft and Google used as much energy in 2021 as Iceland.",
            "By 2030, up to 21% of electricity demand could be generated solely by the IT sector.",
            "Machine learning can help reduce cooling requirements in data centers.",
            "Green Architecture focuses on optimizing the fundamental structure of applications, like implementing automatic shutdown mechanisms.",
            "Green platforms focus on the hardware to ensure optimal server utilization, as infrastructure is as crucial for energy efficiency as the code itself.",
            "Green methodology focuses on making the software development process more sustainable.",
            "Green logic in Green Coding involves making decisions and optimizations to enhance software efficiency, minimize resource usage, and reduce environmental impact.",
            "Avoid For-each loops if not all elements are accessed. Use while loops instead.",
            "If a variable is changed, a primitive data type should always be used instead of a boxed/wrapper data type.",
            "Do not create Map, List or Stack without size. Initializing the size at the beginning helps avoid unnecessary memory allocation.",
            "Avoid using java split method. Instead, create a separate split method that fits your application exactly.",
            "Avoid function calls in loop headers. Function calls are expensive operations that can significantly impact performance. By avoiding function calls in the loop header, you can reduce the overhead of function invocation and improve the overall execution speed.",
            "Avoid shared static collections. Static collections shared across multiple instances or modules can lead to concurrency issues and memory leaks.",
            "Do not use the asterisk symbol (*) in SQL queries to select all columns of a table. Instead, you should specifically query only those fields that are required for your application.",
            "Executing SQL queries within a loop can lead to unnecessary calculations by the CPU, increased RAM consumption and unnecessary network transfer",
            "Utilizing 'try with resources' for handling 'AutoCloseable' interfaces, promoting efficient resource management and minimizing environmental impact.",
            "Training GPT models is estimated to use around 1,300 megawatt hours (MWh) of electricity; about as much power as consumed annually by 130 US homes.",
            "You should only use this Plugin if you intend to run the code for a longer period of time.",
            "Avoid using exceptions to control the normal flow of control in your code. Exceptions should only be used for exceptional conditions, as their generation is costly.",
            "Watching 1,625,000 hours of Netflix consumes the same amount of energy as training a gpt-3 model.",
            "Accesses to disc or network operations are expensive. Cache data where possible and read or write data in larger blocks to reduce the number of operations.",
            "Recursion is elegant, but can lead to stack overflow errors with large amounts of data. Use iterative approaches if possible.",
            "'With ChatGPT we don’t know how big it is, we don’t know how many parameters the underlying model has, we don’t know where it’s running … It could be three raccoons in a trench coat because you just don’t know what’s under the hood' - Sasha Luccioni, a researcher at Hugging Face"
    };

    public LoadingDialog(JFrame parentFrame, int timeInSeconds) {
        dialog = new JDialog(parentFrame, "Loading", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        // Randomly select an energy fact index
        int randomIndex = new Random().nextInt(energyFacts.length);

        // Create a content panel to hold the components
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Create and center the animated icon
        JLabel animatedIcon = new JLabel(new AnimatedIcon.Default());
        animatedIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create and center the information text
        JLabel infoText = new JLabel(
                "<html><div style='text-align: center;'><i>Getting the answer from the AI...<br>This should take around " + timeInSeconds + "s depending on the length of your code and your Network.</i></div></html>"
        );
        infoText.setHorizontalAlignment(SwingConstants.CENTER);
        infoText.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Create and center the elapsed time label
        elapsedTimeLabel = new JLabel("Elapsed time: 0 seconds");
        elapsedTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        elapsedTimeLabel.setBorder(JBUI.Borders.empty(10, 0));

        // Create and center the random fact
        JLabel randomFact = new JLabel(
                "<html><div style='text-align: center;'><b>GreenCoding Fact:</b><br>" + energyFacts[randomIndex] + "</div></html>"
        );
        randomFact.setHorizontalAlignment(SwingConstants.CENTER);
        randomFact.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Add the components to the content panel
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add spacing between components
        contentPanel.add(animatedIcon);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between components
        contentPanel.add(infoText);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 3))); // Add spacing between components
        contentPanel.add(elapsedTimeLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Add spacing between components
        contentPanel.add(randomFact);
        contentPanel.setSize(550,300);

        // Add the content panel to the dialog
        dialog.add(contentPanel, BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());
        dialog.add(cancelButton, BorderLayout.SOUTH);
        dialog.setSize(750, 300);

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
        startTime = System.currentTimeMillis();
        startTimer();
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    public void hideDialog() {
        stopTimer();
        System.out.println("Response took " + ((System.currentTimeMillis() - startTime)/1000) + "s to complete.");
        SwingUtilities.invokeLater(() -> dialog.setVisible(false));
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void cancel() {
        cancelled = true;
        hideDialog();
    }

    private void startTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long seconds = elapsedTime / 1000;
                elapsedTimeLabel.setText("Elapsed time: " + seconds + " seconds");
            }
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }
}
