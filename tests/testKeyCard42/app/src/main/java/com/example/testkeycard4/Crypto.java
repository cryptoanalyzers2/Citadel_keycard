package com.example.testkeycard4;

import android.util.Base64;
import android.util.Log;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import im.status.keycard.applet.Mnemonic;

public class Crypto {


    static final byte TLV_KEY_TEMPLATE = (byte) 0xA1;
    static final byte TLV_PUB_KEY = (byte) 0x80;
    static final byte TLV_PRIV_KEY = (byte) 0x81;
    static final byte TLV_CHAIN_CODE = (byte) 0x82;


    public class SLIP10KeyPair {
        private byte[] privateKey;
        private byte[] chainCode;
        private byte[] publicKey;

        public boolean isExtended() {
            return chainCode != null;
        }


        public SLIP10KeyPair(byte[] privateKey,byte[] chainCode,byte[] publicKey)
        {
            this.privateKey=privateKey;
            this.publicKey=publicKey;
            this.chainCode=chainCode;
        }

        public byte[] toTLV(boolean includePublic) {

            int privLen = privateKey.length;
            int privOff = 0;

            if(privateKey[0] == 0x00) {
                privOff++;
                privLen--;
            }

            int off = 0;
            int totalLength = includePublic ? (publicKey.length + 2) : 0;
            totalLength += (privLen + 2);
            totalLength += isExtended() ? (chainCode.length + 2) : 0;

            if (totalLength > 127) {
                totalLength += 3;
            } else {
                totalLength += 2;
            }

            byte[] data = new byte[totalLength];

            data[off++] = TLV_KEY_TEMPLATE;

            if (totalLength > 127) {
                data[off++] = (byte) 0x81;
                data[off++] = (byte) (totalLength - 3);
            } else {
                data[off++] = (byte) (totalLength - 2);
            }

            if (includePublic) {
                data[off++] = TLV_PUB_KEY;
                data[off++] = (byte) publicKey.length;
                System.arraycopy(publicKey, 0, data, off, publicKey.length);
                off += publicKey.length;
            }

            data[off++] = TLV_PRIV_KEY;
            data[off++] = (byte) privLen;
            System.arraycopy(privateKey, privOff, data, off, privLen);
            off += privLen;

            if (isExtended()) {
                data[off++] = (byte) TLV_CHAIN_CODE;
                data[off++] = (byte) chainCode.length;
                System.arraycopy(chainCode, 0, data, off, chainCode.length);
            }

            return data;

        }
    }

        public  SLIP10KeyPair MnemonicsToSLIP10KeyPair(Mnemonic mnemonic) {

        //step 1) convert a mnemonic to a seed to a keypair using BIP-39
        byte[] seed = mnemonic.toBinarySeed();

        //step2) convert the seed to a master key pair using SLIP-10
        /*
        Let S be a seed byte sequence of 128 to 512 bits in length. This is the same as the seed byte sequence used in BIP-0032. The value of S should be the binary seed obtained from a BIP-0039 mnemonic and optional passphrase or it should be the master secret obtained from a set of SLIP-0039 mnemonics and optional passphrase.

        Calculate I = HMAC-SHA512(Key = Curve, Data = S)
        Split I into two 32-byte sequences, IL and IR.
        Use parse256(IL) as master secret key, and IR as master chain code.
        If curve is not ed25519 and IL is 0 or ≥ n (invalid key):
        Set S := I and continue at step 2.
         */

        try {
            String secret = "ed25519 seed";

            /*
            Curve = "Bitcoin seed" for the secp256k1 curve (this is compatible with BIP-0032).
            Curve = "Nist256p1 seed" for the NIST P-256 curve.
            Curve = "ed25519 seed" for the ed25519 curve.
            */


            Mac sha_HMAC = Mac.getInstance("HmacSHA512");

            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
            sha_HMAC.init(secret_key);
            byte[] hash=sha_HMAC.doFinal(seed);
/*
            String hash = Base64.encodeToString(sha_HMAC.doFinal(seed), Base64.DEFAULT);
            System.out.println(hash);
            Log.i("SLIP-10:","string is "+hash);
 */

            byte[] privK= Arrays.copyOfRange(hash,0,32);
            byte[] cc= Arrays.copyOfRange(hash,32,64);

            Ed25519PrivateKeyParameters privateKeyRebuild = new Ed25519PrivateKeyParameters(privK, 0);
            Ed25519PublicKeyParameters publicKeyRebuild = privateKeyRebuild.generatePublicKey();
            byte[] pubK=publicKeyRebuild.getEncoded();

            return new SLIP10KeyPair(privK,cc,pubK);

        }
        catch (Exception e){

            CardFunctions.setMsg0(CardFunctions.getMsg0()+"\n"+"Error");

        }

        return null;

    }


   public static byte[] hashData(byte[] data)
    {

        MessageDigest digest=null;
        String hash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(data);


            return  digest.digest();


        } catch (Exception e1) {

            e1.printStackTrace();
        }
        return null;
    }


}
