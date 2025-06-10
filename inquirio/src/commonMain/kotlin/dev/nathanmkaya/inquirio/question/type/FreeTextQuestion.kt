package dev.nathanmkaya.inquirio.question.type

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.QuestionValidationError
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError
import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.core.validation.ValidationResult
import dev.nathanmkaya.inquirio.question.config.TextConfig
import dev.nathanmkaya.inquirio.question.config.TextInputType
import dev.nathanmkaya.inquirio.question.model.Question
import dev.nathanmkaya.inquirio.response.model.QuestionResponse
import dev.nathanmkaya.inquirio.response.type.TextResponse

data class FreeTextQuestion private constructor(
    override val id: QuestionId,
    override val text: String,
    override val description: String?,
    override val isRequired: Boolean,
    override val metadata: Map<String, Any>,
    val config: TextConfig
) : Question {
    companion object {
        operator fun invoke(
            id: String,
            text: String,
            description: String? = null,
            isRequired: Boolean = false,
            metadata: Map<String, Any> = emptyMap(),
            config: TextConfig = TextConfig()
        ): Either<NonEmptyList<QuestionValidationError>, FreeTextQuestion> = either {
            val validId = QuestionId(id, 0).bind()
            ensure(text.isNotBlank()) { nonEmptyListOf(QuestionValidationError.EmptyQuestionText(validId)) }
            FreeTextQuestion(validId, text, description, isRequired, metadata, config)
        }
    }

    override fun validate(response: QuestionResponse?): ValidationResult<QuestionResponse> = either {
        when {
            response == null -> {
                ensure(!isRequired) { nonEmptyListOf(ResponseValidationError.RequiredFieldMissing(id)) }
                raise(nonEmptyListOf(ResponseValidationError.RequiredFieldMissing(id)))
            }
            response !is TextResponse -> raise(nonEmptyListOf(ResponseValidationError.InvalidResponseType(id, "Text", response::class.simpleName ?: "Unknown")))
            else -> {
                val errors = mutableListOf<ResponseValidationError>()
                config.maxLength?.let { maxLen ->
                    if (response.value.length > maxLen) {
                        errors.add(ResponseValidationError.TextTooLong(id, response.value.length, maxLen))
                    }
                }
                if (config.inputType == TextInputType.EMAIL && !isValidEmail(response.value)) {
                    errors.add(ResponseValidationError.InvalidResponseType(id, "Valid email", response.value))
                }
                ensure(errors.isEmpty()) { errors.toNonEmptyListOrNull()!! }
                response
            }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
}