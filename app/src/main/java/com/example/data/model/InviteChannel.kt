package com.example.data.model

enum class InviteChannel(val displayName: String, val shortLabel: String) {
    STANDARD("Standard Invite", "Invite"),
    WHATSAPP_ONLY("Invite Only WhatsApp", "WhatsApp Only");

    companion object {
        fun fromString(value: String?): InviteChannel {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: STANDARD
        }
    }
}
