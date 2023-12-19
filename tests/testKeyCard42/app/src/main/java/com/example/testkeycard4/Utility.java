package com.example.testkeycard4;

import android.app.AlertDialog;

import java.util.Random;

public class Utility {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


    static byte[] Bytes2bytes(Byte[] oBytes)
    {

        byte[] bytes = new byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++){
            bytes[i] = oBytes[i];
        }
        return bytes;

    }

    public static String generateRandomWord(int letters)
    {

        Random random = new Random();

        char[] word = new char[letters]; // words of length 3 through 10. (1 and 2 letter words are boring.)
        for(int j = 0; j < word.length; j++)
        {
            word[j] = (char)('a' + random.nextInt(26));
        }
        return new String(word);

    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void displayMesssage(String msg,  AlertDialog.Builder builder)
    {

        builder.setMessage(msg)
                .setTitle("message");

        builder.create();
        builder.show();

    }

}
