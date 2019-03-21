package com.distributed.chordLib.chordCore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashFunction {

    /**
     * Module
     */
    private int m;
    MessageDigest md;

    public HashFunction(int m){
        this.m = m;
        try {
            this.md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    /**
     * calculate SHA-1 of a String
     * @implNote with org.apache.commons.codec.digest.DigestUtils
     * @return digested string
     */
    public String getSHA1(String inputString){
        byte[] digest = md.digest(inputString.getBytes());
        digest =Arrays.copyOfRange(digest, digest.length-m, digest.length);
        return new String(digest);
    }

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
