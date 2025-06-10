package dev.nathanmkaya.inquirio.question.type

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.QuestionValidationError
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError
import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.core.validation.ValidationResult
import dev.nathanmkaya.inquirio.question.model.Question
import dev.nathanmkaya.inquirio.response.model.QuestionResponse
import dev.nathanmkaya.inquirio.response.type.BooleanResponse

data class BooleanQuestion private constructor(
    override val id: QuestionId,
    override val text: String,
    override val description: String?,
    override val isRequired: Boolean,
    override val metadata: Map<String, Any>,
    val trueLabel: String,
    val falseLabel: String
) : Question {
    companion object {
        operator fun invoke(
            id: String,
            text: String,
            description: String? = null,
            isRequired: Boolean = false,
            metadata: Map<String, Any> = emptyMap(),
            trueLabel: String = "Yes",
            falseLabel: String = "No"
        ): Either<NonEmptyList<QuestionValidationError>, BooleanQuestion> = either {
            val validId = QuestionId(id, 0).bind()
            ensure(text.isNotBlank()) { nonEmptyListOf(QuestionValidationError.EmptyQuestionText(validId)) }
            BooleanQuestion(validId, text, description, isRequired, metadata, trueLabel, falseLabel)
        }
    }

    override fun validate(response: QuestionResponse?): ValidationResult<QuestionResponse> = either {
        when {
            response == null -> {
                ensure(!isRequired) { nonEmptyListOf(ResponseValidationError.RequiredFieldMissing(id)) }
                raise(nonEmptyListOf(ResponseValidationError.RequiredFieldMissing(id)))
            }
            response !is BooleanResponse -> raise(nonEmptyListOf(ResponseValidationError.InvalidResponseType(id, "Boolean", response::class.simpleName ?: "Unknown")))
            else -> response
        }
    }
}