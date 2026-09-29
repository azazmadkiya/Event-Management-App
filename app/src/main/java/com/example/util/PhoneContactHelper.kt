package com.example.util

import android.content.Context
import android.provider.ContactsContract

data class PhoneContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String = ""
)

object PhoneContactHelper {
    fun fetchPhoneContacts(context: Context): List<PhoneContactItem> {
        val contacts = mutableListOf<PhoneContactItem>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val seenNumbers = mutableSetOf<String>()
                while (it.moveToNext()) {
                    val id = if (idIdx != -1) it.getString(idIdx) else ""
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val number = if (numIdx != -1) it.getString(numIdx) ?: "" else ""

                    val cleanNum = number.replace(Regex("[^0-9+]"), "")
                    if (cleanNum.isNotBlank() && seenNumbers.add(cleanNum)) {
                        contacts.add(PhoneContactItem(id = id, name = name, phoneNumber = number, email = ""))
                    }
                }
            }
        } catch (e: Exception) {
            // Ignored or logged
        }
        return contacts
    }
}
