package com.example.data.model

enum class DietaryPreference(val label: String, val emoji: String) {
    NONE("No Restrictions", "🍽️"),
    VEGETARIAN("Vegetarian", "🥗"),
    VEGAN("Vegan", "🌱"),
    HALAL("Halal", "🌙"),
    KOSHER("Kosher", "✡️"),
    GLUTEN_FREE("Gluten-Free", "🌾"),
    NUT_ALLERGY("Nut Allergy", "🥜"),
    DAIRY_FREE("Dairy-Free", "🥛"),
    SEAFOOD_ALLERGY("Shellfish/Fish Allergy", "🦐"),
    CUSTOM("Custom / Other", "⚠️");

    companion object {
        fun fromString(value: String): DietaryPreference {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: NONE
        }
    }
}
