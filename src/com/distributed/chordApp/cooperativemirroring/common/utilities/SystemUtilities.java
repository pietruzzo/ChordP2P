package com.distributed.chordApp.cooperativemirroring.common.utilities;

import com.distributed.chordApp.cooperativemirroring.common.Resource;

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
     * Method used to decree if a specific port is a valid one
     * @param port
     * @return
     */
    public static synchronized boolean isValidPort(Integer port){
        if((port == null) || (port < 1)){
            return  false;
        }

        return true;
    }

    /**
     * Method used for decree if a certain IP is a valid IP for our application
     * @param ip
     * @return
     */
    public static synchronized Boolean isValidIP(String ip)
    {
        if((ip == null) || (ip.isEmpty())){
            return false;
        }

        Pattern pt = Pattern.compile("^192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}$");
        Matcher mt = pt.matcher(ip);
        Boolean found = mt.find();

        if(!found){
            if(ip.equals("127.0.0.1") || (ip.equals("127.0.1.1")) || (ip.equals("localhost"))){
                found = true;
            }
            else{
                found = false;
            }
        }

        return found;
    }

    /**
     * Method used for testing if the ID associated to the resource is a valid one or not
     * @param id
     * @return
     */
    public static boolean isValidResourceID(String id){
        if((id == null) || (id.isEmpty()) || (id.isBlank())){
            return false;
        }

        return true;
    }

    /**
     * Method used for checking is a passed resource is a valid one or not.
     * @param resource
     * @return
     */
    public static boolean isValidResource(Resource resource){
        if(resource == null){
            return false;
        }

        return true;
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
