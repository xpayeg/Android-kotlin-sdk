package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import java.util.regex.Matcher
import java.util.regex.Pattern

object XpayUtils {
    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To Xpay Sdk", Toast.LENGTH_LONG).show();
    }

    fun isPalindromeString(inputStr: String): Boolean {
        val sb = StringBuilder(inputStr)

        val reverseStr = sb.reverse().toString()

        return inputStr.equals(reverseStr, ignoreCase = true)
    }

    fun emailValidator(email: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
        return matcher.matches()
    }
}