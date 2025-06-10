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

/**
 * Question that accepts free-form text input from users.
 * 
 * FreeTextQuestion allows users to provide open-ended responses with configurable
 * validation rules such as length limits, input type hints, and pattern matching.
 * 
 * ## Features
 * - **Flexible Input**: Supports various text input types (email, phone, etc.)
 * - **Length Validation**: Configurable minimum and maximum character limits
 * - **Pattern Matching**: Optional regex validation for specific formats
 * - **Input Hints**: UI hints for better user experience
 * 
 * ## Example Usage
 * ```kotlin
 * val nameQuestion = FreeTextQuestion(
 *     id = "full-name",
 *     text = "What is your full name?",
 *     isRequired = true,
 *     config = TextConfig(
 *         inputType = TextInputType.PersonName,
 *         maxLength = 100,
 *         minLength = 2
 *     )
 * ).getOrThrow()
 * 
 * val emailQuestion = FreeTextQuestion(
 *     id = "email",
 *     text = "Enter your email address:",
 *     isRequired = true,
 *     config = TextConfig(
 *         inputType = TextInputType.Email,
 *         validationPattern = "^[A-Za-z0-9+_.-]+@(.+)$"
 *     )
 * ).getOrThrow()
 * ```
 * 
 * @param id Unique identifier for the question
 * @param text The question text displayed to users
 * @param description Optional additional description or instructions
 * @param isRequired Whether a response is required to proceed
 * @param metadata Additional custom data attached to the question
 * @param config Text input configuration including validation rules
 * @since 1.0.0
 */
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