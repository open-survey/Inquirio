package dev.nathanmkaya.inquirio.question.model

import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.core.validation.ValidationResult
import dev.nathanmkaya.inquirio.response.model.QuestionResponse

/**
 * Base interface for all question types
 */
interface Question {
    val id: QuestionId
    val text: String
    val description: String?
    val isRequired: Boolean
    val metadata: Map<String, Any>

    fun validate(response: QuestionResponse?): ValidationResult<QuestionResponse>
}

/**
 * Doubly-linked list node for O(1) forward/backward survey flow.
 */
data class QuestionNode(
    val question: Question,
    var nextResolver: (Map<QuestionId, QuestionResponse>) -> QuestionNode?,
    var previous: QuestionNode? = null // Enables O(1) backward navigation
) {
    companion object {
        /**
         * Creates a linear, doubly-linked chain of QuestionNodes from a list of Questions.
         */
        fun linearChain(questions: List<Question>): QuestionNode? {
            if (questions.isEmpty()) return null

            val nodes = questions.map { question ->
                QuestionNode(question, { null }) // Temp resolver
            }

            // Link nodes forwards and backwards
            for (i in 0 until nodes.size - 1) {
                val current = nodes[i]
                val next = nodes[i + 1]
                current.nextResolver = { _ -> next }
                next.previous = current
            }

            return nodes.firstOrNull()
        }
    }
}