package com.xpay.kotlinutils.model

class RequestBody(
    var variable_amount_id: Number,
    var community_id: String?=null,
    var pay_using: PaymentMethods?=null,
    var amount: Number?=null,
    var currency:String?=null,
    var billing_data: HashMap<String, Any>?=null,
    var custom_fields: List<CustomField>?=null
) {

}