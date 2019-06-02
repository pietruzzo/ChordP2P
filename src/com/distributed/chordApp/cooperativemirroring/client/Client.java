package com.distributed.chordApp.cooperativemirroring.client;

import com.distributed.chordApp.cooperativemirroring.client.controller.ClientController;
import com.distributed.chordApp.cooperativemirroring.client.utilities.ClientException;
import com.distributed.chordApp.cooperativemirroring.client.utilities.ClientSettings;
import com.distributed.chordApp.cooperativemirroring.client.view.ClientGUI;
import com.distributed.chordApp.cooperativemirroring.client.view.ClientTUI;
import com.distributed.chordApp.cooperativemirroring.server.utilities.ChordSettingsLoader;
import com.distributed.chordApp.cooperativemirroring.common.utilities.SystemUtilities;

import javax.swing.*;

/**
 * Class that is used for instantiate client objects for communicating with the host of
 * the cooperative mirroring application
 */
public class Client {

    public static void main(String []args) throws ClientException {
        String clientIP = SystemUtilities.getThisMachineIP();
        Integer clientPort = ChordSettingsLoader.getApplicationClientPort();
        String serverIP = ChordSettingsLoader.getBootstrapServerIP() ;
        Integer serverPort = ChordSettingsLoader.getApplicationServerPort();

        boolean gui = true;

        ClientSettings settings = null;
        try {
            settings = new ClientSettings.ClientSettingsBuilder()
                    .setClientIP(clientIP)
                    .setClientPort(clientPort)
                    .setServerIP(serverIP)
                    .setServerPort(serverPort)
                    .setGUIMode(gui)
                    .setVerbose(true)
                    .build();
        } catch (ClientException e) {
            if(gui){
                JOptionPane.showMessageDialog(null, e.getMessage(), "CLIENT ERROR", JOptionPane.ERROR_MESSAGE);
            }
            else{
                System.err.println(e.getMessage());
                throw e;
            }
        }

        ClientController controller = new ClientController(settings);

        if(settings.isGuiMode()){
            new ClientGUI(settings, controller);
        }else{
            new ClientTUI(settings, controller);
        }
    }

}
