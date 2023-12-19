package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class PINCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_create);
        CardFunctions.resetPIN();
    }

    public void updateSTars()
    {
        Switch sw1 = (Switch) findViewById(R.id.switch_PIN);
        boolean genPIN=sw1.isChecked();

        if(genPIN==true) {

            return;
        
        }
        String stars=((TextView) findViewById(R.id.star_PIN)).getText().toString();
        stars+="*";
        ((TextView) findViewById(R.id.star_PIN)).setText(stars);

    }
    public void pin_0(View v ) {
        PIN_n(0);
    }
    public void pin_1(View v ) {
        PIN_n(1);
    }
    public void pin_2(View v ) {
        PIN_n(2);
    }
    public void pin_3(View v ) {
        PIN_n(3);
    }
    public void pin_4(View v ) {
        PIN_n(4);
    }
    public void pin_5(View v ) {
        PIN_n(5);
    }
    public void pin_6(View v ) {
        PIN_n(6);
    }
    public void pin_7(View v ) {
        PIN_n(7);
    }
    public void pin_8(View v ) {
        PIN_n(8);
    }
    public void pin_9(View v ) {
        PIN_n(9);
    }

    public void PIN_n(int n)
    {

        Switch sw1 = (Switch) findViewById(R.id.switch_PIN);
        boolean genPIN=sw1.isChecked();

        if(genPIN==true) {
            return;
        }

        if(CardFunctions.getPIN().length()>5)
        {
            return;
        }

        CardFunctions.setPIN(CardFunctions.getPIN()+n);
        updateSTars();

    }

    public void next(View v)
    {
        Switch sw1 = (Switch) findViewById(R.id.switch_PIN);
        boolean genPIN=sw1.isChecked();

        if(genPIN==true) {

        CardFunctions.generatePIN();

        startActivity(new Intent(PINCreateActivity.this, InitializeActivity_.class));
        }
        else {
            //check if we have 6 digits
            if(CardFunctions.getPIN().length()<6)
            {
                return;
            }

            startActivity(new Intent(PINCreateActivity.this, PINCreateActivity2.class));
        }

    }
}