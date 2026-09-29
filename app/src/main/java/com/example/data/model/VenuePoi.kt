package com.example.data.model

data class VenuePoi(
    val id: String,
    val name: String,
    val category: String, // "Entrance", "Ceremony", "Dining", "Parking", "Restroom", "Activity"
    val description: String,
    val walkingDirections: String,
    val xPercent: Float, // 0.0f to 1.0f on map canvas
    val yPercent: Float, // 0.0f to 1.0f on map canvas
    val iconName: String
)
