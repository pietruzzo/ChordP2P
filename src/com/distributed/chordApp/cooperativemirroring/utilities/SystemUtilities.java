package com.distributed.chordApp.cooperativemirroring.utilities;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
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
     *  This method uses the System commands for retriving the IP address of the current machine
     * @return
     */
    public static synchronized String getThisMachineIPSystemCommand()
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

    /**
     * Method used for automatically retriving the current host IP for the LAN
     * This method uses the Java method for retriving the IP address of the current machine
     * @return
     */
    public static synchronized String getThisMachineIP()
    {
        String result = null;

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements())
            {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while(nias.hasMoreElements())
                {
                    InetAddress ia= nias.nextElement();

                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address)
                    {
                        String tmp = ia.getHostAddress();

                        Pattern pt = Pattern.compile("192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}");
                        Matcher mt = pt.matcher(tmp);
                        Boolean found = mt.find();

                        if(found) {
                            result = tmp;
                        }

                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Unable to retrive the current host IP, using the 127.0.0.1 instead");
        }

        if(result == null)
        {
           result = "127.0.0.1";
        }

        return result;
    }
}
