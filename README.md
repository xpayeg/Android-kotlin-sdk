# Android-kotlin-sdk

XPay KotlinUtils is an SDK for integrating payments through XPay with android development written in kotlin.

Docs, Refrences and Tutorial are found [here](https://xpayeg.github.io/docs/android-sdk/installation)

## Installation

KotlinUtils is available through jitpack

1. Add the following line in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependencies

```
dependencies {
	implementation 'com.github.xpayeg:Android-kotlin-sdk:2.0'
  implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3'
  implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha02"
}
```

## Usage

### Make payment

```java
import com.xpay.kotlinutils.XpayUtils
import com.xpay.kotlinutils.models.BillingInfo

// set XpayUtils core settings
// The following settings are for testing purposes only
XpayUtils.apiKey = "Cce74Y3B.J0P4tItq7hGu2ddhCB0WF5ND1eTubkpT"
XpayUtils.communityId = "m2J7eBK"
XpayUtils.apiPaymentId = 60
// default server settings are testing servers which is equivalent to
// XpayUtils.serverSetting = ServerSetting.TEST

lifecycleScope.launch {
    try {
        val res = XpayUtils.prepareAmount(50)
        // read active payment methods list and set payUsing property
        // this test community returns CARD and KIOSK payment options
        // the following line is equivalent to XpayUtils.payUsing = XpayUtils.activePaymentMethods[0]
        XpayUtils.payUsing = PaymentMethods.CARD
        // read the total amount of card payment
        // this is also equivalent to
        // val totalAmount = res?.totalAmount!!
        val totalAmount = XpayUtils.PaymentOptionsTotalAmounts?.card!!
        // set billing information
        XpayUtils.billingInfo = BillingInfo( "John Doe", "j.doe@test.com", "+201111111111")
        // make payment
        val paymentRespone = XpayUtils.pay()
        // get payment form url and navigate to it to complete your payment
        val formUrl = paymentRespone?.iframe_url!!
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(resources.getColor(R.color.colorPrimary))
        builder.setShowTitle(true)
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(this@MainActivity, Uri.parse(response.iframe_url))
        //in case of KIOSK payment method, read the message respone which contains your payment reference number from AMAN
        // val formUrl = paymentRespone?.message!!

        // that's it !
    } catch (e: Exception) {
        e.message?.let { msg -> displayError(msg) }
    }
}
```

### Get transaction info

```java
import com.xpay.kotlinutils.XpayUtils

// set XpayUtils core settings
// The following settings are for testing purposes only
XpayUtils.apiKey = "Cce74Y3B.J0P4tItq7hGu2ddhCB0WF5ND1eTubkpT";
XpayUtils.communityId = "m2J7eBK";
XpayUtils.apiPaymentId = 60;
// default server settings are testing servers which is equivalent to
// XpayUtils.serverSetting = ServerSetting.TEST

lifecycleScope.launch {
    try {
        val response = XpayUtils.getTransaction(it)
        // read transaction status and amount
        val status = respone.status
        val totalAmount = respone.total_amount

        // that's it !
    } catch (e: Exception) {
        e.message?.let { errMsg -> displayError(errMsg) }
    }
}
```

## Switching to Production server

By default XpayUtils will make all the requests to XPay testing server, to switch to Production server set `XpayUtils.serverSetting` property to `ServerSetting.LIVE`.

```
XpayUtils.serverSetting = ServerSetting.LIVE
```

to switch back to the test server:

```
XpayUtils.serverSetting = ServerSetting.TEST
```
