package com.distributed.chordApp.cooperativemirroring.client.view;

import com.distributed.chordApp.cooperativemirroring.client.controller.ClientController;
import com.distributed.chordApp.cooperativemirroring.client.utilities.ClientSettings;
import com.distributed.chordApp.cooperativemirroring.common.Resource;
import com.distributed.chordApp.cooperativemirroring.common.messages.RequestMessage;
import com.distributed.chordApp.cooperativemirroring.common.messages.ResponseMessage;
import com.distributed.chordApp.cooperativemirroring.common.utilities.SystemUtilities;
import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class used for representing a terminal of the client side of the application
 */
public class ClientGUI extends JFrame {

    //Settings of the client
    private ClientSettings settings = null;
    //Controller of the client
    private ClientController controller = null;

    //Main Panel
    private JPanel mainPanel = null;
    private JTextArea logArea = null;
    private JTextArea requestArea = null;
    private JTextArea responseArea = null;

    //Menu panel
    private JPanel menuPanel = null;
    private JButton depositButton = null;
    private JButton retriveButton = null;

    //Notification panel
    private JPanel notificationPanel = null;
    private JTextField notificationTextField = null;
    private JButton tacitButton = null;

    public ClientGUI(ClientSettings settings,ClientController controller){

        this.settings = settings ;
        this.controller = controller;

        this.setTitle("Client Console @" + this.settings.getClientIP() + ":" + this.settings.getClientPort());
        this.setSize(new Dimension(800, 600));
        this.setMinimumSize(new Dimension(400, 300));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);

        this.initMenuPanel();
        this.initMainPanel();
        this.initNotificationPanel();

        this.add(this.menuPanel, BorderLayout.NORTH);
        this.add(this.mainPanel, BorderLayout.CENTER);
        this.add(this.notificationPanel, BorderLayout.SOUTH);

        this.setVisible(true);

    }

    /**
     * Method used for setting the main panel
     */
    private void initMainPanel(){
        if(this.mainPanel != null){
            return ;
        }

        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new GridLayout(2, 1));

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new GridLayout(1, 1));
        this.logArea = new JTextArea(10, 10);
        this.logArea.setEditable(false);
        this.logArea.setLineWrap(true);
        this.logArea.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(this.logArea);
        logPanel.add(scroll);

        JPanel messagesPanel = new JPanel();
        messagesPanel.setLayout(new GridLayout(1, 2));

        this.requestArea = new JTextArea(10, 10);
        this.requestArea.setLineWrap(true);
        this.requestArea.setEditable(false);
        this.requestArea.setBackground(Color.WHITE);
        JScrollPane requestScroll = new JScrollPane(this.requestArea);

        this.responseArea = new JTextArea(10, 10);
        this.responseArea.setLineWrap(true);
        this.responseArea.setEditable(false);
        this.responseArea.setBackground(Color.WHITE);
        JScrollPane responseScroll = new JScrollPane(this.responseArea);

        messagesPanel.add(requestScroll);
        messagesPanel.add(responseScroll);

        this.mainPanel.add(scroll);
        this.mainPanel.add(messagesPanel);
    }

    /**
     * Method used for setting the menu panel
     */
    private void initMenuPanel(){
        if(this.menuPanel != null){
            return;
        }

        this.menuPanel = new JPanel();
        this.menuPanel.setLayout(new GridLayout(1, 2));

        this.depositButton = new JButton("Deposit Resource");
        this.depositButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                String resource = JOptionPane.showInputDialog(null, "Insert the new resource value: ", "Deposit Resource", JOptionPane.INFORMATION_MESSAGE);

                if(resource == null)
                    return;

                if(!SystemUtilities.isValidResourceID(resource)){
                    updateLog(settings.clientInfoString("invalid resource"));
                    updateNotification("invalid resource", true);
                    return;
                }

                updateLog(settings.clientInfoString("building a new request ..."));
                RequestMessage requestMessage = controller.buildRequest(resource, true);
                updateLog(settings.clientInfoString("request built: " + requestMessage.conciseToString()));
                updateNotification("request send", false);
                updateLog(settings.clientInfoString("new request created, trying to send the request to server: " + settings.getReferenceServerIP() + ":" + settings.getReferenceServerPort()));
                requestArea.setText(requestMessage.toString());

                ResponseMessage responseMessage = null;
                try {
                    responseMessage = controller.sendRequest(requestMessage);
                } catch (SocketManagerException e) {
                    updateNotification("Unable to send request " + e.getMessage(), true);
                }

                if(responseMessage != null){
                    responseArea.setBackground(Color.WHITE);
                    updateLog(settings.clientInfoString("response arrived from server: " + settings.getReferenceServerIP() + ":" + settings.getReferenceServerPort() + " > " + responseMessage.conciseToString()));
                    updateNotification("response arrived", false);

                    responseArea.setText(responseMessage.toString());
                }else{
                    responseArea.setBackground(Color.RED);
                    responseArea.setText("NO RESPONSE");
                }

            }
        });

        this.retriveButton = new JButton("Retrive Resource");
        this.retriveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String resource = JOptionPane.showInputDialog(null, "Insert resource ID: ", "Retrieve Resource", JOptionPane.INFORMATION_MESSAGE);

                if(resource == null) {
                    return;
                }

                if(!SystemUtilities.isValidResourceID(resource)){
                    updateLog(settings.clientInfoString("invalid resource ID"));
                    updateNotification("invalid resource id", true);
                    return ;
                }


                updateLog(settings.clientInfoString("building a new request ..."));
                RequestMessage requestMessage = controller.buildRequest(resource, false);
                updateLog(settings.clientInfoString("request built: " + requestMessage.conciseToString()));
                updateNotification("request send", false);
                updateLog(settings.clientInfoString("new request created, trying to send the request to server: " + settings.getReferenceServerIP() + ":" + settings.getReferenceServerPort()));
                requestArea.setText(requestMessage.toString());

                ResponseMessage responseMessage = null;
                try {
                    responseMessage = controller.sendRequest(requestMessage);
                } catch (SocketManagerException e) {
                    updateNotification("Unable to send the request " + e.getMessage(), true);
                }

                if(responseMessage != null){
                    updateLog(settings.clientInfoString("response arrived from server: " + settings.getReferenceServerIP() + ":" + settings.getReferenceServerPort() + " > " + responseMessage.conciseToString()));
                    updateNotification("response arrived", false);

                    responseArea.setBackground(Color.WHITE);
                    responseArea.setText(responseMessage.toString());
                }else {
                    responseArea.setBackground(Color.RED);
                    responseArea.setText("NO RESPONSE");
                }
            }
        });

        this.menuPanel.add(this.depositButton);
        this.menuPanel.add(this.retriveButton);
    }

    /**
     * Method used for setting the notification panel
     */
    private void initNotificationPanel(){
        if(this.notificationPanel != null){
            return ;
        }

        this.notificationPanel = new JPanel();
        this.notificationPanel.setLayout(new BorderLayout());

        this.notificationTextField = new JTextField();
        this.notificationTextField.setEditable(false);
        this.notificationTextField.setBackground(Color.LIGHT_GRAY);

        this.tacitButton = new JButton("Tacit");
        this.tacitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                notificationTextField.setText(" ");
                notificationTextField.setBackground(Color.LIGHT_GRAY);
            }
        });

        this.notificationPanel.add(this.notificationTextField, BorderLayout.CENTER);
        this.notificationPanel.add(this.tacitButton, BorderLayout.EAST);
    }

    /**
     * Method used for updating the log
     * @param log
     */
    public void updateLog(String log){
        String text = this.logArea.getText();

        text += "\n" + log;

        this.logArea.setText(text);
    }

    /**
     * Method used for updating the notification Area
     * @param message
     * @param error
     */
    public void updateNotification(String message,boolean error){
        this.notificationTextField.setText(message);

        if(!error){
            this.notificationTextField.setBackground(Color.GREEN);
        }else{
            this.notificationTextField.setBackground(Color.RED);
        }
    }
}
