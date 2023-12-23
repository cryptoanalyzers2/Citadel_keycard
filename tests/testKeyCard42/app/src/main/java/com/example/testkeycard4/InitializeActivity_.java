package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        if(Configuration.isUseNetCardChannel()==false)
        {
            Utility.displayMesssage("Tap Card for initialization",new AlertDialog.Builder(this));
        }

        CardFunctions.getCardChannel(Configuration.isUseNetCardChannel());

        CardFunctions.Initialize(CardFunctions.getChannel());
/*
    If set in an Intent passed to Context.startActivity(), this flag will cause the launched activity to be brought to the front of its task's history stack if it is already running.
*/
        startActivity(new Intent(InitializeActivity_.this, SeedActivity.class));
    }

}