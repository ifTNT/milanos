package com.github.nukcsie110.milanos.crypto;


import java.nio.ByteBuffer;

//Copied interface from java.nio.ByteBuffer
//For the capability with java.nio.ByteBuffer
public interface CryptoBuffer {

    //-------------Methods of mark operation---------------
    //Returns this buffer's capacity.
    int capacity();

    //Clears this buffer.
    CryptoBuffer clear();

    //Flips this buffer.
    CryptoBuffer flip();

    //Tells whether there are any elements between the current position and the limit.
    boolean hasRemaining();

    //Returns this buffer's limit.
    int limit();

    //Sets this buffer's limit.
    CryptoBuffer limit(int newLimit);

    //Sets this buffer's mark at its position.
    CryptoBuffer mark();

    //Returns this buffer's position.
    int position();

    //Sets this buffer's position.
    CryptoBuffer position(int newPosition);

    //Returns the number of elements between the current position and the limit
    int remaining();

    //Resets this buffer's position to the previously-marked position.
    CryptoBuffer reset();

    //Rewinds this buffer.
    CryptoBuffer rewind();
    //-------------End methods of mark operation-------------

    //-------------Methods of content operation--------------

    //Relative get method.
    byte get();

    //Relative bulk get method.
    CryptoBuffer get(byte[] dst);

    //Relative put method
    CryptoBuffer put(byte b);

    //Relative bulk put method
    CryptoBuffer put(byte[] b);

    //Relative bulk put method
    CryptoBuffer put(ByteBuffer b);

    //------ ----End methods of content operation------------

}
