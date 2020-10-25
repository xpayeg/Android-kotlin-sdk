package com.xpay.kotlinutils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xpay.kotlinutils.api.ServiceBuilder
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.models.PaymentMethods
import com.xpay.kotlinutils.models.PaymentOptionsTotalAmounts
import com.xpay.kotlinutils.models.ServerSetting
import com.xpay.kotlinutils.models.User
import com.xpay.kotlinutils.models.api.pay.PayData
import com.xpay.kotlinutils.models.api.pay.PayResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountData
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountResponse
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit


class XpayUtilsTest {
    private val mockWebServer = MockWebServer()
    private val serviceRequest =
        ServiceBuilder(ServerSetting.TEST, true).xpayService(Xpay::class.java)

    @Before
    fun setUp() {
        mockWebServer.start(8080)
    }

    fun userSuccess(res: PrepareAmountData) {
    }

    fun userFailure(res: String) {
    }

    fun paySuccess(res: PayData) {
    }

    fun payFailure(res: String) {
    }


    // Prepare method tests

    @Test(expected = IllegalArgumentException::class)
    fun prepareAmount_noSettings_throwserror() {
        runBlocking {
            XpayUtils.prepareAmount(50)
        }
    }

    @Test
    fun prepareAmount_allSettings_passDataSuccessfully() {
        // test settings
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        val serviceRequest = ServiceBuilder(ServerSetting.TEST, true).xpayService(Xpay::class.java)
        XpayUtils.request = serviceRequest
        val prepareAmountResponseBody = FileUtils.readTestResourceFile("PrepareAmountResponse.json")

        val gson = Gson()
        val listPersonType = object : TypeToken<PrepareAmountResponse>() {}.type
        val prepareAmountMock: PrepareAmountResponse =
            gson.fromJson(prepareAmountResponseBody, listPersonType)
        val prepareDataObject: PrepareAmountData = prepareAmountMock.data
        var prepareData: PrepareAmountData?

        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(FileUtils.readTestResourceFile("PrepareAmountResponse.json")))

        // run
        runBlocking {
            prepareData = XpayUtils.prepareAmount(80)
        }

        // assertion
        assertEquals(prepareData, prepareDataObject)
        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/prepare-amount/", request.path)
        assertEquals(request.getHeader("x-api-key"), XpayUtils.apiKey)
    }

    // pay method tests

    @Test(expected = IllegalArgumentException::class)
    fun pay_noSettings_throwsError() {
        runBlocking {
            XpayUtils.pay()
        }
    }


    @Test(expected = IllegalArgumentException::class)
    fun pay_payUsingNotDefined_throwsError() {
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.request = serviceRequest
        runBlocking {
            XpayUtils.pay()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun pay_paymentMethodNotAvailable_throwsError() {
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.PaymentOptionsTotalAmounts =
            PaymentOptionsTotalAmounts(cash = 50.0, kiosk = 52.85)
//        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201226476026")
        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.request = serviceRequest
        runBlocking {
            XpayUtils.pay()
        }
    }

    // User information is not set
    @Test(expected = IllegalArgumentException::class)
    fun pay_userInfoNotSet_throwError() {

        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.payUsing = PaymentMethods.KIOSK
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.request = serviceRequest
        runBlocking {
            XpayUtils.pay()
        }
    }


    @Test
    fun pay_allSetting_passDataSuccessfully() {
        // test settings
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18

        // simulate prepare amount method
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.activePaymentMethods =
            mutableListOf(PaymentMethods.CARD, PaymentMethods.CASH, PaymentMethods.KIOSK)

        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201226476026")
        XpayUtils.request = serviceRequest
        val payResponseBody = FileUtils.readTestResourceFile("PayResponse.json")

        val gson = Gson()
        val x = object : TypeToken<PayResponse>() {}.type
        val payMock: PayResponse =
            gson.fromJson(payResponseBody, x)
        val payObject: PayData = payMock.data
        var y: PayData? = null

        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(payResponseBody))
        // run methods
        runBlocking {
            y = XpayUtils.pay()
        }

        // assertion
        assertEquals(y, payObject)
        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/pay/variable-amount", request.path)
        assertEquals(request.getHeader("x-api-key"), XpayUtils.apiKey)
    }


    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}

object FileUtils {
    fun readTestResourceFile(fileName: String): String {
        val fileInputStream = javaClass.classLoader?.getResourceAsStream(fileName)
        return fileInputStream?.bufferedReader()?.readText() ?: ""
    }
}