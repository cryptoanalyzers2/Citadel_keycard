package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import 	java.net.HttpURLConnection;
import 	java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private NFCCardManager cardManager;



    private static final String TAG = "MainActivity";
    AlertDialog.Builder builder;

    public void displayMesssage(String msg)
    {

        builder.setMessage(msg)
                .setTitle("message");

        builder.create();
        builder.show();

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
                try {

                    displayMesssage("Connected to card");

                    // Applet-specific code
                    KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);

                    Log.i(TAG, "Applet selection successful");

                    // First thing to do is selecting the applet on the card.
                    ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());

                    // If the card is not initialized, the INIT apdu must be sent. The actual PIN, PUK and pairing password values
                    // can be either generated or chosen by the user. Using fixed values is highly discouraged.
                    if (!info.isInitializedCard()) {
                        Log.i(TAG, "Initializing card with test secrets");
                        displayMesssage("Initializing card with test secrets");
                        cmdSet.init("000000", "123456789012", "KeycardTest").checkOK();
                        info = new ApplicationInfo(cmdSet.select().checkOK().getData());
                    }

                    Log.i(TAG, "Instance UID: " + Hex.toHexString(info.getInstanceUID()));
                    Log.i(TAG, "Secure channel public key: " + Hex.toHexString(info.getSecureChannelPubKey()));
                    Log.i(TAG, "Application version: " + info.getAppVersionString());
                    Log.i(TAG, "Free pairing slots: " + info.getFreePairingSlots());
                    if (info.hasMasterKey()) {
                        Log.i(TAG, "Key UID: " + Hex.toHexString(info.getKeyUID()));
                    } else {
                        Log.i(TAG, "The card has no master key");
                    }
                    Log.i(TAG,  String.format("Capabilities: %02X", info.getCapabilities()));
                    Log.i(TAG, "Has Secure Channel: " + info.hasSecureChannelCapability());
                    Log.i(TAG, "Has Key Management: " + info.hasKeyManagementCapability());
                    Log.i(TAG, "Has Credentials Management: " + info.hasCredentialsManagementCapability());
                    Log.i(TAG, "Has NDEF capability: " + info.hasNDEFCapability());

                    if (info.hasSecureChannelCapability()) {
                        // In real projects, the pairing key should be saved and used for all new sessions.
                        cmdSet.autoPair("KeycardTest");
                        Pairing pairing = cmdSet.getPairing();

                        // Never log the pairing key in a real application!
                        Log.i(TAG, "Pairing with card is done.");
                        displayMesssage("Pairing with card is done");
                        Log.i(TAG, "Pairing index: " + pairing.getPairingIndex());
                        Log.i(TAG, "Pairing key: " + Hex.toHexString(pairing.getPairingKey()));

                        // Opening a Secure Channel is needed for all other applet commands
                        cmdSet.autoOpenSecureChannel();

                        Log.i(TAG, "Secure channel opened. Getting applet status.");
                    }

                    // We send a GET STATUS command, which does not require PIN authentication
                    ApplicationStatus status = new ApplicationStatus(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_APPLICATION).checkOK().getData());

                    Log.i(TAG, "PIN retry counter: " + status.getPINRetryCount());
                    Log.i(TAG, "PUK retry counter: " + status.getPUKRetryCount());
                    Log.i(TAG, "Has master key: " + status.hasMasterKey());

                    if (info.hasKeyManagementCapability()) {
                        // A mnemonic can be generated before PIN authentication. Generating a mnemonic does not create keys on the
                        // card. a subsequent loadKey step must be performed after PIN authentication. In this example we will only
                        // show how to convert the output of the card to a usable format but won't actually load the key
                        Mnemonic mnemonic = new Mnemonic(cmdSet.generateMnemonic(KeycardCommandSet.GENERATE_MNEMONIC_12_WORDS).checkOK().getData());

                        // We need to set a wordlist if we plan using this object to derive the binary seed. If we just need the word
                        // indexes we can skip this step and call mnemonic.getIndexes() instead.
                        mnemonic.fetchBIP39EnglishWordlist();

                        Log.i(TAG, "Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase());
                        Log.i(TAG, "Binary seed: " + Hex.toHexString(mnemonic.toBinarySeed()));
                    }

                    if (info.hasCredentialsManagementCapability()) {
                        // PIN authentication allows execution of privileged commands
                        cmdSet.verifyPIN("000000").checkAuthOK();

                        Log.i(TAG, "Pin Verified.");
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

                    if (!currentPath.toString().equals("m/44'/0'/0'/0/0")) {
                        // Key derivation is needed to select the desired key. The derived key remains current until a new derive
                        // command is sent (it is not lost on power loss).
                        cmdSet.deriveKey("m/44'/0'/0'/0/0").checkOK();
                        Log.i(TAG, "Derived m/44'/0'/0'/0/0");
                    }

                    // We retrieve the wallet public key
                    BIP32KeyPair walletPublicKey = BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().getData());

                    Log.i(TAG, "Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey()));
                    Log.i(TAG, "Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress()));

                    byte[] hash = "thiscouldbeahashintheorysoitisok".getBytes();

                    RecoverableSignature signature = new RecoverableSignature(hash, cmdSet.sign(hash).checkOK().getData());

                    Log.i(TAG, "Signed hash: " + Hex.toHexString(hash));
                    Log.i(TAG, "Recovery ID: " + signature.getRecId());
                    Log.i(TAG, "R: " + Hex.toHexString(signature.getR()));
                    Log.i(TAG, "S: " + Hex.toHexString(signature.getS()));

                    if (info.hasSecureChannelCapability()) {
                        // Cleanup, in a real application you would not unpair and instead keep the pairing key for successive interactions.
                        // We also remove all other pairings so that we do not fill all slots with failing runs. Again in real application
                        // this would be a very bad idea to do.
                        cmdSet.unpairOthers();
                        cmdSet.autoUnpair();

                        Log.i(TAG, "Unpaired.");
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

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

    public void test1(View view) {


        CardChannel netchannel = new netCardChannel();

        if(netchannel.isConnected())
        {
            displayMesssage("connected to card via netCardChannel");

            APDUCommand cmd = new APDUCommand((int)0x00,(int)0x0A,(int)0x04,(int)0x00,null);
            try {
                netchannel.send(cmd);
                displayMesssage("test command sent to card");
            } catch (IOException e) {
                displayMesssage(e.getMessage());

            }

        }



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

        static final String TAG0= "NetCardChannel";

        static HttpURLConnection urlConnection=null;
        URL url = null;

        public netCardChannel(){
        try
            {

                //connection at start
                //url = new URL("http://" + HOSTNAME + ":" + PORT);
                url = new URL("http://" + LOCALHOST + ":" + PORT);
                urlConnection = (HttpURLConnection) url.openConnection();
                isConnected= true;

            }
        catch(Exception ex)
            {

                isConnected= false;
                Log.e(TAG0, "cannot start netCardChannel");
                Log.e(TAG0, ex.getMessage());

            }

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
            out.writeBytes("data=test");
            out.flush();

        }

        private class sendDataNetTask extends AsyncTask<byte[],Integer, Long> {
            @Override
            protected Long doInBackground(byte[]... data) {
               try {

                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(1024);
                   urlConnection.setReadTimeout(15000);
                   urlConnection.setConnectTimeout(15000);
                    urlConnection.setRequestMethod("GET");
                   DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                   writeStream(out, data[0]);
                   //writeStream(out, new byte[4096]);
                    //out.writeBytes("req=data");
                    //out.flush();
                    out.close();

               }
               catch(Exception ex)
               {
                   Log.e(TAG0, ex.getMessage());
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

        public void   sendapduNetwork(byte[] apdu_in) throws IOException
        {

            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            writeStream(out, apdu_in);

        }

        @Override
        public APDUResponse send(APDUCommand cmd) throws IOException {

            if(isConnected==false)
            {
                return null;
            }

            byte CLA =(byte) cmd.getCla();
            byte INS =(byte)  cmd.getIns();
            byte P1 =(byte)  cmd.getP1();
            byte P2 = (byte) cmd.getP2();

            byte[] apdu_in= {CLA,INS,P1,P2};

            try {
               // sendapduNetwork(apdu_in);
                new sendDataNetTask().execute(apdu_in);

                return null;
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