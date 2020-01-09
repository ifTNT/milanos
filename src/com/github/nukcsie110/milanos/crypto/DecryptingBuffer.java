package com.github.nukcsie110.milanos.crypto;

import org.bouncycastle.jce.interfaces.ECPrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import java.util.Random;

public class DecryptingBuffer implements CryptoBuffer{
    ByteBuffer decryptedBuffer;
    byte[] SEK;
    Cipher sessionCipher;
    int _cap;
    int _pos;
    int _mark;
    int _limit;
    final static int VERSION = 1;
    final static byte SE_ALGO_ID = 0x01; //Blowfish

    //Decryption buffer for Relay
    //Will remove the crypto header from first data
    public DecryptingBuffer(ECPrivateKey sk, byte[] firstData) {
        try {

            //Initialize ByteBuffer
            int size = 10240;
            decryptedBuffer = ByteBuffer.allocate(size);
            this._cap = size;
            this._limit = size;
            this._mark = 0;
            this._pos = 0;

            // Extract SEK from header and valid the header
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
            ByteBuffer header = ByteBuffer.wrap(firstData);
            int _ver = header.getInt();
            if(_ver != VERSION) throw new cryptoHeaderException("Version mismatch: "+_ver+"!="+VERSION);
            short _space = header.getShort();
            if(_space != 0x00) throw new cryptoHeaderException("Format mismatch: space!=0x00");
            byte _se_algo_id = header.get();
            if(_se_algo_id != SE_ALGO_ID) throw new cryptoHeaderException("SEK algorithm ID mismatch: "+_se_algo_id+"!="+SE_ALGO_ID);
            int len_SEK = header.get() & 0xFF; //Get lenght of SEK and convert into unsigned integer.
            //System.out.println("Length of SEK: "+len_SEK);

            //Decrypt SEK
            byte[] encrytedSEK = new byte[len_SEK];
            header.get(encrytedSEK);
            Cipher headerCipher = Cipher.getInstance("ECIES", "BC");
            headerCipher.init(Cipher.DECRYPT_MODE, sk);
            SEK = headerCipher.doFinal(encrytedSEK);

            //Create the cipher instance for future encrypting
            SecretKeySpec KS = new SecretKeySpec(this.SEK, "Blowfish");
            this.sessionCipher = Cipher.getInstance("Blowfish");
            sessionCipher.init(Cipher.DECRYPT_MODE, KS);

            //Process rest of data
            if(header.hasRemaining()){
                byte[] data = new byte[header.remaining()];
                this.put(data);
            }

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Creating DecryptingBuffer Error!!");
            System.out.println("In Cipher.getInstance");
            e.printStackTrace();
        } catch  (InvalidKeyException  e) {
            System.out.println("Creating DecryptingBuffer Error!!");
            System.out.println("In cipher initialization");
            e.printStackTrace();
        } catch (cryptoHeaderException e) {
            System.out.println("Crypto header error");
            e.printStackTrace();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    //Decryption buffer for EntryPoint
    public DecryptingBuffer(byte[] _SEK) {
        try {

            //Initialize ByteBuffer
            int size = 10240;
            decryptedBuffer = ByteBuffer.allocate(size);
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
            System.out.println("Creating DecryptingBuffer Error!!");
            System.out.println("In Cipher.getInstance");
            e.printStackTrace();
        } catch  (InvalidKeyException  e) {
            System.out.println("Creating DecryptingBuffer Error!!");
            System.out.println("In cipher initialization");
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
        decryptedBuffer.clear();
        return this;
    }

    //Flips this buffer.
    public CryptoBuffer flip(){
        this._limit = this._pos;
        this._pos = 0;
        decryptedBuffer.flip();
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
        return decryptedBuffer.get();
    }

    //Relative bulk get method.
    public CryptoBuffer get(byte[] dst){
        this._pos += dst.length;
        decryptedBuffer.get(dst);
        return this;
    }

    //Relative put method
    public CryptoBuffer put(byte b){
        byte[] block = new byte[1];
        block[0] = b;
        byte[] decryptedData = sessionCipher.update(block);
        if(decryptedData!=null && decryptedData.length!=0) {
            assert decryptedData.length == sessionCipher.getBlockSize();
            byte[] data = new byte[decryptedData[0]]; //Byte 0 is length of data
            System.arraycopy(decryptedData, 1, data, 0, data.length);
            decryptedBuffer.put(data);
            this._pos+=data.length;
        }
        return this;
    }

    //Relative bulk put method
    public CryptoBuffer put(byte[] b){
        int len = b.length;
        int index = 0;
        int blockSize = sessionCipher.getBlockSize();
        int originalPos = decryptedBuffer.position();
        //Process blocks of data
        while(len-index > 1) {
            int blockLen = Math.min(blockSize, len-index);
            byte[] block = new byte[blockLen];
            System.arraycopy(b, index, block, 0, blockLen);
            byte[] decryptedData = sessionCipher.update(block);
            if(decryptedData!=null && decryptedData.length!=0) {
                int data_len = decryptedData[0] & 0xFF;
                assert data_len >= 0 && data_len <= blockSize - 1;
                byte[] data = new byte[data_len]; //Byte 0 is length of data
                System.arraycopy(decryptedData, 1, data, 0, data.length);
                decryptedBuffer.put(data);
            }
            index += blockLen;
        }
        this._pos += decryptedBuffer.position()-originalPos;
        return this;
    }

    //Must be call when stream ended
    public CryptoBuffer flush() {
        try {
            int blockSize = sessionCipher.getBlockSize();
            byte[] finalBlock = sessionCipher.doFinal();
            if(finalBlock!=null && finalBlock.length!=0){
                assert finalBlock.length == blockSize;
                byte[] decryptedData = sessionCipher.update(finalBlock);
                assert decryptedData[0] <= 7;
                byte[] data = new byte[decryptedData[0]]; //Byte 0 is length of data
                System.arraycopy(decryptedData, 1, data, 0, data.length);
                decryptedBuffer.put(data);
                this._pos += data.length;
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