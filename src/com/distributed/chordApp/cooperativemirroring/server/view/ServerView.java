package com.distributed.chordApp.cooperativemirroring.server.view;

import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.server.core.Host;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ServerView extends JFrame {
    private Host host = null;

    /********** MESSAGE PART OF THE SHELL **********/
    private JPanel messagePanel = null;
    private JTextField messageTextField = null;
    private JButton tacitMessageButton = null;

    /********** CONSOLE PART OF THE SHELL **********/
    private JPanel consolePanel = null;
    //Button used for shutting down the server
    private JButton shutDowsServerButton = null;

    /********** LOG PART OF THE SHELL **********/
    private JPanel logPanel = null;
    //Components that shows the logs messages
    private JTextArea textArea = null;

    public ServerView(String title, Host host, JTextArea logArea){

        this.host = host;

        this.initMessagePanel();
        this.initConsolePanel();

        this.setTitle("Log Shell - " + title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setMinimumSize(new Dimension(400, 300));
        this.setLocationRelativeTo(null);

        this.initLogPanel(logArea);

        this.add(this.messagePanel, BorderLayout.NORTH);
        this.add(this.logPanel,BorderLayout.CENTER);
        this.add(this.consolePanel, BorderLayout.SOUTH);


        this.setVisible(true);
    }

    /**
     * Method used for creating a log area
     * @return
     */
    public static JTextArea createLogArea(){
        JTextArea textArea = new JTextArea(10, 10);
        textArea.setLineWrap(true);
        textArea.setEditable(false);

        return textArea;
    }

    /**
     * Method used for setting the message part of the server
     */
    private void initMessagePanel(){
        if(this.messagePanel != null){
            return ;
        }

        this.messagePanel = new JPanel();
        this.messagePanel.setLayout(new BorderLayout());

        //Message part
        this.messageTextField = new JTextField();
        this.messageTextField.setBackground(Color.LIGHT_GRAY);
        this.messageTextField.setEditable(false);

        //tacit part
        this.tacitMessageButton = new JButton("Tacit");
        this.tacitMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                messageTextField.setBackground(Color.LIGHT_GRAY);
                messageTextField.setText("");
            }
        });

        this.messagePanel.add(this.tacitMessageButton, BorderLayout.EAST);
        this.messagePanel.add(this.messageTextField, BorderLayout.CENTER);
    }

    /**
     * Method used for setting the console panel of the server
     */
    private void initConsolePanel(){
        if(this.consolePanel != null){
            return ;
        }

        this.consolePanel = new JPanel();
        this.consolePanel.setLayout(new GridLayout(1, 1));

        //Shutdown server command
        this.shutDowsServerButton = new JButton("Shut Down Server");
        this.shutDowsServerButton.setForeground(Color.RED);
        this.shutDowsServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int choice = JOptionPane.showConfirmDialog(null, "Are you sure to shutdown the server? ", "Shut Down server command", JOptionPane.WARNING_MESSAGE);

                if(choice == 0){
                    try {
                        host.shutdownHost();
                    } catch (IOException e) {
                        updateText(e.getMessage());
                        updateMessage("Unable to shut-down host : " + e.getMessage(), true);
                    }
                }
            }
        });
        JPanel shutDownServerPanel = new JPanel(new BorderLayout());
        shutDownServerPanel.add(this.shutDowsServerButton, BorderLayout.NORTH);

        this.consolePanel.add(shutDownServerPanel);
    }

    /**
     * Method used for setting the log part of the server shell
     */
    private void initLogPanel(JTextArea textArea){

        if(this.logPanel != null){
            return ;
        }

        this.logPanel = new JPanel();
        this.logPanel.setLayout(new GridLayout(1, 1));


        this.textArea = textArea;

        JScrollPane scrollArea = new JScrollPane(this.textArea);

        this.logPanel.add(scrollArea);
    }

    /**
     * Method used for changing the text of the log console
     * @param newText
     */
    public void updateText(String newText){

        if(newText == null){
            newText = "";
        }

        String text = this.textArea.getText();

        text += "\n" + newText;

        this.textArea.setText(text);

    }

    /**
     * Method used for changing the text displayed by the message console
     */
    public void updateMessage(String text,boolean errorMessage){

        if((text == null) || (text.length() < 1)){
            return;
        }

        if(errorMessage){
            this.messageTextField.setBackground(Color.RED);
        }
        else {
            this.messageTextField.setBackground(Color.GREEN);
        }

        this.messageTextField.setText(text);

    }

    public JTextArea getLogArea(){return this.textArea;}

    @Override
    @SuppressWarnings("deprecation")
    public void finalize(){

        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

}
