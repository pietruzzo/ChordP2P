package com.distributed.chordApp.cooperativemirroring.utilities.consoleInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ExternalConsole {

    public static void main(String[] args) {


        try {
            ServerSocket ss = new ServerSocket(7755);
            Socket process = ss.accept();

            ObjectInputStream in = new ObjectInputStream(process.getInputStream());

            while(true){
                OutputMessage message = (OutputMessage) in.readObject();
                if (message.isError == true) System.err.println(message.message);
                else System.out.println(message.message);
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
