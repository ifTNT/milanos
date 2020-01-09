package com.github.nukcsie110.milanos.entrypoint;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;

public class KeyGenerator {

    private static final String seed = "abcdefghigklmnopkrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
    private byte[] Key;

    public KeyGenerator(){
        generateKeys();
    }

    public byte[] getKey(){
        return Key;
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
            String key1 = generateSeed();
            byte[] keyP = key1.getBytes();
            //key
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyP, "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            Key = cipher.doFinal(pk.getBytes());
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
