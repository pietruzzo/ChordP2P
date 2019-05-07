package com.distributed.javaTests;

import com.distributed.chordLib.chordCore.HashFunction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Random;

import static com.distributed.chordLib.chordCore.HashFunction.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class HashFunctionTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(com.distributed.chordLib.chordCore.HashFunction.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    //region:Vars
    private HashFunction hashFunction;
    private int m;
    //endregion

    @Before
    public void setUp() throws Exception {
        hashFunction = new HashFunction(m);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Verify digest length for many generated strings
     * And verify some specific cases in hash generation
     */
    @Test
    public void getSHA1() {

        //Generates 100 random strings and verify digest length
        Random rg = new Random();
        int[] inputs = new int[100];
        for (int i = 0; i < inputs.length; i++) {
            Hash hash = hashFunction.getSHA1(String.valueOf(rg.nextInt()));
            inputs[i] = hash.getDigest().length;
        }
        int[] expectedValues = new int[100];
        Arrays.fill(expectedValues, m);
        assertArrayEquals(inputs, expectedValues);

        //Manually build some specific hashes
        byte[] digest;
        Hash hash;
        digest = new byte[1];
        digest[0] = 0;
        for (int i = 0; i < 16; i++) {
            digest[0] = 0;
            hash = new Hash(digest, m);
            boolean[] b = new boolean[1];
            b[0] = false; b[1] = false; b[2] = false; b[3] = false; b[4] = false; b[5] = false; b[6] = false; b[7] = false;
            assertEquals(hash.getDigest(), b);
        }

    }

    @Test
    public void areOrdered() {

        byte[] digest;

        digest = new byte[1];
        digest[0] = (byte)0x0;
        Hash h1 = new Hash(digest, m);

        digest = new byte[1];
        digest[0] = (byte)0x1;
        Hash h2 = new Hash(digest, m);

        digest = new byte[3];
        digest[0] = (byte)0xBA7;
        Hash h3 = new Hash(digest, m);


        //Verify order of 3 ordered hashes
        assertEquals(hashFunction.areOrdered(h1, h2, h3), true);
        assertEquals(hashFunction.areOrdered(h2, h3, h1), true);
        assertEquals(hashFunction.areOrdered(h3, h1, h2), true);
        assertEquals(hashFunction.areOrdered(h1, h3, h2), false);
        assertEquals(hashFunction.areOrdered(h2, h1, h3), false);
        assertEquals(hashFunction.areOrdered(h3, h2, h1), false);
    }

    @Test
    public void moduloSum() {
    }

    @Test
    public void getM() {
    }
}
