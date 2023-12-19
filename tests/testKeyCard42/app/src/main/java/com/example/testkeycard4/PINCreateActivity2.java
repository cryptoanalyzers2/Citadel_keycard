package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PINCreateActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pincreate2);
    }

    public void next(View v)
    {
        //set PIN


        startActivity(new Intent(PINCreateActivity2.this, InitializeActivity_.class));
    }

}