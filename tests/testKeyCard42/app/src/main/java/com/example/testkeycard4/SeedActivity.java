package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seed);

        //get the mnemonics and display it on-screen
        String mnemonic = CardFunctions.getMNEMONIC();
        String[] words=mnemonic.split(" ");

        int L= words.length;

        ((TextView) findViewById(R.id.text_seed1)).setText(words[0]+" "+words[1]+" "+words[2]+" "+words[3]);
        L-=4;

        if(L==0)
            return;

        ((TextView) findViewById(R.id.text_seed2)).setText(words[4]+" "+words[5]+" "+words[6]+" "+words[7]);

        L-=4;

        if(L==0)
            return;

        ((TextView) findViewById(R.id.text_seed3)).setText(words[8]+" "+words[9]+" "+words[10]+" "+words[11]);


        L-=4;

        if(L==0)
            return;


        ((TextView) findViewById(R.id.text_seed4)).setText(words[12]+" "+words[13]+" "+words[14]+" "+words[15]);


    }

   public  void agree(View v)
    {
     // boolean isAgree=  ((CheckBox) findViewById(R.id.cb_agree)).isChecked();

    //      if(isAgree==true)
     //     {

              ((Button) findViewById(R.id.button_codes_OK)).setEnabled(true);
    //      }

    }

    public void next(View v)
    {
        startActivity(new Intent(SeedActivity.this, MainActivity2.class));
    }
}