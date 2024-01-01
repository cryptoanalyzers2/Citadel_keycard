package com.example.testkeycard4;

import static com.example.testkeycard4.MainActivity2.MAX_TRY;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import im.status.keycard.io.CardChannel;

public class HederaWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hedera_wallet);
    }

    public void genHederaWallet(View v)
    {

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

        try {

            CardFunctions.generateHEDERAWallet(channel);
        }
        catch(Exception ex)
        {
            Utility.displayMesssage("HEDERA wallet generation failed. Reason="+ ex.getMessage(),new AlertDialog.Builder(this));
            return;
        }



        EditText txtHBARpubK = (EditText) findViewById(R.id.text_hedera_pubK);

        txtHBARpubK.setText( CardFunctions.getHBARPUBLICKEY());



    }
}