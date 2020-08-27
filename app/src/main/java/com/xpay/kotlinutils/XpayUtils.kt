package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast

class XpayUtils {
    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To Xpay Sdk", Toast.LENGTH_LONG).show();
    }
}