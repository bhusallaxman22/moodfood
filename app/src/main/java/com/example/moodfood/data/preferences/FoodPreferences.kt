package com.example.moodfood.data.preferences

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.moodfood.data.auth.UserEntity

@Entity(
    tableName = "food_preferences",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class FoodPreferences(
    @PrimaryKey val userId: Long,
    val dietaryRestrictions: String = "", // Comma-separated: vegetarian, vegan, gluten-free, dairy-free, nut-free
    val allergies: String = "", // Comma-separated list of allergies
    val dislikedFoods: String = "", // Comma-separated list of foods user doesn't like
    val preferredCuisines: String = "", // Comma-separated: italian, asian, mexican, mediterranean, etc.
    val spiceLevel: String = "medium", // none, mild, medium, hot, very_hot
    val mealComplexity: String = "medium", // simple, medium, complex
    val cookingTime: String = "30", // Maximum cooking time in minutes
    val servings: Int = 1, // Typical number of servings needed
    val budget: String = "medium", // low, medium, high
    val organicPreference: Boolean = false,
    val localPreference: Boolean = false,
    val isSetupComplete: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class DietaryRestriction(val label: String) {
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    GLUTEN_FREE("Gluten-Free"),
    DAIRY_FREE("Dairy-Free"),
    NUT_FREE("Nut-Free"),
    PESCATARIAN("Pescatarian"),
    KETO("Keto"),
    PALEO("Paleo"),
    LOW_CARB("Low Carb"),
    HALAL("Halal"),
    KOSHER("Kosher")
}

enum class SpiceLevel(val label: String, val emoji: String) {
    NONE("No Spice", "😊"),
    MILD("Mild", "🌶️"),
    MEDIUM("Medium", "🌶️🌶️"),
    HOT("Hot", "🌶️🌶️🌶️"),
    VERY_HOT("Very Hot", "🌶️🌶️🌶️🌶️")
}

enum class MealComplexity(val label: String, val description: String) {
    SIMPLE("Simple", "Quick & easy meals"),
    MEDIUM("Medium", "Balanced preparation"),
    COMPLEX("Complex", "Gourmet & involved")
}

enum class Budget(val label: String, val emoji: String) {
    LOW("Budget-Friendly", "💵"),
    MEDIUM("Moderate", "💵💵"),
    HIGH("Premium", "💵💵💵")
}
