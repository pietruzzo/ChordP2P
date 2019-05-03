package com.distributed.chordApp.cooperativemirroring.utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used for getting some utilities of the System in order
 * to automathically perform some critical operations
 */
public class SystemUtilities {

    /**
     * Method used for retriving the current machine's IP in an automatic way
     * @return
     */
    public static synchronized String getThisMachineIP()
    {
        String currentIP = null;

        String command = null;
        if(System.getProperty("os.name").equals("Linux"))
            command = "ifconfig";
        else
            command = "ipconfig";
        Runtime r = Runtime.getRuntime();
        Process p = null;
        try {
            p = r.exec(command);
        } catch (IOException e) {
            System.err.println("\nCannot execute the ipconfig command");
            e.printStackTrace();
        }
        Scanner s = new Scanner(p.getInputStream());

        StringBuilder sb = new StringBuilder("");
        while(s.hasNext())
        {
            sb.append(s.next());
        }

        String ipconfig = sb.toString();
        Pattern pt = Pattern.compile("192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}");
        Matcher mt = pt.matcher(ipconfig);
        Boolean found = mt.find();

        if(!found)
        {
            currentIP = "127.0.0.1";
        }
        else
        {
            currentIP = mt.group();
        }


        return currentIP;
    }
}
