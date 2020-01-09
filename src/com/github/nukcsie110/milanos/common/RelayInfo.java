package com.github.nukcsie110.milanos.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.bouncycastle.jce.interfaces.ECPublicKey;

public class RelayInfo implements Serializable{
    public InetSocketAddress address;
    ECPublicKey publicKey;
    String UUID;
}
