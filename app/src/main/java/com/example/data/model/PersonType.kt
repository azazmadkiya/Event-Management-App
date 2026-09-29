package com.example.data.model

enum class PersonType(val label: String) {
    VIP("VIP Guest"),
    FAMILY("Family"),
    FRIEND("Friend"),
    COLLEAGUE("Colleague / Work"),
    SPEAKER("Speaker / Honoree"),
    VENDOR("Vendor / Staff"),
    OTHER("Guest");

    companion object {
        fun fromString(value: String): PersonType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
