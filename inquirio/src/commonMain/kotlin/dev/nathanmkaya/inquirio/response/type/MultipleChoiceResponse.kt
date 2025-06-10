package dev.nathanmkaya.inquirio.response.type

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError
import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.platform.getCurrentTimeMillis
import dev.nathanmkaya.inquirio.response.model.QuestionResponse

data class MultipleChoiceResponse private constructor(
    override val questionId: QuestionId,
    val selectedOptionIds: NonEmptyList<String>,
    override val timestamp: Long,
    override val metadata: Map<String, Any>
) : QuestionResponse {
    companion object {
        operator fun invoke(
            questionId: QuestionId,
            selectedOptionIds: List<String>,
            timestamp: Long = getCurrentTimeMillis(),
            metadata: Map<String, Any> = emptyMap()
        ): Either<NonEmptyList<ResponseValidationError>, MultipleChoiceResponse> = either {
            val selections = ensureNotNull(selectedOptionIds.toNonEmptyListOrNull()) {
                nonEmptyListOf(ResponseValidationError.InvalidSelection(questionId, "Must select at least one option"))
            }
            MultipleChoiceResponse(questionId, selections, timestamp, metadata)
        }
    }
}