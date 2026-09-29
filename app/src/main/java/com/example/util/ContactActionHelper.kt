package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object ContactActionHelper {

    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "").removePrefix("+")
        try {
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val url = if (cleanNumber.isNotEmpty()) {
                "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMsg"
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed or number is invalid", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppWithFile(context: Context, phoneNumber: String, message: String, fileUri: Uri?, mimeType: String = "*/*") {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")

            // Copy message to Clipboard so user can easily paste if WhatsApp caption does not auto-fill
            if (message.isNotBlank()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                if (clipboard != null) {
                    val clip = android.content.ClipData.newPlainText("Invitation Message", message)
                    clipboard.setPrimaryClip(clip)
                }
            }

            if (fileUri != null) {
                // Resolve exact MIME type
                val resolvedMimeType = try {
                    context.contentResolver.getType(fileUri) ?: mimeType
                } catch (e: Exception) {
                    mimeType
                }
                val finalMime = if (resolvedMimeType.isBlank() || resolvedMimeType == "*/*") {
                    if (mimeType.contains("pdf")) "application/pdf" else "image/*"
                } else {
                    resolvedMimeType
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = finalMime
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    if (message.isNotBlank()) {
                        putExtra(Intent.EXTRA_TEXT, message)
                    }
                    if (cleanNumber.isNotEmpty()) {
                        putExtra("jid", "$cleanNumber@s.whatsapp.net")
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                var launched = false
                try {
                    val waIntent = Intent(intent).apply { setPackage("com.whatsapp") }
                    context.startActivity(waIntent)
                    launched = true
                    Toast.makeText(context, "Opening WhatsApp... (Caption copied to clipboard)", Toast.LENGTH_SHORT).show()
                } catch (e1: Exception) {
                    try {
                        val w4bIntent = Intent(intent).apply { setPackage("com.whatsapp.w4b") }
                        context.startActivity(w4bIntent)
                        launched = true
                        Toast.makeText(context, "Opening WhatsApp Business...", Toast.LENGTH_SHORT).show()
                    } catch (e2: Exception) {
                        launched = false
                    }
                }

                if (!launched) {
                    val chooserIntent = Intent.createChooser(intent, "Share Attachment & Message").apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooserIntent)
                    Toast.makeText(context, "Message copied! Select WhatsApp to send.", Toast.LENGTH_SHORT).show()
                }
            } else {
                openWhatsApp(context, phoneNumber, message)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to send via WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun makePhoneCall(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.trim()}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch phone dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, phoneNumber: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phoneNumber.trim()}")
                putExtra("sms_body", body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, email: String, subject: String, body: String) {
        if (email.isBlank()) {
            Toast.makeText(context, "No email address provided", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${email.trim()}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch email client", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMapsNavigation(context: Context, address: String, latitude: Double = 0.0, longitude: Double = 0.0) {
        try {
            val uri = if (latitude != 0.0 && longitude != 0.0) {
                Uri.parse("geo:$latitude,$longitude?q=${Uri.encode(address)}")
            } else {
                Uri.parse("geo:0,0?q=${Uri.encode(address)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }
}
