package co.nilin.opex.profile.core.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java,
                object : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
                    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

                    override fun serialize(
                            src: LocalDateTime?,
                            typeOfSrc: Type,
                            context: JsonSerializationContext
                    ): JsonElement {
                        return JsonPrimitive(src?.format(formatter))
                    }

                    override fun deserialize(
                            json: JsonElement?,
                            typeOfT: Type,
                            context: JsonDeserializationContext
                    ): LocalDateTime {
                        return LocalDateTime.parse(json!!.asString, formatter)
                    }
                })
        .create()
fun <T> Any.convert(classOfT: Class<T>): T = gson.fromJson(gson.toJson(this), classOfT)
