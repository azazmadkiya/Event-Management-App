package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class SavedAttachment(
    val id: String,
    val eventId: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val uriString: String
)

object SavedAttachmentHelper {

    private const val PREF_NAME = "saved_event_attachments_pref"
    private const val KEY_ATTACHMENTS = "event_attachments_json"

    fun copyAndSaveAttachment(context: Context, eventId: String, sourceUri: Uri): SavedAttachment? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(sourceUri) ?: if (sourceUri.toString().contains("pdf")) "application/pdf" else "image/*"

            val ext = if (mimeType.contains("pdf")) "pdf" else "jpg"
            val fileName = "invite_${System.currentTimeMillis()}_${(100..999).random()}.$ext"

            val attachmentsDir = File(context.filesDir, "event_attachments").apply { mkdirs() }
            val targetFile = File(attachmentsDir, fileName)

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            val fileProviderUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )

            val displayName = if (mimeType.contains("pdf")) "PDF Doc" else "Image File"

            val attachment = SavedAttachment(
                id = "att_${System.currentTimeMillis()}_${(1000..9999).random()}",
                eventId = eventId,
                fileName = "$displayName (${targetFile.name})",
                filePath = targetFile.absolutePath,
                mimeType = mimeType,
                uriString = fileProviderUri.toString()
            )

            val current = getAllSavedAttachments(context).toMutableList()
            current.add(attachment)
            saveListToPrefs(context, current)

            attachment
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getSavedAttachments(context: Context, eventId: String): List<SavedAttachment> {
        val all = getAllSavedAttachments(context)
        val result = mutableListOf<SavedAttachment>()
        for (item in all) {
            if (item.eventId == eventId || eventId.isBlank()) {
                val file = File(item.filePath)
                if (file.exists()) {
                    val providerUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    result.add(item.copy(uriString = providerUri.toString()))
                }
            }
        }
        return result
    }

    fun deleteSavedAttachment(context: Context, attachmentId: String) {
        val all = getAllSavedAttachments(context).toMutableList()
        val toRemove = all.find { it.id == attachmentId }
        if (toRemove != null) {
            try {
                File(toRemove.filePath).delete()
            } catch (e: Exception) { }
            all.remove(toRemove)
            saveListToPrefs(context, all)
        }
    }

    private fun getAllSavedAttachments(context: Context): List<SavedAttachment> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_ATTACHMENTS, "[]") ?: "[]"
        val list = mutableListOf<SavedAttachment>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SavedAttachment(
                        id = obj.optString("id"),
                        eventId = obj.optString("eventId"),
                        fileName = obj.optString("fileName"),
                        filePath = obj.optString("filePath"),
                        mimeType = obj.optString("mimeType"),
                        uriString = obj.optString("uriString")
                    )
                )
            }
        } catch (e: Exception) { }
        return list
    }

    private fun saveListToPrefs(context: Context, list: List<SavedAttachment>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("eventId", item.eventId)
                put("fileName", item.fileName)
                put("filePath", item.filePath)
                put("mimeType", item.mimeType)
                put("uriString", item.uriString)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_ATTACHMENTS, array.toString()).apply()
    }
}
