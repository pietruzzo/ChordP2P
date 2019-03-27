package com.distributed.chordApp.cooperativemirroring.app;

import com.distributed.chordApp.cooperativemirroring.core.Host;
import com.distributed.chordApp.cooperativemirroring.core.backend.ChordNetworkSettings;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    public static void main(String []args){
        String serverIP = "127.0.0.1";
        Integer serverPort = 7654;
        ChordNetworkSettings chs = new ChordNetworkSettings(serverIP, serverPort);

        chs.setPerformBasicLookups(false);
        chs.setJoinExistingChordNetwork(false);
        chs.lockChanges();

        ThreadPoolExecutor executor = null;



        try {
            Host host = new Host(serverIP, serverPort, chs, null);

            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

            executor.execute(host);

            System.out.println(host.toString());

            executor.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
