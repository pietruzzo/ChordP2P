package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.Host;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordApp.cooperativemirroring.core.settings.exceptions.ChordNetworkSettingsException;
import com.distributed.chordApp.cooperativemirroring.core.settings.exceptions.HostSettingException;
import com.distributed.chordApp.cooperativemirroring.utilities.ChordSettingsLoader;
import com.distributed.chordApp.cooperativemirroring.utilities.SystemUtilities;
import com.distributed.chordLib.Chord;
import com.intellij.jarRepository.services.artifactory.Endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Server {
    public static void main(String []args)
    {
        String serverIP = SystemUtilities.getThisMachineIP();
        Integer serverPort = ChordSettingsLoader.getApplicationServerPort();

        Integer chordPort = ChordSettingsLoader.getChordPort();
        String bootstrapServerIP = ChordSettingsLoader.getBootstrapServerIP();

        boolean joinAChordNetwork = false;

        Process outputConsole;
        ObjectOutputStream consoleChannel = null;

        /*
         * Here we are checking if we are the bootstrap server for te current application,
         * in such case we don't have to to join a chord network but rather to create ane
         */
        if(serverIP.equals(bootstrapServerIP))
        {
            joinAChordNetwork = false;
        }
        //Otherwise it depends on the settings but , probably we have to join an existing chord network
        else
        {
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

        //Open output console
        try {
            outputConsole = new ProcessBuilder("com.distributed.chordApp.cooperativemirroring.utilities.consoleInterface.ExternalConsole", null).start();
            consoleChannel = new ObjectOutputStream((new Socket("127.0.0.1", 7755)).getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Here we are setting the HostSetting for the current host
        HostSettings hs = null;
        try {
            hs = new HostSettings.HostSettingsBuilder()
                    .setHostIP(serverIP)
                    .setHostPort(serverPort)
                    .setChordNetworkSetting(chs)
                    .setVerboseOperatingMode(true)
                    .setShallopHostIP(ChordSettingsLoader.getBootstrapServerIP())
                    .setShallopHostPort(ChordSettingsLoader.getApplicationServerPort())
                    .setConnectionRetries(5)
                    .setConnectionTimeout_ms(3000)
                    .build();
        } catch (HostSettingException e)
        {
            System.err.println("[Server> " + e + "\nShutting down the server.");
            System.exit(1);
        }

        Thread t1;

        Host host = new Host(hs, null);

        t1 = new Thread(host);

        t1.start();

        host.enjoyChordNetwork();

    }
}
