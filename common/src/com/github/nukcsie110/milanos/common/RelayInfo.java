package com.github.nukcsie110.milanos.common;

import java.io.Serializable;
import java.net.InetAddress;
import org.bouncycastle.jce.interfaces.ECPublicKey;

public class RelayInfo implements Serializable{
    public InetAddress address;

    ECPublicKey publicKey;
    String UUID;
}
