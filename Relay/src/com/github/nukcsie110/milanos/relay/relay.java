package com.github.nukcsie110.milanos.relay;

import java.nio.*;
import java.util.Random;

public class relay {

    public byte[] myPublicKey;
    private byte[] myPrivateKey;
    private byte[] mySEKey;

    public relay() {
        KeyGenerator keySets = new KeyGenerator();
        myPublicKey = keySets.getPublicKey();
        myPrivateKey = keySets.getPrivateKey();
    }

    public static void main(String arg[]){
        System.out.println("I'm relay");
    }

    private void forwarding(byte[] data){

    }

    private void heartBeat(byte[] myPK,byte[] myPriK){

    }
}
