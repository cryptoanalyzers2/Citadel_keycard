package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import im.status.keycard.io.CardChannel;

public class PINCreateActivity2 extends AppCompatActivity {

    String PIN="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pincreate2);

        PIN=CardFunctions.getPIN();
        CardFunctions.resetPIN();
    }

    public void deleteSTar()
    {

        String stars=((TextView) findViewById(R.id.star_PIN)).getText().toString();
        if((stars==null)||(stars.length()==0)){
            return;
        }
        int L=stars.length();
        ((TextView) findViewById(R.id.star_PIN)).setText(stars.substring(0,L-1));

    }


    public void pin_D(View v)
    {
        String PIN = CardFunctions.getPIN();
        if((PIN==null)||(PIN.length()==0))
        {
            return;
        }
        int L=PIN.length();
        CardFunctions.setPIN(PIN.substring(0,L-1));
        deleteSTar();


    }


    public void updateSTars()
    {

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

        if(CardFunctions.getPIN().length()>5)
        {
            return;
        }

        CardFunctions.setPIN(CardFunctions.getPIN()+n);
        updateSTars();

    }

    public void next(View v)
    {
        //set PIN
       String PIN2 = CardFunctions.getPIN();

if(PIN.equals(PIN2)==false)
{
    Utility.displayMesssage("PIN does not match",new AlertDialog.Builder(this));
    return;
}

        startActivity(new Intent(PINCreateActivity2.this, InitializeActivity_.class));
    }

}