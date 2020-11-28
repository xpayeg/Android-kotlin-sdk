package com.xpay.kotlinutils.models

data class BillingInfo(val name: String, val email: String, val phone: String) {
    init {
        val namePattern =
            Regex("^[a-zA-Z\\u0621-\\u064A-]{3,}(?:\\s[a-zA-Z\\u0621-\\u064A-]{3,})+\$")
        val nameMatchResult = namePattern.matches(name)
        if (!nameMatchResult)
            throw IllegalArgumentException("name value provided is in the wrong format")
        val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z.-]+[.][a-zA-Z]{2,4}\$")
        val emailMatchResult = emailPattern.matches(email)
        if (!emailMatchResult)
            throw IllegalArgumentException("email value provided is in the wrong format")
        val phonePattern = Regex("^\\+[0-9]{7,15}\$")
        val phoneMatchResult = phonePattern.matches(phone)
        if (!phoneMatchResult)
            throw IllegalArgumentException("phone value provided is in the wrong format")
    }
}