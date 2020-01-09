package com.github.nukcsie110.milanos.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.ByteBuffer;

public class main {
    protected ByteBuffer encryptedData;
    protected ByteBuffer decrypytdData;
    protected byte[] SEK;
    protected Cipher cypher;
    public static void main(String arg[]){

        System.out.println("I'm crypto");
    }
}