package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "guests")
data class GuestEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val name: String,
    val personType: String = PersonType.FRIEND.name,
    val phoneNumber: String = "",
    val whatsAppNumber: String = "",
    val email: String = "",
    val address: String = "",
    val rsvpStatus: String = RsvpStatus.PENDING.name,
    val dietaryPreference: String = DietaryPreference.NONE.name,
    val dietaryNotes: String = "",
    val plusOnes: Int = 0,
    val plusOneNames: String = "",
    val tableNumber: String = "",
    val isCheckedIn: Boolean = false,
    val checkInTimestamp: Long? = null,
    val notes: String = "",
    val inviteChannel: String = InviteChannel.STANDARD.name,
    val invitedTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
) {
    val totalPartySize: Int get() = 1 + plusOnes
}
