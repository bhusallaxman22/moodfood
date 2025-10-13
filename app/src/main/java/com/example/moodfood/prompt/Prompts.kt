package com.example.moodfood.prompt

object Prompts {
    const val SYSTEM_PROMPT = """You are MoodFood AI. Analyze the user's mood, symptoms, and goal, then suggest exactly 3 foods from the provided list.

CRITICAL: Respond with ONLY a JSON object in this exact format:
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

    fun suggestionPrompt(mood: String, goalMood: String?, symptoms: List<String>, foods: List<Food>): String {
        val foodDb = foods.joinToString(separator = "\n") { f ->
            "- ${f.name}: moods=${f.moods.joinToString()}, nutrients=${f.nutrients.joinToString()}, compounds=${f.compounds.joinToString()}"
        }
        val sym = if (symptoms.isEmpty()) "none" else symptoms.joinToString()
        val goal = goalMood ?: "not specified"
        return """
            Context food-db:
            $foodDb

            User state:
            - mood: $mood
            - symptoms: $sym
            - goal mood: $goal

            Task:
            Suggest the top 3 foods from the food-db that best improve the user's state. Output JSON only using the schema from the system prompt.
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
