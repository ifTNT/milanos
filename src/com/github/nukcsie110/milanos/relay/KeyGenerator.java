package com.github.nukcsie110.milanos.relay;


import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import com.github.nukcsie110.milanos.relay.*;

import javax.crypto.Cipher;

public class KeyGenerator {

    private ECPublicKey pk;
    private ECPrivateKey prik;

    public KeyGenerator(){
        KeyPair key = KeyPair();
        String pK = PublicKey(key);
        String priK = PrivateKey(key);
        pk = string2PublicKey(pK);
        prik = string2PrivateKey(priK);
    }

    public static KeyPair KeyPair(){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(256, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String PublicKey(KeyPair keyPair){
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        return AESUtil.byte2Base64(bytes);
    }

    public static String PrivateKey(KeyPair keyPair){
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return AESUtil.byte2Base64(bytes);
    }

    public static ECPublicKey string2PublicKey(String pubStr){
        try {
            byte[] keyBytes = AESUtil.base642Byte(pubStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(keySpec);
            return publicKey;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static ECPrivateKey string2PrivateKey(String priStr){
        try {
            byte[] keyBytes = AESUtil.base642Byte(priStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);
            return privateKey;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ECPublicKey getPublicKey() {
        return pk;
    }

    public ECPrivateKey getPrivateKey() {
        return prik;
    }
}