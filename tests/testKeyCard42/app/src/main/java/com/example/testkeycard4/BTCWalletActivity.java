package com.example.testkeycard4;

import static com.example.testkeycard4.MainActivity2.MAX_TRY;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import im.status.keycard.io.CardChannel;

public class BTCWalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btcwallet);

        EditText txtBTCAddress = (EditText) findViewById(R.id.txt_BTC);

        txtBTCAddress.setText( CardFunctions.getBTCWALLETADDRESS());

    }

    public void genBTCWallet(View v)
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

            CardFunctions.generateBTCWallet(channel);
        }
        catch(Exception ex)
        {
            Utility.displayMesssage("BTC wallet generation failed. Reason="+ ex.getMessage(),new AlertDialog.Builder(this));
            return;
        }


        EditText txtBTCAddress = (EditText) findViewById(R.id.txt_BTC);

        txtBTCAddress.setText( CardFunctions.getBTCWALLETADDRESS());


    }

}