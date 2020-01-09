package com.github.nukcsie110.milanos.hs;

import com.github.nukcsie110.milanos.common.RelayInfo;
import org.bouncycastle.jce.interfaces.ECPublicKey;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;

public class Test_send_relay_info {
    public static void main(String arg[]) throws IOException, NoSuchProviderException, NoSuchAlgorithmException {
        //Generate key
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(256, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //Create relay info
        RelayInfo relayInfo = new RelayInfo();
        relayInfo.address = new InetSocketAddress(InetAddress.getLocalHost(), 8787);
        relayInfo.publicKey = (ECPublicKey) keyPair.getPublic();

        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), 8500);
        client.connect(isa, 10000);

        DataOutputStream cmd = new DataOutputStream(client.getOutputStream());
        cmd.writeByte(0x01); //Post
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectOutputStream.writeObject(relayInfo);
        objectOutputStream.close();

    }
}
