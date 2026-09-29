package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.ItineraryItemEntity
import com.example.data.model.RsvpStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndSharePdfReport(
        context: Context,
        event: EventEntity,
        guests: List<GuestEntity>,
        itinerary: List<ItineraryItemEntity>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
            var pageNumber = 1
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.US)
            val eventDateStr = dateFormat.format(Date(event.startTimestamp))

            var y = 40f

            // Header Banner
            paint.color = Color.rgb(30, 27, 75) // Deep Indigo
            canvas.drawRect(30f, y, 565f, y + 60f, paint)

            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("EVENTVITE EVENT DOSSIER & CATERING REPORT", 45f, y + 26f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Generated on ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())}", 45f, y + 48f, paint)

            y += 80f

            // Event Details Box
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(30f, y, 565f, y + 65f, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(event.title, 42f, y + 20f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Date & Time: $eventDateStr", 42f, y + 36f, paint)
            canvas.drawText("Venue: ${event.venueName} — ${event.venueAddress}", 42f, y + 50f, paint)

            y += 80f

            // Metrics Summary Cards
            val totalInvited = guests.size
            val attending = guests.count { it.rsvpStatus == RsvpStatus.ATTENDING.name }
            val pending = guests.count { it.rsvpStatus == RsvpStatus.PENDING.name }
            val declined = guests.count { it.rsvpStatus == RsvpStatus.DECLINED.name }
            val checkedIn = guests.count { it.isCheckedIn }
            val totalPartySize = guests.filter { it.rsvpStatus == RsvpStatus.ATTENDING.name }.sumOf { it.totalPartySize }
            val acceptanceRate = if (totalInvited > 0) ((attending.toFloat() / totalInvited) * 100).toInt() else 0

            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRect(30f, y, 565f, y + 45f, paint)

            paint.color = Color.rgb(79, 70, 229)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val colW = 535f / 5f
            canvas.drawText("TOTAL GUESTS", 40f, y + 16f, paint)
            canvas.drawText("ATTENDING", 40f + colW, y + 16f, paint)
            canvas.drawText("PENDING", 40f + colW * 2, y + 16f, paint)
            canvas.drawText("DECLINED", 40f + colW * 3, y + 16f, paint)
            canvas.drawText("CHECKED IN", 40f + colW * 4, y + 16f, paint)

            paint.textSize = 13f
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText("$totalInvited", 40f, y + 34f, paint)
            canvas.drawText("$attending ($totalPartySize total)", 40f + colW, y + 34f, paint)
            canvas.drawText("$pending", 40f + colW * 2, y + 34f, paint)
            canvas.drawText("$declined", 40f + colW * 3, y + 34f, paint)
            canvas.drawText("$checkedIn ($acceptanceRate% OK)", 40f + colW * 4, y + 34f, paint)

            y += 60f

            // Dietary & Catering Breakdown
            paint.color = Color.rgb(30, 27, 75)
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DIETARY & CATERING ALLERGY SUMMARY", 30f, y, paint)
            y += 15f

            val dietaryMap = mutableMapOf<String, Int>()
            guests.filter { it.rsvpStatus == RsvpStatus.ATTENDING.name }.forEach { g ->
                val diet = if (g.dietaryPreference.isBlank()) "None" else g.dietaryPreference
                dietaryMap[diet] = (dietaryMap[diet] ?: 0) + g.totalPartySize
            }

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(51, 65, 85)

            var dietX = 30f
            dietaryMap.forEach { (diet, count) ->
                val badge = "$diet: $count meals"
                canvas.drawText(badge, dietX, y, paint)
                dietX += 130f
                if (dietX > 480f) {
                    dietX = 30f
                    y += 14f
                }
            }
            if (dietX != 30f) y += 18f

            y += 10f

            // Guest Roster Table Header
            paint.color = Color.rgb(79, 70, 229)
            canvas.drawRect(30f, y, 565f, y + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NAME", 35f, y + 14f, paint)
            canvas.drawText("TYPE", 160f, y + 14f, paint)
            canvas.drawText("RSVP", 230f, y + 14f, paint)
            canvas.drawText("DIETARY RESTRICTIONS", 295f, y + 14f, paint)
            canvas.drawText("TABLE", 450f, y + 14f, paint)
            canvas.drawText("ARRIVED", 505f, y + 14f, paint)

            y += 24f

            // Table Rows
            guests.forEachIndexed { index, guest ->
                if (y > 780f) {
                    // Finish current page and create new one
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    y = 40f

                    // Repeat Header on new page
                    paint.color = Color.rgb(79, 70, 229)
                    canvas.drawRect(30f, y, 565f, y + 20f, paint)
                    paint.color = Color.WHITE
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("NAME", 35f, y + 14f, paint)
                    canvas.drawText("TYPE", 160f, y + 14f, paint)
                    canvas.drawText("RSVP", 230f, y + 14f, paint)
                    canvas.drawText("DIETARY RESTRICTIONS", 295f, y + 14f, paint)
                    canvas.drawText("TABLE", 450f, y + 14f, paint)
                    canvas.drawText("ARRIVED", 505f, y + 14f, paint)
                    y += 24f
                }

                // Alternating row background
                if (index % 2 == 1) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(30f, y - 10f, 565f, y + 10f, paint)
                }

                paint.color = Color.rgb(15, 23, 42)
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                val nameLabel = if (guest.plusOnes > 0) "${guest.name} (+${guest.plusOnes})" else guest.name
                canvas.drawText(nameLabel.take(24), 35f, y, paint)
                canvas.drawText(guest.personType.take(12), 160f, y, paint)

                // Status with color hint
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = when (guest.rsvpStatus) {
                    RsvpStatus.ATTENDING.name -> Color.rgb(16, 185, 129)
                    RsvpStatus.PENDING.name -> Color.rgb(217, 119, 6)
                    RsvpStatus.DECLINED.name -> Color.rgb(239, 68, 68)
                    else -> Color.rgb(100, 116, 139)
                }
                canvas.drawText(guest.rsvpStatus, 230f, y, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(15, 23, 42)
                val dietNotes = if (guest.dietaryNotes.isNotBlank()) "${guest.dietaryPreference} (${guest.dietaryNotes})" else guest.dietaryPreference
                canvas.drawText(dietNotes.take(28), 295f, y, paint)
                canvas.drawText(if (guest.tableNumber.isNotBlank()) guest.tableNumber.take(10) else "-", 450f, y, paint)

                val checkStatus = if (guest.isCheckedIn) "YES" else "NO"
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (guest.isCheckedIn) Color.rgb(16, 185, 129) else Color.rgb(148, 163, 184)
                canvas.drawText(checkStatus, 515f, y, paint)

                y += 18f
            }

            pdfDocument.finishPage(page)

            val pdfFile = File(context.cacheDir, "EventVite_Report_${event.id}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Event Roster & Catering Report: ${event.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Event PDF Report"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error generating PDF report: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportGuestListCsv(context: Context, event: EventEntity, guests: List<GuestEntity>) {
        try {
            val sb = StringBuilder()
            sb.append("Name,Person Type,Phone,WhatsApp,Email,Address,RSVP Status,Dietary Preference,Dietary Notes,Plus Ones,Plus One Names,Table Number,Checked In,Notes\n")

            guests.forEach { g ->
                sb.append("\"${escapeCsv(g.name)}\",")
                sb.append("\"${escapeCsv(g.personType)}\",")
                sb.append("\"${escapeCsv(g.phoneNumber)}\",")
                sb.append("\"${escapeCsv(g.whatsAppNumber)}\",")
                sb.append("\"${escapeCsv(g.email)}\",")
                sb.append("\"${escapeCsv(g.address)}\",")
                sb.append("\"${escapeCsv(g.rsvpStatus)}\",")
                sb.append("\"${escapeCsv(g.dietaryPreference)}\",")
                sb.append("\"${escapeCsv(g.dietaryNotes)}\",")
                sb.append("${g.plusOnes},")
                sb.append("\"${escapeCsv(g.plusOneNames)}\",")
                sb.append("\"${escapeCsv(g.tableNumber)}\",")
                sb.append(if (g.isCheckedIn) "YES" else "NO")
                sb.append(",\"${escapeCsv(g.notes)}\"\n")
            }

            val csvFile = File(context.cacheDir, "Guests_${event.id}.csv")
            csvFile.writeText(sb.toString())

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Guest List CSV: ${event.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Guest CSV"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeCsv(str: String): String = str.replace("\"", "\"\"")
}
