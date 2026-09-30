package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

import android.provider.OpenableColumns

data class SavedAttachment(
    val id: String,
    val eventId: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val uriString: String,
    val fileSizeFormatted: String = ""
)

object SavedAttachmentHelper {

    private const val PREF_NAME = "saved_event_attachments_pref"
    private const val KEY_ATTACHMENTS = "event_attachments_json"

    fun copyAndSaveAttachment(context: Context, eventId: String, sourceUri: Uri): SavedAttachment? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(sourceUri) ?: if (sourceUri.toString().contains("pdf")) "application/pdf" else "image/*"

            var realName: String? = null
            var fileSize = 0L
            try {
                contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            realName = cursor.getString(nameIndex)
                        }
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex >= 0) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (e: Exception) { }

            val ext = if (mimeType.contains("pdf")) "pdf" else "jpg"
            val uniqueSuffix = (100..999).random()
            val fileName = realName ?: "invite_${System.currentTimeMillis()}_$uniqueSuffix.$ext"

            val attachmentsDir = File(context.filesDir, "event_attachments").apply { mkdirs() }
            val targetFile = File(attachmentsDir, "${System.currentTimeMillis()}_$fileName")

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (fileSize == 0L) {
                fileSize = targetFile.length()
            }
            val sizeFormatted = if (fileSize > 1024 * 1024) {
                String.format(java.util.Locale.US, "%.1f MB", fileSize / (1024.0 * 1024.0))
            } else {
                "${(fileSize / 1024).coerceAtLeast(1)} KB"
            }

            val fileProviderUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )

            val attachment = SavedAttachment(
                id = "att_${System.currentTimeMillis()}_${(1000..9999).random()}",
                eventId = eventId,
                fileName = realName ?: if (mimeType.contains("pdf")) "Invitation Document.pdf" else "Invitation Card.jpg",
                filePath = targetFile.absolutePath,
                mimeType = mimeType,
                uriString = fileProviderUri.toString(),
                fileSizeFormatted = sizeFormatted
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
                        uriString = obj.optString("uriString"),
                        fileSizeFormatted = obj.optString("fileSizeFormatted")
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
                put("fileSizeFormatted", item.fileSizeFormatted)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_ATTACHMENTS, array.toString()).apply()
    }
}
