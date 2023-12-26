package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity2 extends AppCompatActivity {

    AlertDialog.Builder builder;


    public void close_settings(View v) {

        finish();

    }
public void save_settings(View v)
{

    try {

//is using netCardChannel (vs NFC)
        Switch cbnetcard = (Switch) findViewById(R.id.cb_netcard);
        boolean useNetCard = cbnetcard.isChecked();
        Configuration.setUseNetCardChannel(useNetCard);

//hostname
        TextView txtnetcard = (TextView) findViewById(R.id.txt_netcard);
        String urlNetCard = (String) txtnetcard.getText().toString();

        Configuration.setNetCardChannelHostname(urlNetCard);
//port
        TextView txtnetcardPort = (TextView) findViewById(R.id.txt_netcard_port);
        String netcardPort = (String) txtnetcardPort.getText().toString();




        Configuration.setNetCardChannelPort(netcardPort);

     //   finish();

    }catch(Exception ex)
    {
        Utility.displayMesssage(ex.getMessage(),builder);
    }

}

 void setPIN(View v)
 {

     TextView txtsecret = (TextView) findViewById(R.id.txt_secret);
     CardFunctions.setPASSWORD(txtsecret.getText().toString());
     TextView txtPIN = (TextView) findViewById(R.id.txt_PIN);
     CardFunctions.setPIN(txtPIN.getText().toString());

     Utility.displayMesssage("Pairing password and PIN have been registered", new AlertDialog.Builder(this));

 }

    void load_settings()
    {
//is using netCardChannel (vs NFC)
        Switch cbnetcard = (Switch) findViewById(R.id.cb_netcard);
        boolean useNetCard=Configuration.isUseNetCardChannel();
        cbnetcard.setChecked(useNetCard);

//hostname
        TextView txtnetcard = (TextView) findViewById(R.id.txt_netcard);
        String urlNetCard = Configuration.getNetCardChannelHostname();
        txtnetcard.setText(urlNetCard);

//port
        TextView txtnetcardPort = (TextView) findViewById(R.id.txt_netcard_port);
        String netcardPort = Configuration.getNetCardChannelPort();
        txtnetcardPort.setText(netcardPort);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        builder = new AlertDialog.Builder(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        load_settings();
    }
}