package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.SeedDataProvider
import com.example.data.model.DietaryPreference
import com.example.data.model.RsvpStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EventVite", appName)
  }

  @Test
  fun `verify seed data provider and rsvp models`() {
    val events = SeedDataProvider.getInitialEvents()
    assertTrue(events.isNotEmpty())

    val galaGuests = SeedDataProvider.getInitialGuests(SeedDataProvider.EVENT_ID_1)
    assertTrue(galaGuests.isNotEmpty())

    val attendingCount = galaGuests.count { it.rsvpStatus == RsvpStatus.ATTENDING.name }
    assertTrue(attendingCount > 0)

    val veganGuests = galaGuests.filter { it.dietaryPreference == DietaryPreference.VEGAN.name }
    assertTrue(veganGuests.isNotEmpty())

    val itinerary = SeedDataProvider.getInitialItinerary(SeedDataProvider.EVENT_ID_1)
    assertTrue(itinerary.isNotEmpty())

    val points = SeedDataProvider.getVenuePoints()
    assertTrue(points.isNotEmpty())
  }
}
