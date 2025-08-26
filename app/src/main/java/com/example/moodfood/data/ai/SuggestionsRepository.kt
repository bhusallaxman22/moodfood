package com.example.moodfood.data.ai

import com.example.moodfood.prompt.Food
import com.example.moodfood.prompt.Prompts

class SuggestionsRepository(private val service: OpenRouterService) {
    suspend fun getFoodSuggestions(mood: String, goal: String?, symptoms: List<String>, foods: List<Food>): String {
        val userPrompt = Prompts.suggestionPrompt(mood, goal, symptoms, foods)
        val payload = """
            {
              "model": "openrouter/auto",
              "messages": [
                {"role": "system", "content": "${'$'}{Prompts.SYSTEM_PROMPT}"},
                {"role": "user", "content": ${'$'}{jsonEscape(userPrompt)}}
              ]
            }
        """.trimIndent()
        return service.chat(payload)
    }

    private fun jsonEscape(text: String): String {
        val escaped = text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
        return "\"${'$'}escaped\""
    }
}
