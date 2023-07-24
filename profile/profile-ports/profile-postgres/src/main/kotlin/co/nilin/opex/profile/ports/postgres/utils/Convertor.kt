package co.nilin.opex.profile.ports.postgres.utils

import com.google.gson.Gson


    fun <T> Any.convert(classOfT: Class<T>): T = Gson().fromJson(Gson().toJson(this), classOfT)
