package dev.nathanmkaya.inquirio.response.type

import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.platform.getCurrentTimeMillis
import dev.nathanmkaya.inquirio.response.model.QuestionResponse

/**
 * Boolean response (Yes/No, True/False)
 */
data class BooleanResponse(
    override val questionId: QuestionId,
    val value: Boolean,
    override val timestamp: Long = getCurrentTimeMillis(),
    override val metadata: Map<String, Any> = emptyMap()
) : QuestionResponse