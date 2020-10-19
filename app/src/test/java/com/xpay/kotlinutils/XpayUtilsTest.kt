package com.xpay.kotlinutils

import com.xpay.kotlinutils.api.ServiceBuilder
import com.xpay.kotlinutils.api.Xpay
import com.xpay.kotlinutils.models.ServerSetting
import com.xpay.kotlinutils.models.api.prepare.PrepareAmountData
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

    @Before
    fun setUp() {
        mockWebServer.start(8080)
    }

    fun userSuccess(res: PrepareAmountData) {
    }

    fun userFailure(res: String) {
    }


    @Test(expected = IllegalArgumentException::class)
    fun prepareAmount_noSettings_throwserror() {
        XpayUtils.prepareAmount(50, ::userSuccess, ::userFailure)
    }

    @Test
    fun prepareAmount_allSettings_throwserror() {
        // test settings
        XpayUtils.apiKey = "3uBD5mrj.3HSCm46V7xJ5yfIkPb2gBOIUFH4Ks0Ss"
        XpayUtils.communityId = "zogDmQW"
        XpayUtils.variableAmountID = 18
        val serviceRequest = ServiceBuilder(ServerSetting.TEST, true).xpayService(Xpay::class.java)
        XpayUtils.request = serviceRequest

        // Schedule some responses.
        mockWebServer.enqueue(MockResponse().setBody(FileUtils.readTestResourceFile("PrepareAmountResponse.json")))

        // call our methods
        XpayUtils.prepareAmount(50, { x: PrepareAmountData -> println(x) }, ::userFailure)

        // assertion
        val request: RecordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/payments/prepare-amount/", request.path)
        assertNotNull(request.getHeader("x-api-key"))
        println(request.body)
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