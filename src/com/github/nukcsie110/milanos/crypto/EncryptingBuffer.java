package com.github.nukcsie110.milanos.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

import org.bouncycastle.jce.interfaces.ECPublicKey;

import java.lang.Math;
import java.util.Random;

public class EncryptingBuffer implements CryptoBuffer{
    ByteBuffer encryptedBuffer;
    byte[] SEK;
    Cipher sessionCipher;
    int _cap;
    int _pos;
    int _mark;
    int _limit;
    final static int VERSION = 1;
    final static byte SE_ALGO_ID = 0x01; //Blowfish

    //Encryption buffer for Relay
    public EncryptingBuffer(byte[] _SEK) {
        try {

            //Initialize ByteBuffer
            int size = 10240;
            encryptedBuffer = ByteBuffer.allocate(size);
            this._cap = size;
            this._limit = size;
            this._mark = 0;
            this._pos = 0;

            //Set SEK
            if(SE_ALGO_ID==0x01) { //SEK of blowfish
                this.SEK = Arrays.copyOf(_SEK, Math.min(_SEK.length, 448 / 8));
            }else if(SE_ALGO_ID==0x02){ //SEK of AES-CBC
                if(_SEK.length < 128/8){
                    System.out.println("SEK must be at least 128 bits");
                }
                this.SEK = Arrays.copyOf(_SEK, 128 / 8);
            }

            //Create the cipher instance for future encrypting
            SecretKeySpec KS = new SecretKeySpec(this.SEK, "Blowfish");
            this.sessionCipher = Cipher.getInstance("Blowfish");
            sessionCipher.init(Cipher.ENCRYPT_MODE, KS);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            System.out.println("Creating EncryptingBuffer Error!!");
            System.out.println("In Cipher.getInstance");
            e.printStackTrace();
        } catch  (InvalidKeyException  e) {
            System.out.println("Creating EncryptingBuffer Error!!");
            System.out.println("In cipher initialization");
            e.printStackTrace();
        }
    }

    //Encryption buffer for EntryPoint
    //Will automatic insert encrypting header in front of data
    public EncryptingBuffer(ECPublicKey pk, byte[] _SEK) {
        try {

            //Initialize ByteBuffer
            int size = 10240;
            encryptedBuffer = ByteBuffer.allocate(size);
            this._cap = size;
            this._limit = size;
            this._mark = 0;
            this._pos = 0;

            //Set SEK
            if(SE_ALGO_ID==0x01) { //SEK of blowfish
                this.SEK = Arrays.copyOf(_SEK, Math.min(_SEK.length, 448 / 8));
            }else if(SE_ALGO_ID==0x02){ //SEK of AES-CBC
                if(_SEK.length < 128/8){
                    System.out.println("SEK must be at least 128 bits");
                }
                this.SEK = Arrays.copyOf(_SEK, 128 / 8);
            }

            //Make a SEK encrypted with public-key
            Cipher headerCipher = Cipher.getInstance("ECIES", "BC");
            headerCipher.init(Cipher.ENCRYPT_MODE, pk);
            byte[] encryptedSEK = headerCipher.doFinal(this.SEK);

            //   0                   1                   2                   3
            //   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
            //  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
            //  |                           Version (32)                        |
            //  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
            //  |              0x00             | SE algo ID (8)|  SEK Len (8)  |
            //  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
            //  |                   Stream encryption key (0..2040)             |
            //  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
            //  |                        Encrypted Payload                    ...
            //  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
            //Make header
            ByteBuffer header = ByteBuffer.allocate(1024);
            header.putInt(VERSION);
            header.putShort((short) 0); //Two bytes of space
            header.put(SE_ALGO_ID);
            header.put(new Integer(encryptedSEK.length).byteValue());
            header.put(encryptedSEK);
            header.flip();

            this._pos += header.remaining();
            this.encryptedBuffer.put(header);

            //Create the cipher instance for future encrypting
            SecretKeySpec KS = new SecretKeySpec(this.SEK, "Blowfish");
            this.sessionCipher = Cipher.getInstance("Blowfish");
            sessionCipher.init(Cipher.ENCRYPT_MODE, KS);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Creating EncryptingBuffer Error!!");
            System.out.println("In Cipher.getInstance");
            e.printStackTrace();
        } catch  (InvalidKeyException  e) {
            System.out.println("Creating EncryptingBuffer Error!!");
            System.out.println("In cipher initialization");
            e.printStackTrace();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            System.out.println("Creating EncryptingBuffer Error!!");
            System.out.println("In doFinal");
            e.printStackTrace();
        }
    }

    //-------------Methods of mark operation---------------
    //Returns this buffer's capacity.
    public int capacity(){
        return _cap;
    }

    //Clears this buffer.
    public CryptoBuffer clear(){
        this._pos = 0;
        this._limit = this._cap;
        encryptedBuffer.clear();
        return this;
    }

    //Flips this buffer.
    public CryptoBuffer flip(){
        this._limit = this._pos;
        this._pos = 0;
        encryptedBuffer.flip();
        return this;
    }

    //Tells whether there are any elements between the current position and the limit.
    public boolean hasRemaining(){
        return _pos<_limit;
    }

    //Returns this buffer's limit.
    public int limit(){
        return _limit;
    }

    //Sets this buffer's limit.
    public CryptoBuffer limit(int newLimit){
        this._limit = newLimit;
        return this;
    }

    //Sets this buffer's mark at its position.
    public CryptoBuffer mark(){
        this._mark = this._pos;
        return this;
    }

    //Returns this buffer's position.
    public int position(){
        return this._pos;
    }

    //Sets this buffer's position.
    public CryptoBuffer position(int newPosition){
        this._pos = newPosition;
        return this;
    }

    //Returns the number of elements between the current position and the limit
    public int remaining(){
        return _limit-_pos;
    }

    //Resets this buffer's position to the previously-marked position.
    public CryptoBuffer reset(){
        this._pos = this._mark;
        return this;
    }

    //Rewinds this buffer.
    public CryptoBuffer rewind(){
        this._pos = 0;
        return this;
    }
    //-------------End methods of mark operation-------------

    //-------------Methods of content operation--------------

    //Relative get method.
    public byte get(){
        this._pos++;
        return encryptedBuffer.get();
    }

    //Relative bulk get method.
    public CryptoBuffer get(byte[] dst){
        this._pos += dst.length;
        encryptedBuffer.get(dst);
        return this;
    }

    //Relative put method
    //Inefficient. Not suggested.
    public CryptoBuffer put(byte b){
        byte[] block = new byte[8];
        block[0] = 0x01; //Length of block
        block[1] = b; //Data
        encryptedBuffer.put(sessionCipher.update(block));
        this._pos++;
        return this;
    }

    //Relative bulk put method
    public CryptoBuffer put(byte[] b){
        int len = b.length;
        int index = 0;
        int blockSize = sessionCipher.getBlockSize();
        int originalPos = encryptedBuffer.position();
        //Process blocks of data
        while(len-index >= blockSize-1) {
            byte[] block = new byte[blockSize];
            block[0] = new Integer(blockSize-1).byteValue(); //Length of block
            System.arraycopy(b, index, block, 1, blockSize-1); //Data
            byte[] encryptedBlock = sessionCipher.update(block);
            assert encryptedBlock.length==blockSize;
            encryptedBuffer.put(encryptedBlock);
            index += blockSize-1;
        }

        //Process rest of data
        if(len-index > 1){
            byte[] block = new byte[blockSize];
            block[0] = new Integer(len-index).byteValue(); //Length of block
            System.arraycopy(b, index, block, 1, len-index); //Data
            byte[] encryptedBlock = sessionCipher.update(block);
            assert encryptedBlock.length==blockSize;
            encryptedBuffer.put(encryptedBlock);
        }
        this._pos += encryptedBuffer.position()-originalPos;
        return this;
    }

    //Must be call when stream ended
    public CryptoBuffer flush() {
        try {
            byte[] finalBlock = sessionCipher.doFinal();
            if(finalBlock!=null && finalBlock.length!=0){
                this.encryptedBuffer.put(finalBlock);
                this._pos += finalBlock.length;
            }
        }catch(BadPaddingException | IllegalBlockSizeException e){
            System.out.println("Error while flushing EncryptingBuffer");
            e.printStackTrace();
        }
        return this;
    }

    //Relative bulk put method
    public CryptoBuffer put(ByteBuffer src){
        byte[] data = new byte[src.remaining()];
        System.out.println(src.remaining());
        this.put(data);
        return this;
    }

    //------ ----End methods of content operation------------

}