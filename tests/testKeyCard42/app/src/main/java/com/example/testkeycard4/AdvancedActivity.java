package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AdvancedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);
    }

    public void home(View v)
    {
        startActivity(new Intent(AdvancedActivity.this, MainActivity2.class));
    }

    public void logs(View v)
    {

        Utility.displayMesssage(CardFunctions.getMsg0(),new AlertDialog.Builder(this));


    }
}