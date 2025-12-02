package com.example.moodfood.data.ai

import android.content.Context
import android.service.controls.ControlsProviderService.TAG
import com.example.moodfood.BuildConfig
import com.example.moodfood.data.db.AppDatabase
import com.example.moodfood.data.db.SuggestionEntity
import com.example.moodfood.data.models.NutritionSuggestion
import com.example.moodfood.data.models.Meal
import com.example.moodfood.data.models.Ingredient
import com.example.moodfood.data.models.Timing
import com.example.moodfood.data.models.Nutrition
import com.example.moodfood.data.nutrition.MoodNutritionService
import kotlinx.coroutines.flow.Flow
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import org.json.JSONException
import java.util.*

class SuggestionsRepository(private val service: OpenRouterService, private val context: Context) {
    
    private val moodNutritionService = MoodNutritionService(context)
    private val preferencesRepository = com.example.moodfood.data.preferences.FoodPreferencesRepository(context)
    
    suspend fun getNutritionSuggestion(mood: String, goal: String): Pair<NutritionSuggestion, Long> {
        val moodInfo = moodNutritionService.getMoodNutrition(mood)
            ?: throw Exception("No nutrition data found for mood: $mood")
        
        // Get user's food preferences
        val preferences = preferencesRepository.getUserPreferencesOnce()
        
        val prompt = if (preferences != null && preferences.isSetupComplete) {
            buildPromptWithPreferences(mood, goal, moodInfo.foods, moodInfo.nutrients, preferences)
        } else {
            buildPrompt(mood, goal, moodInfo.foods, moodInfo.nutrients)
        }
        val systemPrompt = buildSystemPrompt()
        
        val payload = """
                        {
                            "model": "google/gemini-2.5-flash-lite",
                            "messages": [
                                {"role": "system", "content": ${jsonEscape(systemPrompt)}},
                                {"role": "user", content: ${jsonEscape(prompt)}}
                            ],
                            "max_tokens": 900,
                            "temperature": 0.7
                        }
                """.trimIndent()
        
        Log.d("OpenRouter", "Sending request for mood: $mood, goal: $goal")
        val resp = service.chat(payload)
        
        if (!resp.isSuccessful) {
            val code = resp.code()
            val err = resp.errorBody()?.string()?.take(500)
            Log.e("OpenRouter", "HTTP $code error body: " + (err ?: "<none>"))
            val fallback = generateFallbackSuggestion(mood, goal, moodInfo.foods, moodInfo.nutrients)
            val id = AppDatabase.get(context).suggestionDao().insert(
                SuggestionEntity(
                    timestamp = System.currentTimeMillis(),
                    name = fallback.title,
                    mood = mood,
                    goal = goal,
                    symptomsCsv = "",
                    json = "{}"
                )
            )
            return Pair(fallback, id)
        }
        
        val raw = resp.body() ?: "{}"

        Log.i("Raw AI Response", raw)
        
        return try {
            val suggestion = parseNutritionSuggestion(raw)
            
            // Debug log the parsed suggestion
            //Log.d("OpenRouter", "Parsed suggestion: Title='${suggestion.title}', Meal='${suggestion.meal.name}', Ingredients count=${suggestion.ingredients.size}, Prep steps=${suggestion.preparation.size}, Tips=${suggestion.tips.size}")
            
            // Save to database
            val id = AppDatabase.get(context).suggestionDao().insert(
                SuggestionEntity(
                    timestamp = System.currentTimeMillis(),
                    name = suggestion.title,
                    mood = mood,
                    goal = goal,
                    symptomsCsv = "",
                    json = raw
                )
            )

            Pair(suggestion, id)
        } catch (e: Exception) {
            Log.e("OpenRouter", "Error parsing suggestion: ${e.message}")
            val fallback = generateFallbackSuggestion(mood, goal, moodInfo.foods, moodInfo.nutrients)
            val id = AppDatabase.get(context).suggestionDao().insert(
                SuggestionEntity(
                    timestamp = System.currentTimeMillis(),
                    name = fallback.title,
                    mood = mood,
                    goal = goal,
                    symptomsCsv = "",
                    json = "{}"
                )
            )
            Pair(fallback, id)
        }
    }
    
    private fun buildSystemPrompt(): String {
        return """You are a nutrition expert. CRITICAL: You must respond with ONLY valid JSON - no additional text, no markdown, no explanations.

Your response must be a single JSON object that starts with { and ends with }. Do not include any text before or after the JSON.

The JSON must follow this exact structure:
{
  "title": "string",
  "emoji": "single emoji",
  "meal": {
    "name": "string",
    "description": "string under 50 words",
    "prepTime": "string like '5 minutes'"
  },
  "ingredients": [
    {
      "name": "string",
      "benefit": "string under 30 words", 
      "emoji": "single emoji"
    }
  ],
  "timing": {
    "when": "string",
    "why": "string under 25 words"
  },
  "preparation": ["step1", "step2", "step3"],
  "tips": ["tip1 under 25 words", "tip2 under 25 words"],
  "nutrition": {
    "calories": "string like '300-400 calories'",
    "mainNutrients": ["nutrient1", "nutrient2", "nutrient3"]
  }
}
"""
    }
    
    private fun buildPrompt(mood: String, goal: String, foods: List<String>, nutrients: List<String>): String {
        return """I am feeling $mood and my goal is to $goal. I know that for my mood, I should eat foods like ${foods.joinToString(", ")} which are rich in ${nutrients.joinToString(", ")}. 

Please provide a nutrition suggestion in the JSON format specified in the system prompt. Keep it concise, practical, and engaging for someone feeling $mood who wants to $goal."""
    }
    
    private fun buildPromptWithPreferences(
        mood: String, 
        goal: String, 
        foods: List<String>, 
        nutrients: List<String>,
        preferences: com.example.moodfood.data.preferences.FoodPreferences
    ): String {
        val basePrompt = buildPrompt(mood, goal, foods, nutrients)
        
        val preferencesText = buildString {
            append("\n\nIMPORTANT - User's dietary preferences:")
            
            if (preferences.dietaryRestrictions.isNotBlank()) {
                val restrictions = preferences.dietaryRestrictions.split(",").map { it.trim() }
                append("\n- Dietary restrictions: ${restrictions.joinToString(", ")} (MUST strictly follow)")
            }
            
            if (preferences.allergies.isNotBlank()) {
                val allergies = preferences.allergies.split(",").map { it.trim() }
                append("\n- ALLERGIES: ${allergies.joinToString(", ")} (NEVER include these - CRITICAL)")
            }
            
            if (preferences.dislikedFoods.isNotBlank()) {
                val disliked = preferences.dislikedFoods.split(",").map { it.trim() }
                append("\n- Foods to avoid: ${disliked.joinToString(", ")}")
            }
            
            if (preferences.preferredCuisines.isNotBlank()) {
                val cuisines = preferences.preferredCuisines.split(",").map { it.trim() }
                append("\n- Preferred cuisines: ${cuisines.joinToString(", ")}")
            }
            
            append("\n- Spice level: ${preferences.spiceLevel}")
            append("\n- Meal complexity: ${preferences.mealComplexity}")
            append("\n- Cooking time: max ${preferences.cookingTime} minutes")
            append("\n- Budget: ${preferences.budget}")
            
            if (preferences.organicPreference) {
                append("\n- Prefers organic ingredients when possible")
            }
            
            if (preferences.localPreference) {
                append("\n- Prefers locally sourced ingredients when possible")
            }
            
            append("\n\nEnsure the suggestion strictly respects these preferences, especially allergies and dietary restrictions.")
        }
        
        return basePrompt + preferencesText
    }
    
    fun parseNutritionSuggestion(raw: String): NutritionSuggestion {
        // Extract content from OpenRouter response format
        val actualJson = if (raw.contains("\"choices\"")) {
            val responseObj = JSONObject(raw)
            val choices = responseObj.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.optJSONObject("message")
                message?.optString("content") ?: raw
            } else raw
        } else raw
        
        Log.d("OpenRouter", "Extracted JSON: $actualJson")
        
        // Clean the JSON response
        var cleanedText = actualJson.trim()
        
        // Remove markdown code blocks if present
        cleanedText = cleanedText.replace(Regex("```json\\s*"), "").replace(Regex("```\\s*$"), "")
        
        // Remove any potential explanatory text before/after JSON
        cleanedText = cleanedText.replace(Regex("^[^{]*"), "").replace(Regex("[^}]*$"), "")
        
        // Find JSON object boundaries
        val startIndex = cleanedText.indexOf('{')
        val lastIndex = cleanedText.lastIndexOf('}')
        
        if (startIndex != -1 && lastIndex != -1 && startIndex < lastIndex) {
            cleanedText = cleanedText.substring(startIndex, lastIndex + 1)
        }
        
        Log.d("OpenRouter", "Cleaned JSON: $cleanedText")
        
        val jsonObj = JSONObject(cleanedText)
        
        return NutritionSuggestion(
            title = jsonObj.optString("title", "Mood-Boosting Nutrition"),
            emoji = jsonObj.optString("emoji", "🌟"),
            meal = jsonObj.optJSONObject("meal")?.let { mealObj ->
                Meal(
                    name = mealObj.optString("name", "Healthy Meal"),
                    description = mealObj.optString("description", "A nutritious meal for your mood"),
                    prepTime = mealObj.optString("prepTime", "10 minutes")
                )
            } ?: Meal("Healthy Meal", "A nutritious meal for your mood", "10 minutes"),
            ingredients = jsonObj.optJSONArray("ingredients")?.let { ingredientsArray ->
                (0 until ingredientsArray.length()).map { i ->
                    val ingObj = ingredientsArray.getJSONObject(i)
                    Ingredient(
                        name = ingObj.optString("name", "Healthy ingredient"),
                        benefit = ingObj.optString("benefit", "Good for your health"),
                        emoji = ingObj.optString("emoji", "🥗")
                    )
                }
            } ?: emptyList(),
            timing = jsonObj.optJSONObject("timing")?.let { timingObj ->
                Timing(
                    `when` = timingObj.optString("when", "Anytime"),
                    why = timingObj.optString("why", "Good for your mood")
                )
            } ?: Timing("Anytime", "Good for your mood"),
            preparation = jsonObj.optJSONArray("preparation")?.let { prepArray ->
                (0 until prepArray.length()).map { prepArray.getString(it) }
            } ?: emptyList(),
            tips = jsonObj.optJSONArray("tips")?.let { tipsArray ->
                (0 until tipsArray.length()).map { tipsArray.getString(it) }
            } ?: emptyList(),
            nutrition = jsonObj.optJSONObject("nutrition")?.let { nutritionObj ->
                Nutrition(
                    calories = nutritionObj.optString("calories", "300-400 calories"),
                    mainNutrients = nutritionObj.optJSONArray("mainNutrients")?.let { nutrientsArray ->
                        (0 until nutrientsArray.length()).map { nutrientsArray.getString(it) }
                    } ?: emptyList()
                )
            } ?: Nutrition("300-400 calories", emptyList())
        )
    }
    
    private suspend fun generateFallbackSuggestion(mood: String, goal: String, foods: List<String>, nutrients: List<String>): NutritionSuggestion {
        val timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val mealType = when {
            timeOfDay < 10 -> "breakfast"
            timeOfDay < 14 -> "lunch"
            timeOfDay < 18 -> "afternoon snack"
            else -> "dinner"
        }

        val mainFood = foods.firstOrNull() ?: "healthy food"
        val secondFood = foods.getOrNull(1) ?: "nutritious ingredient"
        val nutrient1 = nutrients.firstOrNull() ?: "essential nutrients"
        val nutrient2 = nutrients.getOrNull(1) ?: "vitamins"
        
        Log.d("OpenRouter", "Generating fallback with foods: $foods, nutrients: $nutrients")

        return NutritionSuggestion(
            title = "Perfect ${mealType.replaceFirstChar { it.titlecase() }} for Your ${mood.replaceFirstChar { it.titlecase() }} Mood",
            emoji = "🌟",
            meal = Meal(
                name = "$mainFood Power Bowl",
                description = "A nourishing combination featuring ${mainFood.lowercase()} and ${secondFood.lowercase()} to help you ${goal.lowercase()}.",
                prepTime = "10 minutes"
            ),
            ingredients = listOf(
                Ingredient(
                    name = mainFood,
                    benefit = "Rich in $nutrient1, perfect for boosting your mood",
                    emoji = "🥗"
                ),
                Ingredient(
                    name = secondFood,
                    benefit = "Contains $nutrient2, supporting your goal to ${goal.lowercase()}",
                    emoji = "💪"
                ),
                Ingredient(
                    name = "Healthy fats",
                    benefit = "Nuts or seeds for sustained energy and brain health",
                    emoji = "🌰"
                )
            ),
            timing = Timing(
                `when` = "Best enjoyed ${if (timeOfDay < 12) "in the morning" else if (timeOfDay < 17) "in the afternoon" else "in the evening"}",
                why = "Optimal time to help you ${goal.lowercase()} effectively"
            ),
            preparation = listOf(
                "Combine ${mainFood.lowercase()} and ${secondFood.lowercase()} in a bowl",
                "Add a drizzle of healthy oil or your favorite dressing",
                "Top with nuts or seeds for extra nutrition",
                "Enjoy mindfully to enhance mood-boosting benefits"
            ),
            tips = listOf(
                "Stay hydrated with plenty of water throughout the day",
                "Eat slowly and mindfully to maximize nutritional absorption",
                "Pair with gentle exercise or a short walk for best results"
            ),
            nutrition = Nutrition(
                calories = "300-400 calories",
                mainNutrients = listOf(nutrient1, nutrient2, "healthy fats", "fiber")
            )
        )
    }

    fun recent(): Flow<List<SuggestionEntity>> = AppDatabase.get(context).suggestionDao().recent()

    private fun jsonEscape(text: String): String = "\"" + text
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n") + "\""
}
