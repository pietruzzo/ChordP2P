package com.distributed.chordApp.cooperativemirroring.utilities.consoleInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ExternalConsole {

    public static void main(String[] args) {


        String[] lastOptions = null;
        String[] lastMessage = null;

        try {
            ServerSocket ss = new ServerSocket(7755);
            Socket process = ss.accept();

            ObjectInputStream in = new ObjectInputStream(process.getInputStream());

            while(true){
                OutputMessage message = (OutputMessage) in.readObject();

                //Separate message lines
                if (message.message != null){
                    lastMessage = message.message.split("/");
                }


                if (message.messageOptions == OutputMessage.MessageOptions.ISERROR){
                    printToConsole(lastMessage, true);
                }

                else if(message.messageOptions == OutputMessage.MessageOptions.ISMESSAGE) printToConsole(lastMessage, false);

                else if (message.messageOptions == OutputMessage.MessageOptions.ISOPTION) {
                    lastOptions = lastMessage;
                }

                printToConsole(lastOptions, false);
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void printToConsole(String[] message, boolean isError){
        for (String s: message) {
            if (isError) System.err.println(s);
            else System.out.println(s);
        }
    }

}
