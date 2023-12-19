package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class InitializeActivity_ extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize3);

        CardFunctions.generatePUK();
        CardFunctions.generateSECRET();

        //display them on the screen

    }

    public void next(View v)
    {

//launch initialization


        startActivity(new Intent(InitializeActivity_.this, SeedActivity.class));
    }

}