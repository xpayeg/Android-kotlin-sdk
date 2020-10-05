package com.xpay.kotlinutils.models.api.pay

import com.xpay.kotlinutils.models.CustomField
import com.xpay.kotlinutils.models.PaymentMethods

data class PayRequestBody(
    var variable_amount_id: Number? = null,
    var community_id: String? = null,
    var pay_using: PaymentMethods? = null,
    var amount: Number? = null,
    var currency: String? = null,
    var billing_data: HashMap<String, Any>? = null,
    var custom_fields: List<CustomField>? = null
)
