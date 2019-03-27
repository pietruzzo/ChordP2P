package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.Host;
import com.distributed.chordApp.cooperativemirroring.core.settings.ChordNetworkSettings;

public class Server {
    public static void main(String []args){
        String serverIP = "127.0.0.1";
        Integer serverPort = 7654;
        //ChordNetworkSettings chs = new ChordNetworkSettings(serverIP, serverPort);

        //chs.setPerformBasicLookups(false);
        //chs.setJoinExistingChordNetwork(false);
        //chs.lockChanges();
        Thread t1 = null;

        //ThreadPoolExecutor executor = null;

        //Host host = new Host(serverIP, serverPort, chs, null, true);

        //t1 = new Thread(host);

        //t1.start();

        //executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        //executor.execute(host);

        //executor.shutdown();

    }
}
