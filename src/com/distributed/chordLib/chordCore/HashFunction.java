package com.distributed.chordLib.chordCore;

public class HashFunction {

    /**
     * Module
     */
    private int m;

    public HashFunction(int m){
        this.m = m;
    }
    /**
     * calculate SHA-1 of a String
     * @implNote with org.apache.commons.codec.digest.DigestUtils
     * @return digested string
     */
    public String getSHA1(String inputString){return null;}

    /**
     * Return 1 if hash1 > hash2, -1 if hash1 < hash2, 0 otherwise
     *
     * @param hash1
     * @param hash2
     * @return
     */
    public int compare (String hash1, String hash2){
        return hash1.compareTo(hash2);
    }

    /**
     * return true if hash2 is inside range [in module] (hash1, hash3) extreme excluded
     */
    public boolean areOrdered(String hash1, String hash2, String hash3){
        if (compare(hash3, hash1) == 1 && compare(hash2, hash1) == 1 && compare(hash2, hash1)==1) return true;
        if (compare(hash3, hash1) == -1){
            if (compare(hash2, hash3) == 1) return true;
            if (compare(hash1, hash2) == 1) return true;
        }
        return false;
    }

    public int getM() {
        return m;
    }

}
