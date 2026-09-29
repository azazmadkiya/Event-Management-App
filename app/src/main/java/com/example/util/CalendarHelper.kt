package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.EventEntity
import com.example.data.model.ItineraryItemEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CalendarHelper {

    fun addToSystemCalendar(context: Context, event: EventEntity, item: ItineraryItemEntity? = null) {
        try {
            val title = if (item != null) "${event.title}: ${item.title}" else event.title
            val location = if (item != null && item.location.isNotBlank()) "${item.location}, ${event.venueName}" else "${event.venueName}, ${event.venueAddress}"
            val description = if (item != null) item.description else event.description
            val startTime = item?.startEpochMillis ?: event.startTimestamp
            val endTime = item?.endEpochMillis ?: event.endTimestamp

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No calendar application found", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportIcsCalendar(context: Context, event: EventEntity, items: List<ItineraryItemEntity>) {
        try {
            val utcFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val sb = StringBuilder()
            sb.append("BEGIN:VCALENDAR\r\n")
            sb.append("VERSION:2.0\r\n")
            sb.append("PRODID:-//EventVite//Event Invitation & Schedule//EN\r\n")
            sb.append("CALSCALE:GREGORIAN\r\n")
            sb.append("METHOD:PUBLISH\r\n")

            // Main event
            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:event-${event.id}@eventvite.app\r\n")
            sb.append("DTSTAMP:${utcFormat.format(Date())}\r\n")
            sb.append("DTSTART:${utcFormat.format(Date(event.startTimestamp))}\r\n")
            sb.append("DTEND:${utcFormat.format(Date(event.endTimestamp))}\r\n")
            sb.append("SUMMARY:${escapeIcs(event.title)}\r\n")
            sb.append("DESCRIPTION:${escapeIcs(event.description)}\r\n")
            sb.append("LOCATION:${escapeIcs("${event.venueName}, ${event.venueAddress}")}\r\n")
            sb.append("STATUS:CONFIRMED\r\n")
            sb.append("END:VEVENT\r\n")

            // Sub-itinerary items
            items.forEachIndexed { index, item ->
                sb.append("BEGIN:VEVENT\r\n")
                sb.append("UID:itin-${item.id}-${index}@eventvite.app\r\n")
                sb.append("DTSTAMP:${utcFormat.format(Date())}\r\n")
                sb.append("DTSTART:${utcFormat.format(Date(item.startEpochMillis))}\r\n")
                sb.append("DTEND:${utcFormat.format(Date(item.endEpochMillis))}\r\n")
                sb.append("SUMMARY:${escapeIcs("${event.title}: ${item.title}")}\r\n")
                sb.append("DESCRIPTION:${escapeIcs(item.description)}\r\n")
                sb.append("LOCATION:${escapeIcs("${item.location}, ${event.venueName}")}\r\n")
                sb.append("STATUS:CONFIRMED\r\n")
                sb.append("END:VEVENT\r\n")
            }

            sb.append("END:VCALENDAR\r\n")

            val icsFile = File(context.cacheDir, "event_${event.id}_schedule.ics")
            icsFile.writeText(sb.toString())

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                icsFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Calendar Invite: ${event.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Share Calendar Schedule"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting calendar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeIcs(text: String): String {
        return text.replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
    }
}
