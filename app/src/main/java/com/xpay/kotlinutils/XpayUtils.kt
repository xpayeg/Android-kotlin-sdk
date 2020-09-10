package com.xpay.kotlinutils

import android.content.Context
import android.widget.Toast
import api.ServiceBuilder
import com.xpay.kotlin.models.Data
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.model.BillingInfo
import com.xpay.kotlinutils.model.PrepareResponse
import com.xpay.kotlinutils.model.Request
import com.xpay.kotlinutils.model.Transaction
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object XpayUtils {

    var apiKey: String? = null
    var billingInfo: BillingInfo? = null
    var payload: Request? = null

    fun welcomeMessage(context: Context) {
        Toast.makeText(context, "Welcome To Xpay Sdk", Toast.LENGTH_LONG).show();
    }

    fun getTransaction(
        token:String,
        communityID:String,
        transactionUid:String,
        onSuccess: (Transaction) -> Unit,
        onFail: (String) -> Unit
    ){
        val request = ServiceBuilder.buildService(Xpay::class.java)
        val call = request.getTransaction(token,communityID,transactionUid)
        call.enqueue(object : Callback<Transaction>{
            override fun onResponse(call: Call<Transaction>, response: Response<Transaction>) {
                if (response.body() != null && response.isSuccessful) {
                    println(response.body())
                    onSuccess(response.body()!!)
                }
            }
            override fun onFailure(call: Call<Transaction>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }

    fun getUserInfo(
        id: String,
        token: String,
        onSuccess: (String) -> Unit,
        onFail: (String) -> Unit
    ) {
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

    fun payBill(
        token:String,
        body:HashMap<String,Any>
    ){
        val billingData:HashMap<String,Any> = HashMap<String,Any>()
        val requestBody:HashMap<String,Any> = HashMap<String,Any>()

        billingData.put("name","Mahmoud Aziz")
        billingData.put("email","mabdelaziz@xpay.app")
        billingData.put("phone_number","+201226476026")
        requestBody.put("amount",150)
        requestBody.put("currency","EGP")
        requestBody.put("variable_amount_id",18)
        requestBody.put("community_id","zogDmQW")
        requestBody.put("pay_using","card")
        requestBody.put("billing_data",billingData)

        val request = ServiceBuilder.buildService(Xpay::class.java)
        val call =request.payBill(token,requestBody)
        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() != null && response.isSuccessful) {
                    println("Success Payment")
                    print(response.body()!!.string())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(t.message)
            }
        })
    }

    fun prepareAmount(
        token: String,
        amount: Number,
        communityID: String,
        onSuccess: (Data) -> Unit,
        onFail: (String) -> Unit
    ) {
        val hashMap: HashMap<String, Any> = HashMap<String, Any>()
        hashMap.put("amount", amount)
        hashMap.put("community_id", communityID)
        val request = ServiceBuilder.buildService(Xpay::class.java)
        val call = request.prepareAmount(hashMap, token)
        call.enqueue(object : Callback<PrepareResponse> {
            override fun onResponse(
                call: Call<PrepareResponse>,
                response: Response<PrepareResponse>
            ) {
                if (response.body() != null && response.isSuccessful) {
                    println(response.body())
                    onSuccess(response.body()!!.data)
                }
            }

            override fun onFailure(call: Call<PrepareResponse>, t: Throwable) {
                onFail(t.message.toString())
            }
        })
    }


}