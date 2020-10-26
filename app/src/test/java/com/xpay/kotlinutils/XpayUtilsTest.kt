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
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test


class XpayUtilsTest {
    private val mockWebServer = MockWebServer()
    private val serviceRequest =
        ServiceBuilder(ServerSetting.TEST, true).xpayService(Xpay::class.java)

    @Before
    fun setUp() {
        mockWebServer.start(8080)
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
        XpayUtils.request = serviceRequest
        val prepareAmountResponseBody = FileUtils.readTestResourceFile("PrepareAmountResponse.json")

        val gson = Gson()
        val listPersonType = object : TypeToken<PrepareAmountResponse>() {}.type
        val prepareAmountMock: PrepareAmountResponse =
            gson.fromJson(prepareAmountResponseBody, listPersonType)
        val prepareDataObject: PrepareAmountData = prepareAmountMock.data
        var prepareData: PrepareAmountData? = null

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


    // check that prepare amount sets payment options successfully
    @Test
    fun prepareAmount_setsPaymentOptionsSuccessfully() {
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.request = serviceRequest
        val prepareAmountResponseBody = FileUtils.readTestResourceFile("PrepareAmountResponse.json")
        mockWebServer.enqueue(MockResponse().setBody(FileUtils.readTestResourceFile("PrepareAmountResponse.json")))

        runBlocking {
            XpayUtils.prepareAmount(80)
        }
        // assertion
        assertFalse(XpayUtils.activePaymentMethods.isEmpty());
    }

    // pay method tests

    // pay returns error without settings
    @Test(expected = IllegalArgumentException::class)
    fun pay_noSettings_throwsError() {
        runBlocking {
            XpayUtils.pay()
        }
    }

    // pay returns error when called without setting pay using
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

    // pay returns error when payment method is not available in payment options
    @Test(expected = IllegalArgumentException::class)
    fun pay_paymentMethodNotAvailable_throwsError() {
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.PaymentOptionsTotalAmounts =
            PaymentOptionsTotalAmounts(cash = 50.0, kiosk = 52.85)
        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.request = serviceRequest
        runBlocking {
            XpayUtils.pay()
        }
    }

    // pay returns error when user informations is missing
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

    // pay sends correct payload
    @Test
    fun pay_sendsCorrectPayload_Passed() {
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
        mockWebServer.enqueue(MockResponse().setBody(payResponseBody))

        runBlocking {
            XpayUtils.pay()
        }
    }

    // pay passes pay to is successful on successful operation
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

    //  pay returns error to is failed (no network errors- server error)
    @Test
    fun pay_returnsErrorToIsFailed_throwsError() {
        // test settings
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "120"
        XpayUtils.variableAmountID = 0
        // simulate prepare amount method
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.activePaymentMethods =
            mutableListOf(PaymentMethods.CARD, PaymentMethods.CASH, PaymentMethods.KIOSK)

        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201226476026")
        XpayUtils.request = serviceRequest
        val payResponseBody = FileUtils.readTestResourceFile("PayResponseError.json")
        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody(payResponseBody))

        runBlocking {
            XpayUtils.pay()
        }
    }

    // pay returns error to is failed (network error)
    @Test
    fun pay_networkError_throwsError() {
        //  SocketPolicy.DISCONNECT_AT_START, SocketPolicy.NO_RESPONSE

        val response = MockResponse()
            .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)

        mockWebServer.enqueue(response)
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