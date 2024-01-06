package com.example.testkeycard4;

import static com.example.testkeycard4.MainActivity2.MAX_TRY;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.bouncycastle.util.encoders.Hex;

import im.status.keycard.io.CardChannel;

public class SignatureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.spinner_algo);
//create a list of items for the spinner.
        String[] items = new String[]{"ECDSA(SECP256K1)", "ECDSA(NIST-P256)", "EDDSA(ED25519)"};
//create an adapter to describe how the items are displayed, adapters are used in several places in android.
//There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
//set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

    }
    public void verify(View v)
    {
        Spinner list = (Spinner) findViewById(R.id.spinner_algo);
        int curve= list.getSelectedItemPosition();

        if(curve==0) {
            EditText txtsign = (EditText) findViewById(R.id.txt_signature_data);
            String hex_to_sign = (String) txtsign.getText().toString();
            EditText txtresult = (EditText) findViewById(R.id.txt_signature_result);
            String hex_result = (String) txtresult.getText().toString();
            EditText txtpubK = (EditText) findViewById(R.id.txt_signature_pubK);
            String hex_pubK = (String) txtpubK.getText().toString();

            //we sign a hash!
            byte[]  hsh= Crypto.hashData(Hex.decode(hex_to_sign));


            boolean res = Crypto.verify_secp256k1_signature(Hex.decode(hex_pubK), hsh, Hex.decode(hex_result));

            if (res == true) {
                Utility.displayMesssage("Signature is valid", new AlertDialog.Builder((this)));
            } else {
                Utility.displayMesssage("Signature is invalid", new AlertDialog.Builder((this)));
            }
        }
        else if(curve==2)
        {
            EditText txtsign = (EditText) findViewById(R.id.txt_signature_data);
            String hex_to_sign = (String) txtsign.getText().toString();
            EditText txtresult = (EditText) findViewById(R.id.txt_signature_result);
            String hex_result = (String) txtresult.getText().toString();
            EditText txtpubK = (EditText) findViewById(R.id.txt_signature_pubK);
            String hex_pubK = (String) txtpubK.getText().toString();

            //we sign a hash!
            byte[]  hsh= Crypto.hashData(Hex.decode(hex_to_sign));

            boolean res = Crypto.verify_ed25519_signature(Hex.decode(hex_pubK),hsh , Hex.decode(hex_result));

            if (res == true) {
                Utility.displayMesssage("Signature is valid", new AlertDialog.Builder((this)));
            } else {
                Utility.displayMesssage("Signature is invalid", new AlertDialog.Builder((this)));
            }
        }
        else
        {
            Utility.displayMesssage("Unsupported curve", new AlertDialog.Builder((this)));
        }


    }

    public void sign(View v)
    {

        try {

            if((CardFunctions.getPIN()=="")||(CardFunctions.getPASSWORD()==""))
            {
                Utility.displayMesssage("Missing PIN and/or Pairing SECRET, use Settings to store them", new AlertDialog.Builder((this)));
                return;
            }

            EditText txtsign = (EditText) findViewById(R.id.txt_signature_data);
            String hex_to_sign = (String) txtsign.getText().toString();
            byte[] bHex_to_sign=null;


            bHex_to_sign = Hex.decode(hex_to_sign);


            if(bHex_to_sign==null)
            {
                Utility.displayMesssage("incorrect hexadecimal data", new AlertDialog.Builder((this)));
                return;
            }

            Spinner list = (Spinner) findViewById(R.id.spinner_algo);
            int curve= list.getSelectedItemPosition();

            boolean cc= Configuration.isUseNetCardChannel();
            CardFunctions.getCardChannel(cc);
            CardChannel channel=null;

            if(cc==false) {
                //  displayMesssage("tap card");
                Utility.displayMesssage("Tap Card",new AlertDialog.Builder(this));
            }

            for(int i=0;i<MAX_TRY;i++)
            {
                channel = CardFunctions.getChannel();

                if(channel!=null)
                {
                    break;
                }

                try
                {
                    Thread.sleep(50,0);
                }
                catch(Exception ex)
                {

                }

            }

            if(channel==null)
            {
                Utility.displayMesssage("no card detected!",new AlertDialog.Builder(this));
                return;
            }

           CardFunctions.signature_data sd= CardFunctions.signtool(channel,bHex_to_sign,curve);

            if(sd!=null)
            {


                EditText txt_sign_res = (EditText) findViewById(R.id.txt_signature_result);
                txt_sign_res.setText(Hex.toHexString(sd.signature));

                EditText txt_sign_pubK = (EditText) findViewById(R.id.txt_signature_pubK);
                txt_sign_pubK.setText(Hex.toHexString(sd.pubKey));

            }
            else
            {
                Utility.displayMesssage("null data returned!",new AlertDialog.Builder(this));
                return;
            }

        }
        catch(Exception ex)
        {
            Utility.displayMesssage(ex.getMessage(), new AlertDialog.Builder((this)));
        }

    }

    public void home(View v)
    {
        startActivity(new Intent(SignatureActivity.this, MainActivity2.class));
    }
}