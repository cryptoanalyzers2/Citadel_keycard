package com.example.testkeycard4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
    }

    public void home(View v)
    {
        startActivity(new Intent(WalletActivity.this, MainActivity2.class));
    }

    public void BITCOIN(View v)
    {
        startActivity(new Intent(WalletActivity.this, BTCWalletActivity.class));
    }

    public void HEDERA(View v)
    {
        startActivity(new Intent(WalletActivity.this, HederaWalletActivity.class));
    }
}