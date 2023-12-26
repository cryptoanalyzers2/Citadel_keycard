package com.example.testkeycard4;

import static com.example.testkeycard4.Utility.generateRandomWord;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.subArray;

import android.annotation.SuppressLint;
import android.nfc.NfcAdapter;
import android.util.Log;

import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Random;

import im.status.keycard.android.NFCCardManager;
import im.status.keycard.applet.ApplicationInfo;
import im.status.keycard.applet.ApplicationStatus;
import im.status.keycard.applet.BIP32KeyPair;
import im.status.keycard.applet.KeyPath;
import im.status.keycard.applet.KeycardCommandSet;
import im.status.keycard.applet.Mnemonic;
import im.status.keycard.applet.Pairing;
import im.status.keycard.io.APDUException;
import im.status.keycard.io.APDUResponse;
import im.status.keycard.io.CardChannel;
import im.status.keycard.io.CardListener;

public class CardFunctions {
    private static final byte LOAD_KEY_ED25519_P1 =0x11 ;
    private static final byte SIGN_P1_ED25519_TEST=0x20;
    private static NfcAdapter nfcAdapter;
    private static NFCCardManager cardManager = new NFCCardManager();

    private static CardChannel channel;


    private static String msg0="";

    private static String txtInfo="";

    private static String TAG="CITADEL_KEYCARD";

    private static String PIN="";
    private static String PASSWORD="";

    private static String PUK="";
    private static String MNEMONIC="";


   private static Random rnd = new Random();

   public static class signature_data
   {
       public byte[] signature;
       public byte[] pubKey;

       public int curve;

       public  void set_signature_data(byte[] signature,byte[] pubKey,int curve)
       {

        this.signature=signature;
        this.pubKey=pubKey;
        this.curve=curve;

       }

   }

    public static void getCardChannel(boolean useNetCardChannel)
    {
        try {

            msg0+="\n"+"getCardChannel";

                if (useNetCardChannel == true) {
                    setChannel(new MainActivity.netCardChannel());
                    msg0+="\n"+"set channel to netCardChannel";
                } else {
                    msg0+="\n"+"set channel to NFCCardChannel (listener)";
                    cardManager.setCardListener(new CardListener()
                    {

                        @Override
                        public void onConnected(CardChannel channel_) {

                            Log.i(TAG, "connected via NFC");
                            msg0+="\n"+"connected via NFC";

                            setChannel(channel_);
                        }

                        @Override
                        public void onDisconnected() {
                            Log.i(TAG, "disconnected from NFC");
                            msg0+="\n"+"disconnected from NFC";

                            /*
                            setChannel(null);
                            */
                        }
                    });

                }
            }
        catch(Exception ex)
        {
            setChannel(null);
        }

    }

    public static String getPIN() {
        return PIN;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }

    public static void setPASSWORD(String s) {
         PASSWORD = s;
    }

    public static String getPUK() {
        return PUK;
    }

    public static String getMNEMONIC() {
        return MNEMONIC;
    }

    public static void loadKey_ed25519(CardChannel cardChannel,Crypto.SLIP10KeyPair kp, boolean omitPublic) throws IOException, APDUException {

        KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);

        cmdSet.loadKey(kp.toTLV(!omitPublic), LOAD_KEY_ED25519_P1).checkOK();

    }


    @SuppressLint("SuspiciousIndentation")
    public static void Initialize(CardChannel cardChannel) throws Exception {
        try
        {
            msg0="";
            String txt="";

            // Applet-specific code
            KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);

            Log.i(TAG, "Applet selection successful");

            // First thing to do is selecting the applet on the card.
            ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());

            // If the card is not initialized, the INIT apdu must be sent. The actual PIN, PUK and pairing password values
            // can be either generated or chosen by the user. Using fixed values is highly discouraged.
            if (!info.isInitializedCard()) {
                Log.i(TAG, "Initializing card with test secrets");


                //cmdSet.init("000000", "123456789012", "KeycardTest").checkOK();
                cmdSet.init(getPIN(), getPUK(), PASSWORD).checkOK();
                msg0 = getMsg0() + "\n"+"Card has been initialized  with test secrets";
                msg0 = getMsg0() + "\n"+"PIN='"+ getPIN() +"' PUK='"+ getPUK() +"' pairing password='"+PASSWORD+"'";
                txt+="\n"+"PIN='"+ getPIN() +"' PUK='"+ getPUK() +"' pairing password='"+PASSWORD+"'";

                /*
                TextView txtsecret = (TextView) findViewById(R.id.txt_secret);
                txtsecret.setText(PASSWORD);
                TextView txtPIN = (TextView) findViewById(R.id.txt_PIN);
                txtPIN.setText(PIN);
                 */

                info = new ApplicationInfo(cmdSet.select().checkOK().getData());
            }

            Log.i(TAG, "Instance UID: " + Hex.toHexString(info.getInstanceUID()));
            msg0 = getMsg0() + "\n"+"Instance UID: " + Hex.toHexString(info.getInstanceUID());
            txt+="\n"+"Instance UID: " + Hex.toHexString(info.getInstanceUID());
            Log.i(TAG, "Secure channel public key: " + Hex.toHexString(info.getSecureChannelPubKey()));
            msg0 = getMsg0() + "\n"+"Secure channel public key: " + Hex.toHexString(info.getSecureChannelPubKey());
            txt+="\n"+"Secure channel public key: " +Hex.toHexString(info.getSecureChannelPubKey());
            Log.i(TAG, "Application version: " + info.getAppVersionString());
            msg0 = getMsg0() + "\n"+"Application version: "+ info.getAppVersionString();
            txt+="\n"+"Application version: "+ info.getAppVersionString();
            Log.i(TAG, "Free pairing slots: " + info.getFreePairingSlots());
            msg0 = getMsg0() + "\n"+"Free pairing slots: " + info.getFreePairingSlots();
            txt+="\n"+"Free pairing slots: " + info.getFreePairingSlots();
            if (info.hasMasterKey()) {
                Log.i(TAG, "Key UID: " + Hex.toHexString(info.getKeyUID()));
                msg0 = getMsg0() + "\n"+"Key UID: " + Hex.toHexString(info.getKeyUID());
                txt+="\n"+"Key UID: " + Hex.toHexString(info.getKeyUID());
            } else {
                Log.i(TAG, "The card has no master key");
                msg0 = getMsg0() + "\n"+"The card has no master key";
                txt+="\n"+"The card has no master key";
            }
            Log.i(TAG,  String.format("Capabilities: %02X", info.getCapabilities()));
            msg0 = getMsg0() + "\n"+String.format("Capabilities: %02X", info.getCapabilities());
            txt+="\n"+String.format("Capabilities: %02X", info.getCapabilities());
            Log.i(TAG, "Has Secure Channel: " + info.hasSecureChannelCapability());
            msg0 = getMsg0() + "\n"+"Has Secure Channel: " + info.hasSecureChannelCapability();
            txt+="\n"+"Has Secure Channel: " + info.hasSecureChannelCapability();
            Log.i(TAG, "Has Key Management: " + info.hasKeyManagementCapability());
            msg0 = getMsg0() + "\n"+"Has Key Management: " + info.hasKeyManagementCapability();
            txt+="\n"+"Has Key Management: " + info.hasKeyManagementCapability();
            Log.i(TAG, "Has Credentials Management: " + info.hasCredentialsManagementCapability());
            msg0 = getMsg0() + "\n"+"Has Credentials Management: " + info.hasCredentialsManagementCapability();
            txt+="\n"+"Has Credentials Management: " + info.hasCredentialsManagementCapability();
            Log.i(TAG, "Has NDEF capability: " + info.hasNDEFCapability());
            msg0 = getMsg0() + "\n"+"Has NDEF capability: " + info.hasNDEFCapability();
            txt+="\n"+"Has NDEF capability: " + info.hasNDEFCapability();

            if (info.hasSecureChannelCapability()) {
                // In real projects, the pairing key should be saved and used for all new sessions.
                cmdSet.autoPair(PASSWORD);
                //cmdSet.pair((byte) 0x00,"KeycardTest".getBytes());
                Pairing pairing = cmdSet.getPairing();

                // Never log the pairing key in a real application!
                Log.i(TAG, "Pairing with card is done.");

                //displayMesssage("Pairing with card is done: "+"Pairing index: " + pairing.getPairingIndex()+"Pairing key: " + Hex.toHexString(pairing.getPairingKey()));
                Log.i(TAG, "Pairing index: " + pairing.getPairingIndex());

                Log.i(TAG, "Pairing key: " + Hex.toHexString(pairing.getPairingKey()));
                msg0 = getMsg0() + "\n"+"Pairing with card is done: "+"Pairing index: " + pairing.getPairingIndex()+"Pairing key: " + Hex.toHexString(pairing.getPairingKey());
                // Opening a Secure Channel is needed for all other applet commands
                cmdSet.autoOpenSecureChannel();

                Log.i(TAG, "Secure channel opened. Getting applet status.");
                msg0 = getMsg0() + "\n"+"Secure channel opened. Getting applet status.";
            }

            // We send a GET STATUS command, which does not require PIN authentication
            ApplicationStatus status = new ApplicationStatus(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_APPLICATION).checkOK().getData());

            Log.i(TAG, "PIN retry counter: " + status.getPINRetryCount());
            msg0 = getMsg0() + "\n"+"PIN retry counter: " + status.getPINRetryCount();
            txt+="\n"+"PIN retry counter: " + status.getPINRetryCount();
            Log.i(TAG, "PUK retry counter: " + status.getPUKRetryCount());
            msg0 = getMsg0() + "\n"+"PUK retry counter: " + status.getPUKRetryCount();
            txt+="\n"+"PUK retry counter: " + status.getPUKRetryCount();
            Log.i(TAG, "Has master key: " + status.hasMasterKey());
            msg0 = getMsg0() + "\n"+"Has master key: " + status.hasMasterKey();
            txt+="\n"+"Has master key: " + status.hasMasterKey();

            Mnemonic mnemonic=null;

            if (info.hasKeyManagementCapability()) {
                // A mnemonic can be generated before PIN authentication. Generating a mnemonic does not create keys on the
                // card. a subsequent loadKey step must be performed after PIN authentication. In this example we will only
                // show how to convert the output of the card to a usable format but won't actually load the key
                mnemonic = new Mnemonic(cmdSet.generateMnemonic(KeycardCommandSet.GENERATE_MNEMONIC_12_WORDS).checkOK().getData());

                // We need to set a wordlist if we plan using this object to derive the binary seed. If we just need the word
                // indexes we can skip this step and call mnemonic.getIndexes() instead.
                mnemonic.fetchBIP39EnglishWordlist();

                Log.i(TAG, "Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase());
                msg0 = getMsg0() + "\n"+"Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase();
                txt+="\n"+"Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase();

                MNEMONIC= mnemonic.toMnemonicPhrase();
                //displayMesssage("Generated mnemonic phrase: " + mnemonic.toMnemonicPhrase());

                Log.i(TAG, "Binary seed: " + Hex.toHexString(mnemonic.toBinarySeed()));
                msg0 = getMsg0() + "\n"+"Binary seed: " + Hex.toHexString(mnemonic.toBinarySeed());
                txt+="\n"+"Binary seed: " + Hex.toHexString(mnemonic.toBinarySeed());
            }

            if (info.hasCredentialsManagementCapability()) {
                // PIN authentication allows execution of privileged commands
                cmdSet.verifyPIN(getPIN()).checkAuthOK();

                Log.i(TAG, "Pin Verified.");
                msg0 = getMsg0() + "\n"+"Pin Verified.";
            }

            // If the card has no keys, we generate a new set. Keys can also be loaded on the card starting from a binary
            // seed generated from a mnemonic phrase. In alternative, we could load the generated keypair as shown in the
            // commented line of code.
            if (!status.hasMasterKey() && info.hasKeyManagementCapability()) {
                //cmdSet.generateKey();
                cmdSet.loadKey(mnemonic.toBIP32KeyPair()).checkOK();
                //we need to do the same for ED25519
                //eg toSLIP10KeyPair()
                CardFunctions.loadKey_ed25519(channel,new Crypto().MnemonicsToSLIP10KeyPair(mnemonic),false);

                /*
                Crypto.SLIP10KeyPair kp = new Crypto().MnemonicsToSLIP10KeyPair(mnemonic);
                boolean omitPublic= false;

                cmdSet.loadKey(kp.toTLV(!omitPublic), LOAD_KEY_ED25519_P1).checkOK();
                 */

            }

            //this is to be done in the wallet functions

            /*
            // Get the current key path using GET STATUS
            KeyPath currentPath = new KeyPath(cmdSet.getStatus(KeycardCommandSet.GET_STATUS_P1_KEY_PATH).checkOK().getData());
            Log.i(TAG, "Current key path: " + currentPath);
            msg0 = getMsg0() + "\n"+"Current key path: " + currentPath;
            if (!currentPath.toString().equals("m/44'/0'/0'/0/0")) {
                // Key derivation is needed to select the desired key. The derived key remains current until a new derive
                // command is sent (it is not lost on power loss).
                cmdSet.deriveKey("m/44'/0'/0'/0/0").checkOK();
                Log.i(TAG, "Derived m/44'/0'/0'/0/0");
                msg0 = getMsg0() + "\n"+"Derived m/44'/0'/0'/0/0";
            }

            // We retrieve the wallet public key
            BIP32KeyPair walletPublicKey = BIP32KeyPair.fromTLV(cmdSet.exportCurrentKey(true).checkOK().getData());

            Log.i(TAG, "Wallet public key: " + Hex.toHexString(walletPublicKey.getPublicKey()));
            Log.i(TAG, "Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress()));
            txt +="\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());

            msg0 = getMsg0() + "\n"+ Hex.toHexString(walletPublicKey.getPublicKey());
            msg0 = getMsg0() + "\n"+"Wallet address: " + Hex.toHexString(walletPublicKey.toEthereumAddress());
            byte[] hash = "thiscouldbeahashintheorysoitisok".getBytes();

            RecoverableSignature signature = new RecoverableSignature(hash, cmdSet.sign(hash).checkOK().getData());

            Log.i(TAG, "Signed hash: " + Hex.toHexString(hash));
            msg0 = getMsg0() + "\n"+"Signed hash: " + Hex.toHexString(hash);
            txt+="\n"+"Signed hash: " + Hex.toHexString(hash);
            Log.i(TAG, "Recovery ID: " + signature.getRecId());
            msg0 = getMsg0() + "\n"+"Recovery ID: " + signature.getRecId();
            txt+="\n"+"Recovery ID: " + signature.getRecId();
            Log.i(TAG, "R: " + Hex.toHexString(signature.getR()));
            msg0 = getMsg0() + "\n"+"R: " + Hex.toHexString(signature.getR());
            txt+="\n"+"R: " + Hex.toHexString(signature.getR());
            Log.i(TAG, "S: " + Hex.toHexString(signature.getS()));
            msg0 = getMsg0() + "\n"+"S: " + Hex.toHexString(signature.getS());
            txt+="\n"+"S: " + Hex.toHexString(signature.getS());
            */

            if (info.hasSecureChannelCapability()) {
                // Cleanup, in a real application you would not unpair and instead keep the pairing key for successive interactions.
                // We also remove all other pairings so that we do not fill all slots with failing runs. Again in real application
                // this would be a very bad idea to do.
                cmdSet.unpairOthers();
                cmdSet.autoUnpair();

                Log.i(TAG, "Unpaired.");
                msg0 = getMsg0() + "\n"+"Unpaired.";
            }

          //  displayMesssage(txt);
          //  displayMesssage("Card has been initialized  with test secrets"+"\n"+"PIN='"+PIN+"' PUK='"+PUK+"' pairing password='"+PASSWORD+"'"+"\n"+MNEMONIC);
          //  ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
           // ClipData clip = ClipData.newPlainText("credentials", "Card has been initialized  with test secrets"+"\n"+"PIN='"+PIN+"' PUK='"+PUK+"' pairing password='"+PASSWORD+"'"+"\n"+MNEMONIC);
            //clipboard.setPrimaryClip(clip);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            msg0 = getMsg0() + "\n"+e.getMessage();
         //   throw new Exception(e.getMessage());
        }

    }


    public static ApplicationInfo getInfo(CardChannel cardChannel) {

        try {

            KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);
            ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());

           return info;

        }
        catch (Exception ex)
        {
            return null;
        }

    }

    private static void setChannel(CardChannel channel) {
        CardFunctions.channel = channel;
    }

    public static CardChannel getChannel() {
        return channel;
    }

    public static String getMsg0() {
        return msg0;
    }

    public static String getTxtInfo() {
        return txtInfo;
    }

    public static void generatePIN() {

        msg0 = getMsg0() + "\n"+"PIN is not provided by user: Generating PIN randomly";
        for (int i = 0; i < 6; i++) {
            PIN = PIN + rnd.nextInt(10);
        }


    }

    public static void resetPIN() {
        PIN="";
    }

    public static void setPIN(String s) {

        PIN=s;
    }

    public static void generateSECRET()
    {
        PASSWORD =generateRandomWord(11);
    }
    public static void generatePUK() {


        for(int i=0;i<12;i++)
        {
            PUK = PUK + rnd.nextInt(10);
        }

    }

    public static void setMsg0(String s) {

        msg0=s;
    }

    public static signature_data signtool(CardChannel cardChannel, byte[] data, int curve)
    {

        try {


            KeycardCommandSet cmdSet = new KeycardCommandSet(cardChannel);
            ApplicationInfo info = new ApplicationInfo(cmdSet.select().checkOK().getData());
            if (!info.isInitializedCard()) {
               return null;
            }

            if(!info.hasMasterKey())
            {
                return null;
            }

            //cmdSet.autoPair("KeycardTest");
            cmdSet.autoPair(getPASSWORD());
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
            byte[] hsh= Crypto.hashData(data);
            // byte[] hsh=data;

            Log.i(TAG, "SHA-256 result is " + Hex.toHexString(hsh));
            msg0+="\n"+"SHA-256 result is " +  Hex.toHexString(hsh);
            APDUResponse response=null;
            byte[] sign_hash=null;
            byte[] pubK=null;

            if(curve==2)
            {
                response = cmdSet.sign(hsh,0x20);

                byte[] res= response.getData();
                pubK=subArray(res,5,5+31);
                sign_hash=subArray(res,5+32,5+32+63);

            }
            else
            {
                response = cmdSet.sign(hsh);

                byte[] res= response.getData();
                pubK=subArray(res,5,5+64);
                sign_hash=subArray(res,5+65,5+65+63);

            }

            //   response.checkOK();


            cmdSet.unpairOthers();
            cmdSet.autoUnpair();

            Log.i(TAG, "Unpaired.");
            msg0+="\n"+"Unpaired.";


            Log.i(TAG, "signature= " + Hex.toHexString(sign_hash));
            Log.i(TAG, "pubK= " + Hex.toHexString(pubK));

            msg0+="signature= " + Hex.toHexString(sign_hash);
            msg0+="pubK= " + Hex.toHexString(pubK);

            signature_data sign_data = new signature_data();

            sign_data.set_signature_data(sign_hash,pubK,curve);

             return sign_data;

        }
        catch (Exception ex)
        {
            msg0+="\n"+"error:"+ex.getMessage();
            Log.e(TAG,"error:"+ex.getMessage());

        }


        return null;
    }
}
