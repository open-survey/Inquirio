package dev.nathanmkaya.inquirio.response.type

import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.platform.getCurrentTimeMillis
import dev.nathanmkaya.inquirio.response.model.QuestionResponse

/**
 * Text response
 */
data class TextResponse(
    override val questionId: QuestionId,
    val value: String,
    override val timestamp: Long = getCurrentTimeMillis(),
    override val metadata: Map<String, Any> = emptyMap()
) : QuestionResponse