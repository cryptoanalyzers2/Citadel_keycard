package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import im.status.keycard.applet.ApplicationInfo;
import im.status.keycard.io.CardChannel;

public class MainActivity2 extends AppCompatActivity {

    AlertDialog.Builder builder;

    static final int MAX_TRY =200;


    public state getCurrent_state() {
        return current_state;
    }

    enum state
    {
        FACTORY,
        INITIALIZED,
        BLOCKED,
        BRICKED
    }

    private state current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        builder= new AlertDialog.Builder(this);
    }

    public void displayMesssage(String msg) {
        Utility.displayMesssage(msg, builder);

    }

    public void citadel_settings(View v)
    {

        startActivity(new Intent(MainActivity2.this, SettingsActivity2.class));

    }

    public void citadel_wallet(View v)
    {

        boolean cc= Configuration.isUseNetCardChannel();
        CardFunctions.getCardChannel(cc);
        CardChannel channel=null;

        if(cc==false) {
            displayMesssage("tap card");

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

        ApplicationInfo info = CardFunctions.getInfo(channel);


        boolean isInit= info.isInitializedCard();

        if(isInit==false)
        {
            displayMesssage("Initialize card first");
            return;
        }

        startActivity(new Intent(MainActivity2.this, WalletActivity.class));

    }
public void citadel_initializa(View v) {
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



    ApplicationInfo info = CardFunctions.getInfo(channel);


   boolean isInit= info.isInitializedCard();

   if(isInit==true)
   {
       displayMesssage("card already initialized");
       return;
   }
/*
    If set in an Intent passed to Context.startActivity(), this flag will cause the launched activity to be brought to the front of its task's history stack if it is already running.
*/
   //else we start the initialization screens process
    startActivity(new Intent(MainActivity2.this, PINCreateActivity.class));


}


}