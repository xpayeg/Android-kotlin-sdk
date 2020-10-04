package com.xpay.kotlinutils.model

data class User(val name: String, val email: String, val phone: String) {
    init {
        val namePattern = Regex("^[a-zA-Z\\u0621-\\u064A-]{3,}(?:\\s[a-zA-Z\\u0621-\\u064A-]{3,})+\$")
        val nameMatchResult = namePattern.matches(name)
        if (!nameMatchResult)
            throw IllegalArgumentException("must provide a full name in the correct format")
        val emailPattern = Regex("^[a-z0-9._%+-]+@[a-z.-]+[.][a-z]{2,4}\$")
        val emailMatchResult = emailPattern.matches(email)
        if (!emailMatchResult)
            throw IllegalArgumentException("must provide an email in the correct format")
        val phonePattern = Regex("^\\+[0-9]{7,15}\$")
        val phoneMatchResult = phonePattern.matches(phone)
        if (!phoneMatchResult)
            throw IllegalArgumentException("must provide a phone number in the correct format")
    }
}