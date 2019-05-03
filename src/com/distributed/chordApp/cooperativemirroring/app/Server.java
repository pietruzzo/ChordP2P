package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.Host;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;
import com.distributed.chordApp.cooperativemirroring.utilities.ChordSettingsLoader;
import com.distributed.chordApp.cooperativemirroring.utilities.SystemUtilities;

public class Server {
    public static void main(String []args){
        String serverIP = SystemUtilities.getThisMachineIP();
        Integer serverPort = ChordSettingsLoader.getApplicationServerPort();

        Integer chordPort = ChordSettingsLoader.getChordPort();

        Boolean isBootstrapServer = false;
        String bootstrapServerIP = ChordSettingsLoader.getBootstrapServerIP();

        if(serverIP.equals(bootstrapServerIP))
        {
            isBootstrapServer = true;
        }



        ChordNetworkSettings chs = new ChordNetworkSettings(chordPort);
        HostSettings hs = new HostSettings(serverIP, serverPort, chs, true);

        chs.setPerformBasicLookups(false);

        if(!isBootstrapServer)
        {
            chs.setBootstrapServerAddress(ChordSettingsLoader.getBootstrapServerIP());
            chs.setJoinExistingChordNetwork(ChordSettingsLoader.getJoinChordNetwork());
        }
        else
        {
            chs.setBootstrapServerAddress(serverIP);
            chs.setJoinExistingChordNetwork(false);
        }

        chs.lockChanges();

        Thread t1 = null;

        Host host = new Host(hs, null);

        t1 = new Thread(host);

        t1.start();

    }
}
