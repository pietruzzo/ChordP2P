package com.distributed.chordApp.cooperativemirroring.server;

import com.distributed.chordApp.cooperativemirroring.server.core.Host;
import com.distributed.chordApp.cooperativemirroring.common.utilities.exceptions.SocketManagerException;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.HostSettings;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.exceptions.ChordNetworkSettingsException;
import com.distributed.chordApp.cooperativemirroring.server.core.settings.exceptions.HostSettingException;
import com.distributed.chordApp.cooperativemirroring.server.utilities.ChordSettingsLoader;
import com.distributed.chordApp.cooperativemirroring.server.view.ServerView;
import com.distributed.chordApp.cooperativemirroring.common.utilities.SystemUtilities;
import com.distributed.chordLib.Chord;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
//import com.intellij.jarRepository.services.artifactory.Endpoint;

public class Server {
    private static AbstractMap.SimpleEntry<Host, Thread> hostThreadPair = null;

    public static void main(String []args) throws IOException {
        String serverIP = SystemUtilities.getThisMachineIP();
        Integer serverPort = ChordSettingsLoader.getApplicationServerPort();

        Integer chordPort = ChordSettingsLoader.getChordPort();
        String bootstrapServerIP = ChordSettingsLoader.getBootstrapServerIP();

        boolean joinAChordNetwork = false;

        /*
         * Here we are checking if we are the bootstrap server for te current application,
         * in such case we don't have to to join a chord network but rather to create ane
         */
        if(serverIP.equals(bootstrapServerIP)) {
            joinAChordNetwork = false;
        }
        //Otherwise it depends on the settings but , probably we have to join an existing chord network
        else {
            joinAChordNetwork = true;
        }


        //Here we are setting the ChordNetwork settings for the current host
        ChordNetworkSettings chs = null;
        try {
            chs = new ChordNetworkSettings.ChordNetworkSettingsBuilder().setAssociatedPort(chordPort)
                                                                        .setBootstrapServerAddress(bootstrapServerIP)
                                                                        .setJoinExistingChordNetwork(joinAChordNetwork)
                                                                        .setNumberOfFingers(Chord.DEFAULT_NUM_FINGERS)
                                                                        .setNumberOfSuccessors(Chord.DEFAULT_NUM_SUCCESSORS)
                                                                        .setChordModule(Chord.DEFAULT_CHORD_MODULE)
                                                                        .setPerformBasicLookup(false)
                                                                        .build();
        } catch (ChordNetworkSettingsException e) {
            System.err.println("[Server> " + e + "\nShutting down the server.");
            System.exit(1);
        }

        //Here we are setting the HostSetting for the current host
        HostSettings hs = null;
        ServerView logShellGUI = null;
        JTextArea textArea = null;

        if(ChordSettingsLoader.getVerboseOperatingMode() && ChordSettingsLoader.getEnableLogShellGUI()){
            textArea = ServerView.createLogArea();
        }

        try {

            hs = new HostSettings.HostSettingsBuilder()
                    .setHostIP(serverIP)
                    .setHostPort(serverPort)
                    .setChordNetworkSetting(chs)
                    .setVerboseOperatingMode(ChordSettingsLoader.getVerboseOperatingMode())
                    .setShallopHostIP(ChordSettingsLoader.getBootstrapServerIP())
                    .setShallopHostPort(ChordSettingsLoader.getApplicationServerPort())
                    .setShell(textArea)
                    .build();
        } catch (HostSettingException e) {
            System.err.println("[Server> " + e + "\nShutting down the server.");
            System.exit(1);
        }
        Host host = new Host(hs, null);

        Thread t1 = new Thread(host);
        t1.start();

        try {
            host.joinChordNetwork();
        } catch (IOException e) {
            System.err.println("\nUnable to open connection");
            t1.interrupt();
            System.exit(1);
        }

        hostThreadPair = new AbstractMap.SimpleEntry<>(host, t1);

        if(ChordSettingsLoader.getVerboseOperatingMode() && ChordSettingsLoader.getEnableLogShellGUI()){
          logShellGUI = new ServerView(Server.class.getSimpleName() + " @" + serverIP + ":" + serverPort, host, textArea);
        }
        else{
            serverConsole();
        }

        try {
            t1.join();

            if(ChordSettingsLoader.getVerboseOperatingMode() && ChordSettingsLoader.getEnableLogShellGUI()){
                logShellGUI.finalize();
            }else{
                System.exit(0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static String serverInfoString(String infoMessage)
    {
        String infoString = "[Server >";

        infoString += infoMessage;

        return infoString;
    }


    /*
     * Method used for allowing the client to perform some operations on a server
     */
    private static void serverConsole()
    {
        Boolean goAhead = true ;
        Integer choice = -1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        do {
            System.out.println("\n======{SERVER CONSOLE}======\n");
            System.out.println("0)Shut Down Server");
            System.out.print("[Choice> ");

            try {
                choice = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.out.println(serverInfoString("Invalid input choice, retry"));
                choice = -1;
                goAhead = true;
            }

            switch(choice)
            {
                case 0:
                    goAhead = false ;
                    try {
                        hostThreadPair.getKey().shutdownHost();
                    } catch (SocketManagerException e) {
                        System.err.println("\nUnable to close the server : " + e.getMessage());
                    }

                    break;
                default :
                    break;
            }

        }while(goAhead);

        System.out.println(serverInfoString("Exiting from the server, bye"));

    }

}
