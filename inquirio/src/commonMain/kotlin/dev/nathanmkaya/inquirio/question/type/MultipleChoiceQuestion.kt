package dev.nathanmkaya.inquirio.question.type

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.QuestionValidationError
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError
import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.core.validation.ValidationResult
import dev.nathanmkaya.inquirio.question.config.QuestionOption
import dev.nathanmkaya.inquirio.question.model.Question
import dev.nathanmkaya.inquirio.response.model.QuestionResponse
import dev.nathanmkaya.inquirio.response.type.MultipleChoiceResponse

data class MultipleChoiceQuestion private constructor(
    override val id: QuestionId,
    override val text: String,
    override val description: String?,
    override val isRequired: Boolean,
    override val metadata: Map<String, Any>,
    val options: NonEmptyList<QuestionOption>,
    val selectionRange: IntRange
) : Question {
    companion object {
        operator fun invoke(
            id: String,
            text: String,
            description: String? = null,
            isRequired: Boolean = false,
            metadata: Map<String, Any> = emptyMap(),
            options: List<QuestionOption>,
            minSelections: Int = 1,
            maxSelections: Int? = null
        ): Either<NonEmptyList<QuestionValidationError>, MultipleChoiceQuestion> = either {
            val validId = QuestionId(id, 0).bind()
            ensure(text.isNotBlank()) { nonEmptyListOf(QuestionValidationError.EmptyQuestionText(validId)) }
            val optionsNel = ensureNotNull(options.toNonEmptyListOrNull()) {
                nonEmptyListOf(QuestionValidationError.InvalidQuestionConfig(validId, "Must have at least one option"))
            }
            val maxSel = maxSelections ?: optionsNel.size
            ensure(minSelections <= maxSel) {
                nonEmptyListOf(QuestionValidationError.InvalidQuestionConfig(validId, "minSelections cannot be greater than maxSelections"))
            }
            MultipleChoiceQuestion(validId, text, description, isRequired, metadata, optionsNel, minSelections..maxSel)
        }
    }

    override fun validate(response: QuestionResponse?): ValidationResult<QuestionResponse> = either {
        when {
            response == null -> {
                ensure(!isRequired) { nonEmptyListOf(ResponseValidationError.RequiredFieldMissing(id)) }
                raise(nonEmptyListOf(ResponseValidationError.RequiredFieldMissing(id)))
            }
            response !is MultipleChoiceResponse -> raise(nonEmptyListOf(ResponseValidationError.InvalidResponseType(id, "MultipleChoice", response::class.simpleName ?: "Unknown")))
            response.selectedOptionIds.size !in selectionRange -> raise(nonEmptyListOf(ResponseValidationError.InvalidSelection(id, "Must select between ${selectionRange.first} and ${selectionRange.last} options")))
            else -> response
        }
    }
}