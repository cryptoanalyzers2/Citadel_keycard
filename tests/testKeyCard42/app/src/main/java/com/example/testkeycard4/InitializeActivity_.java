package com.example.testkeycard4;

import static com.example.testkeycard4.MainActivity2.MAX_TRY;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import im.status.keycard.io.CardChannel;

public class InitializeActivity_ extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize3);

        CardFunctions.generatePUK();
        CardFunctions.generateSECRET();

        //display them on the screen

        ((TextView) findViewById(R.id.text_PUK)).setText(CardFunctions.getPUK());
        ((TextView) findViewById(R.id.text_SECRET)).setText(CardFunctions.getPASSWORD());

    }


    public void next(View v)
    {

//launch initialization
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

            CardFunctions.Initialize(channel);
        }
        catch(Exception ex)
        {
            Utility.displayMesssage("Initialization failed. Reason="+ ex.getMessage(),new AlertDialog.Builder(this));
            return;
        }


/*
    If set in an Intent passed to Context.startActivity(), this flag will cause the launched activity to be brought to the front of its task's history stack if it is already running.
*/
        startActivity(new Intent(InitializeActivity_.this, SeedActivity.class));
    }

}