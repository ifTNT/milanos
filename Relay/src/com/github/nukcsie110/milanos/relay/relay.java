package com.github.nukcsie110.milanos.relay;

import java.nio.*;
import java.util.Random;

public class relay {

    public byte[] myPublicKey;
    private byte[] myPrivateKey;
    private byte[] mySEKey;
    private static final String seed = "abcdefghigklmnopkrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";

    public relay() {
        String key = generateSeed();
        
    }

    private String generateSeed(){
        Random random = new Random();
        StringBuffer sf = new StringBuffer();
        for (int i = 0; i < 15; i++) {
            int number = random.nextInt(62);
            sf.append(seed.charAt(number));
        }
        return sf.toString();
    }

    public static void main(String arg[]){
        System.out.println("I'm relay");
    }

    private void forwarding(byte[] data){

    }

    private void heartBeat(byte[] myPK,byte[] myPriK){

    }
}
