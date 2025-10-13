package com.example.moodfood.data.models

data class NutritionSuggestion(
    val title: String,
    val emoji: String,
    val meal: Meal,
    val ingredients: List<Ingredient>,
    val timing: Timing,
    val preparation: List<String>,
    val tips: List<String>,
    val nutrition: Nutrition
)

data class Meal(
    val name: String,
    val description: String,
    val prepTime: String
)

data class Ingredient(
    val name: String,
    val benefit: String,
    val emoji: String
)

data class Timing(
    val `when`: String,
    val why: String
)

data class Nutrition(
    val calories: String,
    val mainNutrients: List<String>
)

data class MoodNutrition(
    val foods: List<String>,
    val nutrients: List<String>
)
