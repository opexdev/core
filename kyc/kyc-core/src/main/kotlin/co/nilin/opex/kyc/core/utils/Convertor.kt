package co.nilin.opex.kyc.core.utils

import com.google.gson.Gson
    fun <T> Any.convert(classOfT: Class<T>): T = Gson().fromJson(Gson().toJson(this), classOfT)
