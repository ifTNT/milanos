package com.github.nukcsie110.milanos.crypto;

import java.nio.ByteBuffer;

public interface CryptoBuffer {
    ByteBuffer encryptedBuffer;
    ByteBuffer decryptedBuffer;
    byte[] SEK
}
