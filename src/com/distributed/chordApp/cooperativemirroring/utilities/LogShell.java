package com.distributed.chordApp.cooperativemirroring.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogShell extends JFrame {

    //Components that shows the logs messages
    private JTextArea textArea = null;
    //Boolean flag used to state if we have to automatically show older messages or not
    private boolean autohide = false;
    //String used for keeping track of all the logs produced so far
    private String logHistory = "";
    //Button used for cleaning or showing the log history
    private JToggleButton logAutohideButton = null;

    public LogShell(String title,boolean autohide){

        this.autohide = autohide;

        this.textArea = new JTextArea();
        this.textArea.setLineWrap(true);
        this.textArea.setEditable(false);

        JScrollPane scrollArea = new JScrollPane(this.textArea);

        this.logAutohideButton = new JToggleButton();
        if(autohide){
            this.logAutohideButton.setText("show log history");
            this.logAutohideButton.setSelected(autohide);
            this.textArea.setText(this.extractLastShellCommand());
        }
        else{
            this.logAutohideButton.setText("hide log history");
            this.logAutohideButton.setSelected(autohide);
            this.textArea.setText(this.logHistory);
        }
        this.logAutohideButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                switchAutoHide();
            }
        });

        this.setTitle("Log Shell - " + title);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);
        this.setMinimumSize(new Dimension(400, 300));
        this.setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.logAutohideButton);

        this.add(scrollArea, BorderLayout.CENTER);
        this.add(this.logAutohideButton, BorderLayout.SOUTH);
        this.setVisible(true);
    }

    /**
     * Method used for enable/disable the the autohide mode for the log shell
     */
    public void switchAutoHide(){

        this.autohide = !this.autohide;

        if(this.autohide){
            this.logAutohideButton.setText("show log history");
            this.logAutohideButton.setSelected(this.autohide);
            this.textArea.setText(this.extractLastShellCommand());
        }else{
            this.logAutohideButton.setText("hide log history");
            this.logAutohideButton.setSelected(this.autohide);
            this.textArea.setText(this.logHistory);
        }

    }

    /**
     * Method used for extracting the last shell command used
     * @return
     */
    public String extractLastShellCommand(){
        String lastCommand = "";

        lastCommand = this.logHistory.substring(this.logHistory.lastIndexOf("\n") + 1);

        return lastCommand;
    }

    /**
     * Method used for changing the text of the log console
     * @param newText
     */
    public void updateText(String newText){

        if(newText == null){
            newText = "";
        }

        this.logHistory += "\n";
        this.logHistory += newText;

        if(!autohide){
            this.textArea.setText(this.logHistory);
        }
        else{
            this.textArea.setText(newText);
        }

    }

    public boolean getAutohide(){return this.autohide;}
    public String getLogHistory(){return this.logHistory;}

}
