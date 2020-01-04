package com.github.nukcsie110.milanos.relay;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;

public class KeyGenerator {

    private static final String seed = "abcdefghigklmnopkrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
    private byte[] publicKey;
    private byte[] privateKey;

    public KeyGenerator(){
        generateKeys();
    }

    public byte[] getPublicKey(){
        return publicKey;
    }

    public byte[] getPrivateKey(){
        return privateKey;
    }

    private String generateSeed(){
        Random random = new Random();
        StringBuffer sf = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            int number = random.nextInt(62);
            sf.append(seed.charAt(number));
        }
        return sf.toString();
    }

    private void generateKeys(){
        try {
            //String for encryption
            String pk = generateSeed();
            String prik = generateSeed();

            String key1 = generateSeed();
            String key2 = generateSeed();

            byte[] keyP = key1.getBytes();
            byte[] keyPri = key2.getBytes();
            //Public key
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyP, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            publicKey = cipher.doFinal(pk.getBytes());
            //Private key
            SecretKeySpec secretPriKeySpec = new SecretKeySpec(keyPri, "Blowfish");
            Cipher cipherPri = Cipher.getInstance("Blowfish");
            cipherPri.init(Cipher.ENCRYPT_MODE,secretPriKeySpec);
            privateKey = cipher.doFinal(prik.getBytes());
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
