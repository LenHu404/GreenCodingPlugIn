package com.example.plugintest;

import com.intellij.ui.AnimatedIcon;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog {
    private JDialog dialog;
    private volatile boolean cancelled = false;

    public LoadingDialog(JFrame parentFrame) {
        dialog = new JDialog(parentFrame, "Loading", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        JLabel infoText = new JLabel(
                " Getting the answer from the AI... \n This can take up to 15s depending on your Network.",
                new AnimatedIcon.Default(),
                SwingConstants.LEFT);
        infoText.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(infoText);
        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> cancel());
        dialog.add(cancelButton, BorderLayout.SOUTH);
        dialog.setSize(550,100);

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
