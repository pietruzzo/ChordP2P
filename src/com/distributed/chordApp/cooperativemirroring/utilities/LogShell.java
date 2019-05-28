package com.distributed.chordApp.cooperativemirroring.utilities;

import javax.swing.*;
import java.awt.*;

public class LogShell extends JFrame {

    private JTextArea textArea = null;

    public LogShell(String title){

        this.textArea = new JTextArea();
        this.textArea.setLineWrap(true);
        this.textArea.setEditable(false);


        this.setTitle(title + " - Log Shell");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);

        this.add(this.textArea);
        this.setVisible(true);
    }

    public void updateText(String newText){
        String text = this.textArea.getText();

        text += "\n";
        text += newText;

        this.textArea.setText(text);

    }

}
