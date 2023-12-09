package com.example.testkeycard4;

import android.app.AlertDialog;

public class Utility {


    public static void displayMesssage(String msg,  AlertDialog.Builder builder)
    {

        builder.setMessage(msg)
                .setTitle("message");

        builder.create();
        builder.show();

    }

}
