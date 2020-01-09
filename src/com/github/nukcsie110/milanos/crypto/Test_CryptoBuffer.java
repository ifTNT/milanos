package com.github.nukcsie110.milanos.crypto;

import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.ECPrivateKey;

import java.util.Arrays;
import java.security.*;
import java.util.Random;

public class Test_CryptoBuffer {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static void main(String args[]) throws NoSuchProviderException, NoSuchAlgorithmException {

        //Generate PK
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(256, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //Generate SEK
        byte[] SEK = new byte[256];
        Random rand = new Random();
        rand.nextBytes(SEK);

        EncryptingBuffer eb = new EncryptingBuffer((ECPublicKey)keyPair.getPublic(), SEK);

        eb.flip();
        assert eb.hasRemaining();

        int len = eb.remaining();
        System.out.println("Length of header: " + len);
        byte[] header = new byte[len];
        eb.get(header);
        System.out.println("Header: " + bytesToHex(header));

        byte[] plaintext = new byte[100];
        rand.nextBytes(plaintext);
        System.out.println("Length of plaintext: "+plaintext.length);
        System.out.println("Plaintext: "+ bytesToHex(plaintext));

        eb.clear();

        eb.put(plaintext);
        eb.flush();

        eb.flip();
        assert eb.hasRemaining();

        len = eb.remaining();
        System.out.println("Length of encrypted data: " + len);
        byte[] cipher = new byte[len];
        eb.get(cipher);
        System.out.println("Encrypted data: " + bytesToHex(cipher));

        //===================Decrypting=====================

        DecryptingBuffer db = new DecryptingBuffer((ECPrivateKey) keyPair.getPrivate(), header);

        assert !db.hasRemaining();

        db.put(cipher);
        db.flush();

        db.flip();
        assert db.hasRemaining();

        len = db.remaining();
        System.out.println("Length of decrypted data: " + len);
        byte[] decrypted = new byte[len];
        db.get(decrypted);
        System.out.println("Encrypted data: " + bytesToHex(decrypted));

        System.out.println("Is decrypted correct: "+ Arrays.equals(plaintext, decrypted));

    }
}
