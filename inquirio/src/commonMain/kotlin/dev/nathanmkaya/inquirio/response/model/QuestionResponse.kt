package dev.nathanmkaya.inquirio.response.model

import dev.nathanmkaya.inquirio.core.id.QuestionId

/**
 * Base interface for all question responses
 */
interface QuestionResponse {
    val questionId: QuestionId
    val timestamp: Long
    val metadata: Map<String, Any>
}