package co.nilin.opex.profile.ports.postgres.utils

import co.nilin.opex.profile.core.data.profile.ProfileHistory
import kotlin.reflect.full.memberProperties

 fun Any.compare( s2: Any): List<String>? {
        val changedProperties: MutableList<String> = ArrayList()
        for (field in this::class.memberProperties) {
//            // You might want to set modifier to public first (if it is not public yet)
//            field.isAccessible = true
            val value1: Any? = field.getter.call(this)
            val value2: Any? = field.getter.call(s2)
            // if (value1 != null && value2 != null) {
            if (value1 != value2) {
                if (!field.name.lowercase().contains("date"))
                    changedProperties.add(field.name)
            }
            //   }
        }
        return changedProperties
    }
