package com.distributed.chordLib.chordCore;


import java.io.Serializable;

public class Node implements Serializable {

    private final String IP;
    private final String key;

    public Node(String IP, String key) {
        this.IP = IP;
        this.key = key;
    }

    public String getIP(){return IP;}

    /**
     * @return the hash for the IP
     */
    public String getkey(){return key;}

    /**
     * Find if nodes are equals
     * @param node
     * @return comparison response over node's key
     */
    @Override
    public boolean equals(Object node){
        if (node instanceof Node) {
            if (this.IP == ((Node) node).getIP() && this.key == ((Node) node).getkey()) return true;
        }
        return false;
    }
}
