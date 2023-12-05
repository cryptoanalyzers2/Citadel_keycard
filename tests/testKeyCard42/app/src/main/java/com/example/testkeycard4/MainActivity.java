package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.nfc.NfcAdapter;

import android.app.AlertDialog;

//import im.status.keycard.demo.R;
import im.status.keycard.io.APDUCommand;
import im.status.keycard.io.APDUResponse;
import im.status.keycard.io.CardChannel;
import im.status.keycard.io.CardListener;
import im.status.keycard.android.NFCCardManager;
import im.status.keycard.applet.*;

import android.util.Base64;
import android.util.Log;
import org.bouncycastle.util.encoders.Hex;

import android.view.View;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import 	java.net.HttpURLConnection;
import 	java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private NFCCardManager cardManager;



    private static final String TAG = "MainActivity";
    AlertDialog.Builder builder;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String msg0="";

    public static String txtInfo="";

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

    public void displayMesssage(String msg)
    {

        builder.setMessage(msg)
                .setTitle("message");

        builder.create();
        builder.show();

    }

    byte[] hashData(byte[] data)
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

    void test_keycard6(CardChannel cardChannel) {

        msg0 = "";
        txtInfo="";
        try {
            TextView txtsecret = (TextView) findViewById(R.id.txt_secret);
            String SECRET = (String) txtsecret.getText().toString();

            TextView txtPIN = (TextView) findViewById(R.id.txt_PIN);
            String PIN = (String) txtPIN.getText().toString();


            KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);
            ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());
            if (!info.isInitializedCard()) {
                displayMesssage("Initialize Card First!");
                return;
            }

            //cmdSet.autoPair("KeycardTest");
            cmdSet.autoPair(SECRET);
            msg0 += "\n" + "Pairing done";
            //cmdSet.pair((byte) 0x00,"KeycardTest".getBytes());
            Pairing pairing = cmdSet.getPairing();
            //pairing.getPairingIndex();

            cmdSet.autoOpenSecureChannel();
            msg0 += "\n" + "Secure Channel opened";

            //cmdSet.verifyPIN("000000").checkAuthOK();
            cmdSet.verifyPIN(PIN).checkAuthOK();
            msg0 += "\n" + "PIN verified";


            KeyPath currentPath = new KeyPath(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().getData());
            Log.i(TAG, "Current key path: " + currentPath);
            msg0 += "\n" + "Current key path: " + currentPath;
            txtInfo+="\n"+ "Current key path: " + currentPath;

            if (!currentPath.toString().equals("m/44'/0'/0'/0/0")) {
                // Key derivation is needed to select the desired key. The derived key remains current until a new derive
                // command is sent (it is not lost on power loss).
                cmdSet.deriveKey("m/44'/0'/0'/0/0").checkOK();
                Log.i(TAG, "Derived m/44'/0'/0'/0/0");
                msg0 += "\n" + "Derived m/44'/0'/0'/0/0";
                txtInfo  += "\n" + "Derived m/44'/0'/0'/0/0";

            }

            // We retrieve the wallet public key
            BIP32KeyPair walletPublicKey = BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().getData());

            Log.i(TAG, "Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey()));
            txtInfo+="\n"+"Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey());
            msg0+="\n"+"Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey());

            Log.i(TAG, "Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress()));
            msg0 +="\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());
            txtInfo +="\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());
            cmdSet.unpairOthers();
            cmdSet.autoUnpair();

            Log.i(TAG, "Unpaired.");
            msg0+="\n"+"Unpaired.";

            displayMesssage("key generated, SECP256K1 public key="+Hex.toHexString(walletPublicKey.getPublicKey()));

        }
        catch (Exception ex)
        {
            displayMesssage(ex.getMessage());
        }

    }



    void test_keycard5(CardChannel cardChannel, byte[] data) {

        msg0 = "";
        try {

            TextView txtsecret = (TextView) findViewById(R.id.txt_secret);
            String SECRET = (String) txtsecret.getText().toString();

            TextView txtPIN = (TextView) findViewById(R.id.txt_PIN);
            String PIN = (String) txtPIN.getText().toString();



            KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);
            ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());
            if (!info.isInitializedCard()) {
                displayMesssage("Initialize Card First!");
                return;
            }

            if(!info.hasMasterKey())
            {
                displayMesssage("Generate a key first!");
                return;
            }

            //cmdSet.autoPair("KeycardTest");
            cmdSet.autoPair(SECRET);
            msg0 += "\n" + "Pairing done";
            //cmdSet.pair((byte) 0x00,"KeycardTest".getBytes());
            Pairing pairing = cmdSet.getPairing();
            //pairing.getPairingIndex();

            cmdSet.autoOpenSecureChannel();
            msg0 += "\n" + "Secure Channel opened";

            //cmdSet.verifyPIN("000000").checkAuthOK();
            cmdSet.verifyPIN(PIN).checkAuthOK();
            msg0 += "\n" + "PIN verified";



            KeyPath currentPath = new KeyPath(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().getData());
            Log.i(TAG, "Current key path: " + currentPath);
            msg0 += "\n" + "Current key path: " + currentPath;
            if (!currentPath.toString().equals("m/44'/0'/0'/0/0")) {
                // Key derivation is needed to select the desired key. The derived key remains current until a new derive
                // command is sent (it is not lost on power loss).
                cmdSet.deriveKey("m/44'/0'/0'/0/0").checkOK();
                Log.i(TAG, "Derived m/44'/0'/0'/0/0");
                msg0 += "\n" + "Derived m/44'/0'/0'/0/0";
            }

            // We retrieve the wallet public key
            BIP32KeyPair walletPublicKey = BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().getData());
            byte[] hsh= hashData(data);
           // byte[] hsh=data;

            Log.i(TAG, "SHA-256 result is " + Hex.toHexString(hsh));
            msg0+="\n"+"SHA-256 result is " +  Hex.toHexString(hsh);

            APDUResponse response = cmdSet.sign(hsh);

         //   response.checkOK();

            byte[] sign_hash=response.getData();

            cmdSet.unpairOthers();
            cmdSet.autoUnpair();

            Log.i(TAG, "Unpaired.");
            msg0+="\n"+"Unpaired.";

            displayMesssage("signature="+Hex.toHexString(sign_hash)+"\n"+Hex.toHexString(response.getBytes()));

        }
        catch (Exception ex)
        {
            displayMesssage(ex.getMessage());
        }

    }


    void test_keycard3(CardChannel cardChannel) {

        msg0 = "";
        try {
            TextView txtsecret = (TextView) findViewById(R.id.txt_secret);
            String SECRET = (String) txtsecret.getText().toString();

            TextView txtPIN = (TextView) findViewById(R.id.txt_PIN);
            String PIN = (String) txtPIN.getText().toString();


            KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);
            ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());
            if (!info.isInitializedCard()) {
                displayMesssage("Initialize Card First!");
                return;
            }

            //cmdSet.autoPair("KeycardTest");
            cmdSet.autoPair(SECRET);
            msg0 += "\n" + "Pairing done";
            //cmdSet.pair((byte) 0x00,"KeycardTest".getBytes());
            Pairing pairing = cmdSet.getPairing();
            //pairing.getPairingIndex();

            cmdSet.autoOpenSecureChannel();
            msg0 += "\n" + "Secure Channel opened";

            //cmdSet.verifyPIN("000000").checkAuthOK();
            cmdSet.verifyPIN(PIN).checkAuthOK();
            msg0 += "\n" + "PIN verified";


            cmdSet.generateKey();
            msg0 += "\n" + "Key Generated";

        KeyPath currentPath = new KeyPath(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().getData());
        Log.i(TAG, "Current key path: " + currentPath);
        msg0 += "\n" + "Current key path: " + currentPath;
        if (!currentPath.toString().equals("m/44'/0'/0'/0/0")) {
            // Key derivation is needed to select the desired key. The derived key remains current until a new derive
            // command is sent (it is not lost on power loss).
            cmdSet.deriveKey("m/44'/0'/0'/0/0").checkOK();
            Log.i(TAG, "Derived m/44'/0'/0'/0/0");
            msg0 += "\n" + "Derived m/44'/0'/0'/0/0";
        }

            // We retrieve the wallet public key
            BIP32KeyPair walletPublicKey = BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().getData());

            Log.i(TAG, "Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey()));
            txtInfo+="\n"+"Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey());
            msg0+="\n"+"Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey());

            Log.i(TAG, "Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress()));
            msg0 +="\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());
            txtInfo +="\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());
            cmdSet.unpairOthers();
            cmdSet.autoUnpair();

            Log.i(TAG, "Unpaired.");
            msg0+="\n"+"Unpaired.";

            displayMesssage("key generated, SECP256K1 public key="+Hex.toHexString(walletPublicKey.getPublicKey()));

    }
    catch (Exception ex)
        {
            displayMesssage(ex.getMessage());
        }

    }

    @SuppressLint("SuspiciousIndentation")
    void test_keycard(CardChannel cardChannel)
    {
try
{
msg0="";
    String txt="";
    String PIN="";
    String PASSWORD="";
    String PUK="";
    String MNEMONIC="";

        // Applet-specific code
        KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);

        Log.i(TAG, "Applet selection successful");

        // First thing to do is selecting the applet on the card.
        ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());

        // If the card is not initialized, the INIT apdu must be sent. The actual PIN, PUK and pairing password values
        // can be either generated or chosen by the user. Using fixed values is highly discouraged.
        if (!info.isInitializedCard()) {
            Log.i(TAG, "Initializing card with test secrets");

           // String PIN="";
           // String PUK="";
            Random rnd = new Random();
            for(int i=0;i<6;i++)
            {
                PIN += rnd.nextInt(10);
            }
            for(int i=0;i<12;i++)
            {
                PUK += rnd.nextInt(10);
            }

             PASSWORD =generateRandomWord(11);

            //cmdSet.init("000000", "123456789012", "KeycardTest").checkOK();
            cmdSet.init(PIN, PUK, PASSWORD).checkOK();
            msg0+="\n"+"Card has been initialized  with test secrets";
            msg0+="\n"+"PIN='"+PIN+"' PUK='"+PUK+"' pairing password='"+PASSWORD+"'";
            txt+="\n"+"PIN='"+PIN+"' PUK='"+PUK+"' pairing password='"+PASSWORD+"'";

            info = new ApplicationInfo(cmdSet.select().checkOK().getData());
        }

        Log.i(TAG, "Instance UID: " + Hex.toHexString(info.getInstanceUID()));
        msg0+="\n"+"Instance UID: " + Hex.toHexString(info.getInstanceUID());
    txt+="\n"+"Instance UID: " + Hex.toHexString(info.getInstanceUID());
        Log.i(TAG, "Secure channel public key: " + Hex.toHexString(info.getSecureChannelPubKey()));
        msg0+="\n"+"Secure channel public key: " +Hex.toHexString(info.getSecureChannelPubKey());
    txt+="\n"+"Secure channel public key: " +Hex.toHexString(info.getSecureChannelPubKey());
        Log.i(TAG, "Application version: " + info.getAppVersionString());
        msg0+="\n"+"Application version: "+ info.getAppVersionString();
    txt+="\n"+"Application version: "+ info.getAppVersionString();
        Log.i(TAG, "Free pairing slots: " + info.getFreePairingSlots());
        msg0+="\n"+"Free pairing slots: " + info.getFreePairingSlots();
    txt+="\n"+"Free pairing slots: " + info.getFreePairingSlots();
        if (info.hasMasterKey()) {
            Log.i(TAG, "Key UID: " + Hex.toHexString(info.getKeyUID()));
            msg0+="\n"+"Key UID: " + Hex.toHexString(info.getKeyUID());
            txt+="\n"+"Key UID: " + Hex.toHexString(info.getKeyUID());
        } else {
            Log.i(TAG, "The card has no master key");
            msg0+="\n"+"The card has no master key";
            txt+="\n"+"The card has no master key";
        }
        Log.i(TAG,  String.format("Capabilities: %02X", info.getCapabilities()));
    msg0+="\n"+String.format("Capabilities: %02X", info.getCapabilities());
    txt+="\n"+String.format("Capabilities: %02X", info.getCapabilities());
        Log.i(TAG, "Has Secure Channel: " + info.hasSecureChannelCapability());
    msg0+="\n"+"Has Secure Channel: " + info.hasSecureChannelCapability();
    txt+="\n"+"Has Secure Channel: " + info.hasSecureChannelCapability();
        Log.i(TAG, "Has Key Management: " + info.hasKeyManagementCapability());
    msg0+="\n"+"Has Key Management: " + info.hasKeyManagementCapability();
    txt+="\n"+"Has Key Management: " + info.hasKeyManagementCapability();
        Log.i(TAG, "Has Credentials Management: " + info.hasCredentialsManagementCapability());
    msg0+="\n"+"Has Credentials Management: " + info.hasCredentialsManagementCapability();
    txt+="\n"+"Has Credentials Management: " + info.hasCredentialsManagementCapability();
        Log.i(TAG, "Has NDEF capability: " + info.hasNDEFCapability());
    msg0+="\n"+"Has NDEF capability: " + info.hasNDEFCapability();
    txt+="\n"+"Has NDEF capability: " + info.hasNDEFCapability();

        if (info.hasSecureChannelCapability()) {
            // In real projects, the pairing key should be saved and used for all new sessions.
            cmdSet.autoPair(PASSWORD);
            //cmdSet.pair((byte) 0x00,"KeycardTest".getBytes());
            Pairing pairing = cmdSet.getPairing();

            // Never log the pairing key in a real application!
            Log.i(TAG, "Pairing with card is done.");

            displayMesssage("Pairing with card is done: "+"Pairing index: " + pairing.getPairingIndex()+"Pairing key: " + Hex.toHexString(pairing.getPairingKey()));
            Log.i(TAG, "Pairing index: " + pairing.getPairingIndex());

            Log.i(TAG, "Pairing key: " + Hex.toHexString(pairing.getPairingKey()));
            msg0+="\n"+"Pairing with card is done: "+"Pairing index: " + pairing.getPairingIndex()+"Pairing key: " + Hex.toHexString(pairing.getPairingKey());
            // Opening a Secure Channel is needed for all other applet commands
            cmdSet.autoOpenSecureChannel();

            Log.i(TAG, "Secure channel opened. Getting applet status.");
            msg0+="\n"+"Secure channel opened. Getting applet status.";
        }

        // We send a GET STATUS command, which does not require PIN authentication
        ApplicationStatus status = new ApplicationStatus(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_APPLICATION).checkOK().getData());

        Log.i(TAG, "PIN retry counter: " + status.getPINRetryCount());
    msg0+="\n"+"PIN retry counter: " + status.getPINRetryCount();
    txt+="\n"+"PIN retry counter: " + status.getPINRetryCount();
        Log.i(TAG, "PUK retry counter: " + status.getPUKRetryCount());
    msg0+="\n"+"PUK retry counter: " + status.getPUKRetryCount();
    txt+="\n"+"PUK retry counter: " + status.getPUKRetryCount();
        Log.i(TAG, "Has master key: " + status.hasMasterKey());
    msg0+="\n"+"Has master key: " + status.hasMasterKey();
    txt+="\n"+"Has master key: " + status.hasMasterKey();
        if (info.hasKeyManagementCapability()) {
            // A mnemonic can be generated before PIN authentication. Generating a mnemonic does not create keys on the
            // card. a subsequent loadKey step must be performed after PIN authentication. In this example we will only
            // show how to convert the output of the card to a usable format but won't actually load the key
            Mnemonic mnemonic = new Mnemonic(cmdSet.generateMnemonic(KeycardCommandSet.GENERATE_MNEMONIC_12_WORDS).checkOK().getData());

            // We need to set a wordlist if we plan using this object to derive the binary seed. If we just need the word
            // indexes we can skip this step and call mnemonic.getIndexes() instead.
            mnemonic.fetchBIP39EnglishWordlist();

            Log.i(TAG, "Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase());
            msg0+="\n"+"Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase();
            txt+="\n"+"Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase();

            MNEMONIC="Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase();
            //displayMesssage("Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase());

            Log.i(TAG, "Binary seed: " + Hex.toHexString(mnemonic.toBinarySeed()));
            msg0+="\n"+"Binary seed: " + Hex.toHexString(mnemonic.toBinarySeed());
            txt+="\n"+"Binary seed: " + Hex.toHexString(mnemonic.toBinarySeed());
        }

        if (info.hasCredentialsManagementCapability()) {
            // PIN authentication allows execution of privileged commands
            cmdSet.verifyPIN(PIN).checkAuthOK();

            Log.i(TAG, "Pin Verified.");
            msg0+="\n"+"Pin Verified.";
        }

        // If the card has no keys, we generate a new set. Keys can also be loaded on the card starting from a binary
        // seed generated from a mnemonic phrase. In alternative, we could load the generated keypair as shown in the
        // commented line of code.
        if (!status.hasMasterKey() && info.hasKeyManagementCapability()) {
            cmdSet.generateKey();
            //cmdSet.loadKey(mnemonic.toBIP32KeyPair());
        }

        // Get the current key path using GET STATUS
        KeyPath currentPath = new KeyPath(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().getData());
        Log.i(TAG, "Current key path: " + currentPath);
    msg0+="\n"+"Current key path: " + currentPath;
        if (!currentPath.toString().equals("m/44'/0'/0'/0/0")) {
            // Key derivation is needed to select the desired key. The derived key remains current until a new derive
            // command is sent (it is not lost on power loss).
            cmdSet.deriveKey("m/44'/0'/0'/0/0").checkOK();
            Log.i(TAG, "Derived m/44'/0'/0'/0/0");
            msg0+="\n"+"Derived m/44'/0'/0'/0/0";
        }

        // We retrieve the wallet public key
        BIP32KeyPair walletPublicKey = BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().getData());

        Log.i(TAG, "Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey()));
        Log.i(TAG, "Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress()));
 txt +="\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());

    msg0+="\n"+Hex.toHexString(walletPublicKey.getPublicKey());
    msg0+="\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());
        byte[] hash = "thiscouldbeahashintheorysoitisok".getBytes();

        RecoverableSignature signature = new RecoverableSignature(hash, cmdSet.sign(hash).checkOK().getData());

        Log.i(TAG, "Signed hash: " + Hex.toHexString(hash));
    msg0+="\n"+"Signed hash: " + Hex.toHexString(hash);
    txt+="\n"+"Signed hash: " + Hex.toHexString(hash);
        Log.i(TAG, "Recovery ID: " + signature.getRecId());
    msg0+="\n"+"Recovery ID: " + signature.getRecId();
    txt+="\n"+"Recovery ID: " + signature.getRecId();
        Log.i(TAG, "R: " + Hex.toHexString(signature.getR()));
    msg0+="\n"+"R: " + Hex.toHexString(signature.getR());
    txt+="\n"+"R: " + Hex.toHexString(signature.getR());
        Log.i(TAG, "S: " + Hex.toHexString(signature.getS()));
    msg0+="\n"+"S: " + Hex.toHexString(signature.getS());
    txt+="\n"+"S: " + Hex.toHexString(signature.getS());

        if (info.hasSecureChannelCapability()) {
            // Cleanup, in a real application you would not unpair and instead keep the pairing key for successive interactions.
            // We also remove all other pairings so that we do not fill all slots with failing runs. Again in real application
            // this would be a very bad idea to do.
            cmdSet.unpairOthers();
            cmdSet.autoUnpair();

            Log.i(TAG, "Unpaired.");
            msg0+="\n"+"Unpaired.";
        }

        displayMesssage(txt);
         displayMesssage("Card has been initialized  with test secrets"+"\n"+"PIN='"+PIN+"' PUK='"+PUK+"' pairing password='"+PASSWORD+"'"+"\n"+MNEMONIC);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("credentials", "Card has been initialized  with test secrets"+"\n"+"PIN='"+PIN+"' PUK='"+PUK+"' pairing password='"+PASSWORD+"'"+"\n"+MNEMONIC);
        clipboard.setPrimaryClip(clip);

} catch (Exception e) {
        Log.e(TAG, e.getMessage());
        msg0+="\n"+e.getMessage();
    }

}

    @Override
    protected void onCreate(Bundle savedInstanceState) {

         builder = new AlertDialog.Builder(this);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        cardManager = new NFCCardManager();
        //cardManager = new LedgerBLEManager(this);
        cardManager.setCardListener(new CardListener() {
            @Override
            public void onConnected(CardChannel cardChannel) {

                test_keycard(cardChannel);

            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, "Card disconnected.");
            }
        });
        cardManager.start();
    /*connected = false;
    cardManager.startScan(new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (connected) {
          return;
        }

        connected = true;
        cardManager.stopScan(this);
        cardManager.connectDevice(device);
      }
    });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, this.cardManager, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    public void test4(View view) {

      displayMesssage("not yet implemented");

    }

    public void test3(View view) {

       //generate an ECDSA SECP256K1 key if not already generated
        CardChannel netchannel = new netCardChannel();

        test_keycard3(netchannel);


    }

    public void test5(View view) {
        try {
            //generate an ECDSA SECP256K1 key if not already generated
            CardChannel netchannel = new netCardChannel();

            //getting the data to sign
            TextView txtsign = (TextView) findViewById(R.id.txt_signature);
            String hex_to_sign = (String) txtsign.getText().toString();
            byte[] bHex_to_sign=null;


                bHex_to_sign = Hex.decode(hex_to_sign);


            if(bHex_to_sign==null)
            {
                displayMesssage("incorrect hexa data");
                return;
            }

            test_keycard5(netchannel,bHex_to_sign);
        }
        catch(Exception ex)
        {
            displayMesssage(ex.getMessage());
        }

    }

    public void test6(View view) {
        CardChannel netchannel = new netCardChannel();

        test_keycard6(netchannel);
        displayMesssage(txtInfo);

    }

    public void test2(View view) {

        displayMesssage(msg0);

    }

        public void test1(View view) {


        CardChannel netchannel = new netCardChannel();

      //  if(netchannel.isConnected())
     //   {
     //       displayMesssage("connected to card via netCardChannel");

           // APDUCommand cmd = new APDUCommand((int)0x00,(int)0xA4,(int)0x04,(int)0x00,null);

            test_keycard(netchannel);
           // displayMesssage("test keycard completed");
            /*
            try {
                netchannel.send(cmd);
                displayMesssage("test command sent to card");
            } catch (IOException e) {
                displayMesssage(e.getMessage());

            }
             */

    //    }



        //displayMesssage("Test KeyCard4 Application");
        //use netCardChannel
    }

    /// only for development / debug (not for production)
    public static class netCardChannel implements CardChannel{

        //the hostname/IP of the apdu server
        //no need for SSL...
        static final String HOSTNAME= "encryptiontest.gotdns.ch";

        //Special alias to your host loopback interface (127.0.0.1 on your development machine)
        static final String LOCALHOST="10.0.2.2";
        static final int PORT=38099;
        static boolean isConnected=false;

        byte[] apdu_out;

        static final String TAG0= "NetCardChannel";

        //static HttpURLConnection urlConnection=null;
        //URL url = null;

        public boolean connect(HttpURLConnection urlConnection )
        {
            if(isConnected==true)
            {
                 return true;
            }

            try
            {

                //connection at start
                //url = new URL("http://" + HOSTNAME + ":" + PORT);
               URL url = new URL("http://" + LOCALHOST + ":" + PORT);
                urlConnection = (HttpURLConnection) url.openConnection();
                isConnected= true;
                msg0+="\n"+"connected to host (netcardchannel)";

            }
            catch(Exception ex)
            {

                isConnected= false;
                Log.e(TAG0, "cannot start netCardChannel");
                Log.e(TAG0, ex.getMessage());
                msg0+="\n"+ex.getMessage();

            }

            return isConnected;
        }


        public netCardChannel(){



        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return result.toString();
        }
        private void writeStream(DataOutputStream out, byte[] apdu_in) throws IOException {

            HashMap<String, String> postDataParams = new HashMap<String, String>();
            postDataParams.put("req", Base64.encodeToString(apdu_in,Base64.NO_WRAP | Base64.URL_SAFE));

            out.writeBytes(getPostDataString(postDataParams));
           //out.writeBytes("data=test");
            //out.flush();

        }

        private class sendDataNetTask extends AsyncTask<byte[],Integer, Long> {
            @Override
            protected Long doInBackground(byte[]... data) {
               try {

                   byte[] apdu_in = data[0];
                   HashMap<String, String> postDataParams = new HashMap<String, String>();
                   postDataParams.put("req", Base64.encodeToString(apdu_in,Base64.NO_WRAP | Base64.URL_SAFE));
                   Log.i(TAG0,"\nsending to card="+bytesToHex(apdu_in));
                   msg0+="\nsending to card="+bytesToHex(apdu_in);


                   HttpURLConnection urlConnection =null;
                 // boolean res= connect(urlConnection)


                   //connection at start
                   //url = new URL("http://" + HOSTNAME + ":" + PORT);
                   URL url = new URL("http://" + LOCALHOST + ":" + PORT+"?"+getPostDataString(postDataParams));
                   urlConnection = (HttpURLConnection) url.openConnection();
                   isConnected= true;


                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(1024);
                    urlConnection.setReadTimeout(15000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                    //writeStream(out, data[0]);
                    //writeStream(out, new byte[4096]);
                    //out.writeBytes("req=data");
                    //out.flush();

                   out.writeBytes(getPostDataString(postDataParams));
                   out.flush();
                   DataInputStream in = new DataInputStream(urlConnection.getInputStream());

                   List<Byte> b = new ArrayList<Byte>();

                   try {
                       while(true) {
                           byte b0 = in.readByte();

                           b.add(b0);
                       }
                   }
                   catch(Exception ex)
                   {
                     //  Log.e(TAG0, ex.getMessage());
                   }

                   byte[] b1= new byte[b.toArray().length];
                  for(int i=0;i<b.toArray().length;i++)
                  {
                      b1[i]=(byte)b.toArray()[i];
                  }


                   apdu_out=Base64.decode(b1,Base64.NO_WRAP | Base64.URL_SAFE);

                   Log.i(TAG0, "answer from card="+bytesToHex(apdu_out));
                   msg0+="\nanswer from card="+bytesToHex(apdu_out);
                    out.close();
                    urlConnection.disconnect();


               }
               catch(Exception ex)
               {
                   msg0=ex.getMessage();
                   Log.e(TAG0, ex.getMessage());
                   apdu_out=null;
               }

               return 0L;
            }

            protected void onProgressUpdate(Integer... progress) {
                Log.i(TAG0, "progress="+progress);
            }


            protected void onPostExecute(Long result) {
                Log.i(TAG0, "executed="+result);
            }
        }



        @Override
        public APDUResponse send(APDUCommand cmd) throws IOException {



            byte CLA =(byte) cmd.getCla();
            byte INS =(byte)  cmd.getIns();
            byte P1 =(byte)  cmd.getP1();
            byte P2 = (byte) cmd.getP2();

            ArrayList<Byte> apdu_list = new ArrayList<>();

            // byte[] apdu_header= {CLA,INS,P1,P2};
            apdu_list.add(CLA);
            apdu_list.add(INS);
            apdu_list.add(P1);
            apdu_list.add(P2);

            if((cmd.getData()!=null)&&(cmd.getData().length>0))
            {

                apdu_list.add((byte)cmd.getData().length);
                for(int i=0;i<cmd.getData().length;i++)
                {
                    apdu_list.add(cmd.getData()[i]);
                }
            }

            byte[] apdu = new byte[apdu_list.toArray().length];
            //the joy of Java...
            for(int i=0;i<apdu_list.toArray().length;i++)
            {
                apdu[i]= (byte) apdu_list.toArray()[i];
            }

            try {
               // sendapduNetwork(apdu_in);

                apdu_out= null;

                 new sendDataNetTask().execute(apdu);
                 //50x500=25secs timeout
                for(int i=0;i<500;i++)
                {
                    Thread.sleep(50);
                    if(apdu_out!=null)
                    {
                        break;
                    }
                }

                APDUResponse response = new APDUResponse(apdu_out);

                return response;
            }
            catch(Exception ex)
            {
                Log.e(TAG0, ex.getMessage());
                return null;
            }

        }

        @Override
        public boolean isConnected() {
            return isConnected;
        }
    }
}