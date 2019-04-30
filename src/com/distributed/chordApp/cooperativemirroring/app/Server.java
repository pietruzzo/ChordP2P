package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.Host;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;

public class Server {
    public static void main(String []args){
        String serverIP = "192.168.137.199";
        Integer chordPort = 7654;
        Integer serverPort = 9999;



        ChordNetworkSettings chs = new ChordNetworkSettings(chordPort);
        HostSettings hs = new HostSettings(serverIP, serverPort, chs, true);

        chs.setPerformBasicLookups(false);
        chs.setBootstrapServerAddress("192.168.137.199");
        chs.setJoinExistingChordNetwork(false);
        chs.lockChanges();

        Thread t1 = null;

        Host host = new Host(hs, null);

        t1 = new Thread(host);

        t1.start();

    }
}
