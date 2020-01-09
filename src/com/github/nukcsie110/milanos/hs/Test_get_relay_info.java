package com.github.nukcsie110.milanos.hs;

import com.github.nukcsie110.milanos.common.RelayInfo;
import org.bouncycastle.jce.interfaces.ECPublicKey;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;

public class Test_get_relay_info {
    public static void main(String arg[]) throws IOException, ClassNotFoundException {
        ArrayList<RelayInfo> relayInfos;

        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), 8500);
        client.connect(isa, 10000);

        DataOutputStream cmd = new DataOutputStream(client.getOutputStream());
        cmd.writeByte(0x00); //Get
        ObjectInputStream objectInputStream =
                new ObjectInputStream(client.getInputStream());
        relayInfos = (ArrayList<RelayInfo>) objectInputStream.readObject();
        for(RelayInfo i:relayInfos){
            System.out.println(i);
        }
        objectInputStream.close();

    }
}
