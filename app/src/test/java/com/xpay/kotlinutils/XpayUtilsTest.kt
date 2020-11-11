package com.xpay.kotlinutils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xpay.kotlinutils.api.ServiceBuilder
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.models.*
import com.xpay.kotlinutils.models.api.pay.PayData
import com.xpay.kotlinutils.models.api.pay.PayResponse
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountData
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountResponse
import com.xpay.kotlinutils.models.api.transaction.TransactionData
import com.xpay.kotlinutils.models.api.transaction.TransactionResponse
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException


class XpayUtilsTest {
    private val mockWebServer = MockWebServer()
    private val serviceRequest =
        ServiceBuilder(ServerSetting.TEST, true).xpayService(Xpay::class.java)

    @Before
    fun setUp() {
        reset()
        mockWebServer.start(8080)
    }

    fun reset() {
        XpayUtils.apiKey = null
        XpayUtils.communityId = null
        XpayUtils.variableAmountID = null
        XpayUtils.activePaymentMethods.clear()

        XpayUtils.PaymentOptionsTotalAmounts = null
        XpayUtils.payUsing = null
        XpayUtils.userInfo = null
        XpayUtils.ShippingInfo = null
    }

    fun setSettings() {
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        XpayUtils.request = serviceRequest
    }

    //test rules
    @Rule
    @JvmField
    var exceptionRule: ExpectedException = ExpectedException.none()

    // Prepare method tests

    // throws error when no settings are found
    @Test
    fun prepareAmount_noApiKey_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("API key is not ")

        // set settings
        setSettings()
        XpayUtils.apiKey = null

        runBlocking {
            XpayUtils.prepareAmount(50)
        }
    }

    @Test
    fun prepareAmount_noCommunityId_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("Community ID is not set")

        // set settings
        setSettings()
        XpayUtils.communityId = null

        // run method
        runBlocking {
            XpayUtils.prepareAmount(50)
        }
    }

    @Test
    fun prepareAmount_noVariableAmountId_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("API Payment ID is not set")

        // set settings
        setSettings()
        XpayUtils.variableAmountID = null

        // run method
        runBlocking {
            XpayUtils.prepareAmount(50)
        }
    }

    @Test
    fun prepareAmount_allSettings_makeRequestSuccessfully() {
        // test settings
        setSettings()

        val prepareAmountResponseBody = FileUtils.readTestResourceFile("PrepareAmountResponse.json")

        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(prepareAmountResponseBody))

        // run
        runBlocking {
            XpayUtils.prepareAmount(50)
        }

        // assertion
        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/prepare-amount/", request.path)
        assertEquals(request.getHeader("x-api-key"), XpayUtils.apiKey)
    }


    @Test
    fun prepareAmount_allSettings_returnsDataSuccessfully() {
        // test settings
        setSettings()

        val prepareAmountResponseBody = FileUtils.readTestResourceFile("PrepareAmountResponse.json")
        val gson = Gson()
        val responseType = object : TypeToken<PrepareAmountResponse>() {}.type
        val responseMock: PrepareAmountResponse =
            gson.fromJson(prepareAmountResponseBody, responseType)
        val prepareDataObject: PrepareAmountData = responseMock.data
        var prepareData: PrepareAmountData? = null

        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(prepareAmountResponseBody))

        // run
        runBlocking {
            prepareData = XpayUtils.prepareAmount(50)
        }

        // assertion
        assertEquals(prepareData, prepareDataObject)
    }


    // check that prepare amount sets payment options successfully
    @Test
    fun prepareAmount_allSettings_setsPaymentOptionsSuccessfully() {
        // set
        setSettings()
        val testPaymentMethods = mutableListOf<PaymentMethods>(
            PaymentMethods.CARD,
            PaymentMethods.CASH,
            PaymentMethods.KIOSK
        )
        mockWebServer.enqueue(MockResponse().setBody(FileUtils.readTestResourceFile("PrepareAmountResponse.json")))

        // run
        runBlocking {
            XpayUtils.prepareAmount(50)
        }

        // assertion
        assertEquals(XpayUtils.activePaymentMethods, testPaymentMethods)
    }

    // check that prepare amount set total prepared amount successfully
    @Test
    fun prepareAmount_setsTotalPreparedAmountSuccessfully_isPassed() {

        setSettings()
        val testPaymentOptionsTotalAmounts =
            PaymentOptionsTotalAmounts(card = 5002.0, cash = 5000.0, kiosk = 5285.0)
        val prepareAmountResponseBody = FileUtils.readTestResourceFile("PrepareAmountResponse.json")
        mockWebServer.enqueue(MockResponse().setBody(prepareAmountResponseBody))

        runBlocking {
            XpayUtils.prepareAmount(50)
        }

        // assertion
        assertEquals(XpayUtils.PaymentOptionsTotalAmounts, testPaymentOptionsTotalAmounts)
    }

    // check that prepare amount returns error to is failed (server error)
    @Test
    fun prepareAmount_serverError_throwsError() {
        // get error response from resources
        val responseBody = FileUtils.readTestResourceFile("PrepareResponseError.json")
        // get error string
        val gson = Gson()
        val responseBodyType = object : TypeToken<PrepareAmountResponse>() {}.type
        val prepareAmountMock: PrepareAmountResponse =
            gson.fromJson(responseBody, responseBodyType)
        val prepareDataObject: String = prepareAmountMock.status.errors[0].toString()
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage(prepareDataObject)

        setSettings()

        // Schedule server error response
        val response = MockResponse()
            .setResponseCode(400)
            .setBody(responseBody)
        mockWebServer.enqueue(response)

        runBlocking {
            XpayUtils.prepareAmount(2)
        }
    }

    // pay method tests
    // throws error when no settings are found
    @Test
    fun pay_noApiKey_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("API key is not set")

        // set settings
        setSettings()
        XpayUtils.apiKey = null

        runBlocking {
            XpayUtils.pay()
        }
    }

    @Test
    fun pay_noCommunityId_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("Community ID is not set")

        // set settings
        setSettings()
        XpayUtils.communityId = null

        // run method
        runBlocking {
            XpayUtils.pay()
        }
    }

    @Test
    fun pay_noVariableAmountId_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("API Payment ID is not set")

        // set settings
        setSettings()
        XpayUtils.variableAmountID = null

        // run method
        runBlocking {
            XpayUtils.prepareAmount(50)
        }
    }

    // pay returns error when called without setting pay using
    @Test
    fun pay_payUsingNotDefined_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("Payment method is not set")

        setSettings()

        runBlocking {
            XpayUtils.pay()
        }
    }

    // pay returns error when payment method is not available in payment options
    @Test
    fun pay_paymentMethodNotAvailable_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("Payment method is not available")

        setSettings()
        XpayUtils.activePaymentMethods =
            mutableListOf(PaymentMethods.CASH, PaymentMethods.KIOSK)
        XpayUtils.payUsing = PaymentMethods.CARD

        runBlocking {
            XpayUtils.pay()
        }
    }

    // pay returns error when user information is missing
    @Test
    fun pay_userInfoNotFound_throwsError() {

        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("User information is not set")

        setSettings()
        XpayUtils.activePaymentMethods =
            mutableListOf(PaymentMethods.CASH, PaymentMethods.KIOSK)
        XpayUtils.payUsing = PaymentMethods.KIOSK
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)

        runBlocking {
            XpayUtils.pay()
        }
    }

    // pay sends correct payload
    @Test
    fun pay_allSettings_returnsPayDataSuccessfully() {
        // test settings
        setSettings()
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.activePaymentMethods =
            mutableListOf(PaymentMethods.CARD, PaymentMethods.CASH, PaymentMethods.KIOSK)
        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201111111111")

        val payResponseBody = FileUtils.readTestResourceFile("PayResponse.json")
        mockWebServer.enqueue(MockResponse().setBody(payResponseBody))

        val gson = Gson()
        val responseType = object : TypeToken<PayResponse>() {}.type
        val payMock: PayResponse =
            gson.fromJson(payResponseBody, responseType)
        val payDataObject: PayData = payMock.data
        var payResponseData: PayData? = null

        runBlocking {
            payResponseData = XpayUtils.pay()
        }

        // assertion
        assertEquals(payResponseData, payDataObject)
    }

    @Test
    fun pay_allSettings_makeRequestSuccessfully() {
        // test settings
        setSettings()
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.activePaymentMethods =
            mutableListOf(PaymentMethods.CARD, PaymentMethods.CASH, PaymentMethods.KIOSK)
        XpayUtils.payUsing = PaymentMethods.CARD
        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201226476026")

        val payResponseBody = FileUtils.readTestResourceFile("PayResponse.json")
        mockWebServer.enqueue(MockResponse().setBody(payResponseBody))

        runBlocking {
            XpayUtils.pay()
        }

        // assertion
        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/pay/variable-amount", request.path)
        assertEquals(request.getHeader("x-api-key"), XpayUtils.apiKey)
    }


    //  pay returns error to is failed (server error)
    @Test
    fun pay_returnsErrorToIsFailed_throwsError() {

        // get error response from resources
        val responseBody = FileUtils.readTestResourceFile("PayResponseError.json")
        // get error string
        val gson = Gson()
        val responseBodyType = object : TypeToken<PayResponse>() {}.type
        val prepareAmountMock: PayResponse =
            gson.fromJson(responseBody, responseBodyType)
        val prepareDataObject: String = prepareAmountMock.status.errors[0].toString()
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage(prepareDataObject)

        setSettings()
        // simulate prepare amount method
        XpayUtils.PaymentOptionsTotalAmounts = PaymentOptionsTotalAmounts(52.0, 50.0, 52.85)
        XpayUtils.activePaymentMethods =
            mutableListOf(PaymentMethods.CARD, PaymentMethods.CASH, PaymentMethods.KIOSK)

        XpayUtils.payUsing = PaymentMethods.CASH
        XpayUtils.userInfo = User("Mahmoud Aziz", "mabdelaziz@xpay.app", "+201226476026")
        XpayUtils.ShippingInfo = ShippingInfo("egypt", "", "", "", "", "", "")
        XpayUtils.request = serviceRequest
        // Schedule some responses.

        // Schedule server error response
        val response = MockResponse()
            .setResponseCode(400)
            .setBody(responseBody)
        mockWebServer.enqueue(response)

        runBlocking {
            XpayUtils.pay()
        }
    }

    //transaction method tests

    // throws error when no settings are found
    @Test
    fun getTransaction_noApiKey_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("API key is not set")

        // set settings
        setSettings()
        XpayUtils.apiKey = null

        runBlocking {
            XpayUtils.getTransaction("")
        }
    }

    @Test
    fun getTransaction_noCommunityId_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("Community ID is not set")

        // set settings
        setSettings()
        XpayUtils.communityId = null

        // run method
        runBlocking {
            XpayUtils.getTransaction("")
        }
    }

    @Test
    fun getTransaction_noVariableAmountId_throwsError() {
        // set expected exception properties
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("API Payment ID is not set")

        // set settings
        setSettings()
        XpayUtils.variableAmountID = null

        // run method
        runBlocking {
            XpayUtils.getTransaction("")
        }
    }

    @Test
    fun getTransaction_allSettings_makeRequestSuccessfully() {
        // test settings
        setSettings()

        val getTransactionResponseBody =
            FileUtils.readTestResourceFile("getTransactionResponse.json")

        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(getTransactionResponseBody))

        // run
        runBlocking {
            XpayUtils.getTransaction("562c6565-1cc9-4773-87d8-f2b1f3af3246")
        }

        // assertion
        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals(
            "/v1/communities/${XpayUtils.communityId}/transactions/562c6565-1cc9-4773-87d8-f2b1f3af3246/",
            request.path
        )
        assertEquals(request.getHeader("x-api-key"), XpayUtils.apiKey)
    }

    @Test
    fun getTransaction_allSettings_returnsDataSuccessfully() {
        // test settings
        setSettings()

        val getTransactionResponseBody =
            FileUtils.readTestResourceFile("getTransactionResponse.json")
        val gson = Gson()
        val responseType = object : TypeToken<TransactionResponse>() {}.type
        val responseMock: TransactionResponse =
            gson.fromJson(getTransactionResponseBody, responseType)
        val prepareDataObject: TransactionData = responseMock.data
        var prepareData: TransactionData? = null

        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(getTransactionResponseBody))

        // run
        runBlocking {
            prepareData = XpayUtils.getTransaction("562c6565-1cc9-4773-87d8-f2b1f3af3246")
        }

        // assertion
        assertEquals(prepareData, prepareDataObject)
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