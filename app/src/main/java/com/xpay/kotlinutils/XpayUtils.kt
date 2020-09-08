package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import api.ServiceBuilder
import api.TmdbEndpoints
import api.Xpay
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    fun getTotalMovies(key: String, onSuccess: (String) -> String, onFail: (String) -> String) {
        val request = ServiceBuilder.buildService(TmdbEndpoints::class.java)
        val call = request.getMovies(key)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                response.body()?.string()?.let { onSuccess(it) }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }

    fun getUserInfo(id: Int, token: String, onSuccess: (String) -> Unit, onFail: (String) -> Unit) {
        val request = ServiceBuilder.xpayService(Xpay::class.java)
        val call = request.userInfo(token, id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                response.body()?.string()?.let { onSuccess(it) }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }

    fun emailValidator(email: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email.toString())
        return matcher.matches()
    }

}