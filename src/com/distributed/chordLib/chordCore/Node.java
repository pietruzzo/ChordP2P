package com.distributed.chordLib.chordCore;


import java.io.Serializable;

public class Node implements Serializable {

    private String IP;


    public Node(String IP) {
        this.IP = IP;
    }

    public String getIP(){return IP;}

    /**
     * @return the hash for the IP
     */
    public String getkey(){return null;}
}
