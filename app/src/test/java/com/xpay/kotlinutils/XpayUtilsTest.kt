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
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountData
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountResponse
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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

    fun userSuccess(res: PrepareAmountData) {
    }

    fun userFailure(res: String) {
    }

    fun paySuccess(res: PayData) {
    }

    fun payFailure(res: String) {
    }


    @Test(expected = IllegalArgumentException::class)
    fun prepareAmount_noSettings_throwserror() {
        XpayUtils.prepareAmount(50, ::userSuccess, ::userFailure)
    }

    @Test(expected = IllegalArgumentException::class)
    fun pay_noSettings_throwsError() {
        XpayUtils.pay(::paySuccess, ::payFailure)
    }

    @Test
    fun prepareAmount_allSettings_throwserror() {
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
        val prepareData: PrepareAmountData? = null
        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(FileUtils.readTestResourceFile("PrepareAmountResponse.json")))
        XpayUtils.prepareAmount(80, ::userSuccess, ::userFailure)
        print(prepareDataObject)
        // assertion
        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/prepare-amount/", request.path)
        assertNotNull(request.getHeader("x-api-key"))
//        println(request.body)
    }

    @Test(expected = IllegalArgumentException::class)
    fun pay_noSettings_pay_using_throwsError() {
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.prepareAmount(50, ::userSuccess, ::userFailure)
        XpayUtils.request = serviceRequest
        XpayUtils.pay(::paySuccess, ::payFailure)
    }

    @Test(expected = KotlinNullPointerException::class)
    fun pay_paymentMethodNotAvailable_throwsError() {
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.PaymentOptionsTotalAmounts =
            PaymentOptionsTotalAmounts(cash = 50.0, kiosk = 52.85)
        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201226476026")
        XpayUtils.request = serviceRequest
        XpayUtils.pay(::paySuccess, ::payFailure)
    }

    @Test
    fun pay_allSetting_passDataSuccessfully() {
        // test settings
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201226476026")
        XpayUtils.request = serviceRequest

        mockWebServer.enqueue(MockResponse().setBody(FileUtils.readTestResourceFile("PayResponse.json")))
        XpayUtils.pay(::paySuccess, ::payFailure)

        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/pay/variable-amount", request.path)
        assertNotNull(request.getHeader("x-api-key"))
        println(request.requestUrl)
    }

    @Test(expected = IllegalArgumentException::class) // User information is not set
    fun pay_userInfoNotSet_throwError() {

        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.payUsing = PaymentMethods.KIOSK
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.request = serviceRequest

        mockWebServer.enqueue(MockResponse().setBody(FileUtils.readTestResourceFile("PayResponse.json")))
        XpayUtils.pay({ x: PayData -> println(x) }, ::payFailure)

        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/pay/variable-amount", request.path)
        assertNotNull(request.getHeader("x-api-key"))
        println(request.requestUrl)
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