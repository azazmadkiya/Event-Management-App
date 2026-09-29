package com.example.util

import com.example.data.model.GuestEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object GuestBackupHelper {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val type = Types.newParameterizedType(List::class.java, GuestEntity::class.java)
    private val adapter = moshi.adapter<List<GuestEntity>>(type)

    fun exportToJson(guests: List<GuestEntity>): String {
        return adapter.toJson(guests)
    }

    fun importFromJson(jsonString: String): List<GuestEntity>? {
        return try {
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
