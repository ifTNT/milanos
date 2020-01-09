package com.github.nukcsie110.milanos.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.github.nukcsie110.milanos.relay.relay;
import org.bouncycastle.jce.interfaces.ECPublicKey;

public class RelayInfo implements Serializable{
    public InetSocketAddress address;
    ECPublicKey publicKey;
    String UUID;
    public void main(String[] arg) throws Exception {
        address = new InetSocketAddress(8592);
//        relay r2 = new relay();
//        relay r3 = new relay();
    }
}
