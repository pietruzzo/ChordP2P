package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.Host;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;
import com.distributed.chordApp.cooperativemirroring.core.settings.HostSettings;

import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    public static void main(String []args){
        String serverIP = "127.0.0.1";
        Integer chordPort = 7654;
        Integer serverPort = 9999;

        ChordNetworkSettings chs = new ChordNetworkSettings(chordPort);
        HostSettings hs = new HostSettings(serverIP, serverPort, chs, true);

        chs.setPerformBasicLookups(false);
        chs.setJoinExistingChordNetwork(false);
        //chs.setBootstrapServerAddress(serverIP);
        //chs.setNumberOfFingers(2);
        //chs.setNumberOfSuccessors(2);
        //chs.setChordModule(2);
        chs.lockChanges();

        Thread t1 = null;

        //ThreadPoolExecutor executor = null;

        Host host = new Host(hs, null);

        t1 = new Thread(host);

        t1.start();

        //executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        //executor.execute(host);

        //executor.shutdown();

    }
}