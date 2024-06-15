package com.example.plugintest;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog {

    private final JDialog dialog;

    public LoadingDialog(JFrame parentFrame) {
        dialog = new JDialog(parentFrame, "Loading...", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Please wait...");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.SOUTH);

        dialog.add(panel);
    }

    public void showDialog() {
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    public void hideDialog() {
        SwingUtilities.invokeLater(dialog::dispose);
    }
}

