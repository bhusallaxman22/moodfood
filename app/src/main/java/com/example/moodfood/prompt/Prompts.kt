package com.example.moodfood.prompt

object Prompts {
    const val SYSTEM_PROMPT = """You are MoodFood AI. Analyze the user's mood, symptoms, goal, and dietary preferences, then suggest exactly 3 foods from the provided list.

CRITICAL RULES:
1. NEVER suggest foods the user is allergic to or has dietary restrictions against
2. Respect the user's food preferences (spice level, complexity, cuisine, budget)
3. Avoid foods the user dislikes
4. Respond with ONLY a JSON object in this exact format:

{
  "items": [
    {
      "name": "Food Name",
      "ingredients": ["ingredient1", "ingredient2"],
      "prep_method": "How to prepare",
      "best_time": "When to eat",
      "prep_tips": "Preparation tips",
      "why": "Why this helps the mood"
    }
  ]
}

No explanation, no markdown, no extra text. Just pure JSON."""

    fun suggestionPrompt(
        mood: String, 
        goalMood: String?, 
        symptoms: List<String>, 
        foods: List<Food>,
        dietaryRestrictions: List<String> = emptyList(),
        allergies: List<String> = emptyList(),
        dislikedFoods: List<String> = emptyList(),
        preferredCuisines: List<String> = emptyList(),
        spiceLevel: String = "medium",
        mealComplexity: String = "medium",
        budget: String = "medium"
    ): String {
        val foodDb = foods.joinToString(separator = "\n") { f ->
            "- ${f.name}: moods=${f.moods.joinToString()}, nutrients=${f.nutrients.joinToString()}, compounds=${f.compounds.joinToString()}"
        }
        val sym = if (symptoms.isEmpty()) "none" else symptoms.joinToString()
        val goal = goalMood ?: "not specified"
        
        val preferencesSection = buildString {
            if (dietaryRestrictions.isNotEmpty()) {
                append("\n- dietary restrictions: ${dietaryRestrictions.joinToString()}")
                append(" (MUST AVOID these)")
            }
            if (allergies.isNotEmpty()) {
                append("\n- allergies: ${allergies.joinToString()}")
                append(" (NEVER suggest these - CRITICAL)")
            }
            if (dislikedFoods.isNotEmpty()) {
                append("\n- dislikes: ${dislikedFoods.joinToString()}")
            }
            if (preferredCuisines.isNotEmpty()) {
                append("\n- preferred cuisines: ${preferredCuisines.joinToString()}")
            }
            append("\n- spice level: $spiceLevel")
            append("\n- meal complexity: $mealComplexity")
            append("\n- budget: $budget")
        }
        
        return """
            Context food-db:
            $foodDb

            User state:
            - mood: $mood
            - symptoms: $sym
            - goal mood: $goal

            User preferences:$preferencesSection

            Task:
            Suggest the top 3 foods from the food-db that:
            1. Best improve the user's mood state
            2. Strictly respect dietary restrictions and allergies
            3. Match the user's preferences (spice, complexity, cuisine)
            4. Avoid disliked foods when possible
            
            Output JSON only using the schema from the system prompt.
        """.trimIndent()
    }
}

data class Food(
    val name: String,
    val nutrients: List<String>,
    val compounds: List<String>,
    val moods: List<String>
)

object FoodDB {
    val foods = listOf(
        Food("Salmon", nutrients = listOf("Omega-3", "Protein", "B12"), compounds = listOf("EPA", "DHA"), moods = listOf("anxious", "low", "stressed")),
        Food("Dark Chocolate (70%)", nutrients = listOf("Magnesium", "Flavonoids"), compounds = listOf("Theobromine"), moods = listOf("low", "fatigued")),
        Food("Greek Yogurt", nutrients = listOf("Probiotics", "Protein", "Calcium"), compounds = listOf("Lactobacillus"), moods = listOf("stressed", "anxious")),
        Food("Banana", nutrients = listOf("B6", "Potassium", "Carbs"), compounds = listOf("Tryptophan"), moods = listOf("irritable", "fatigued")),
        Food("Oatmeal", nutrients = listOf("Fiber", "Complex carbs"), compounds = listOf(), moods = listOf("anxious", "stressed")),
        Food("Spinach", nutrients = listOf("Folate", "Magnesium"), compounds = listOf(), moods = listOf("low", "fatigued")),
        Food("Blueberries", nutrients = listOf("Antioxidants", "Fiber"), compounds = listOf("Anthocyanins"), moods = listOf("stressed")),
        Food("Green Tea", nutrients = listOf("Catechins"), compounds = listOf("L-theanine"), moods = listOf("anxious", "stressed")),
    )
}
