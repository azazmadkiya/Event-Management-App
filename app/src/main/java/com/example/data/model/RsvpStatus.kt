package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusAttending
import com.example.ui.theme.StatusAttendingContainer
import com.example.ui.theme.StatusDeclined
import com.example.ui.theme.StatusDeclinedContainer
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusDraftContainer
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingContainer
import com.example.ui.theme.StatusWaitlist
import com.example.ui.theme.StatusWaitlistContainer

enum class RsvpStatus(
    val displayName: String,
    val shortLabel: String,
    val color: Color,
    val containerColor: Color
) {
    ATTENDING("Attending (OK)", "OK", StatusAttending, StatusAttendingContainer),
    PENDING("Pending", "Pending", StatusPending, StatusPendingContainer),
    DECLINED("Declined", "Declined", StatusDeclined, StatusDeclinedContainer),
    NOT_INVITED("Not Invited (Draft)", "Draft", StatusDraft, StatusDraftContainer),
    WAITLIST("Waitlist", "Waitlist", StatusWaitlist, StatusWaitlistContainer);

    companion object {
        fun fromString(value: String): RsvpStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}
