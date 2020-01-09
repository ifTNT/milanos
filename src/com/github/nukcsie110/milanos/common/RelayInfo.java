package com.github.nukcsie110.milanos.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.github.nukcsie110.milanos.relay.relay;
import org.bouncycastle.jce.interfaces.ECPublicKey;

public class RelayInfo implements Serializable{
    public InetSocketAddress address;
    public ECPublicKey publicKey;
    public String UUID;
    public String toString(){
        return address.toString()+" "+publicKey.toString();
    }
}
