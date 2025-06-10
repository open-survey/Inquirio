package dev.nathanmkaya.inquirio.core.id

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.QuestionValidationError
import dev.nathanmkaya.inquirio.core.error.SurveyValidationError

/**
 * Type-safe identifiers using data classes
 * CUPID: Domain-based, prevents ID confusion
 */

data class SurveyId private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String): Either<NonEmptyList<SurveyValidationError>, SurveyId> = either {
            ensure(value.isNotBlank()) { nonEmptyListOf(SurveyValidationError.EmptySurveyId) }
            SurveyId(value)
        }
        fun unsafe(value: String): SurveyId = SurveyId(value)
    }
}

data class QuestionId private constructor(val value: String) {
    companion object {
        operator fun invoke(value: String, index: Int): Either<NonEmptyList<QuestionValidationError>, QuestionId> = either {
            ensure(value.isNotBlank()) { nonEmptyListOf(QuestionValidationError.EmptyQuestionId(index)) }
            QuestionId(value)
        }
        fun unsafe(value: String): QuestionId = QuestionId(value)
    }
}

data class ResponseId private constructor(val value: String) {
    companion object {
        fun generate(): ResponseId = ResponseId(generateUUID())
        fun unsafe(value: String): ResponseId = ResponseId(value)
    }
}

// Simple UUID generator for KMP
private fun generateUUID(): String {
    val chars = "0123456789abcdef"
    val random = kotlin.random.Random.Default
    return buildString {
        repeat(8) { append(chars[random.nextInt(16)]) }
        append('-')
        repeat(4) { append(chars[random.nextInt(16)]) }
        append('-')
        append('4')
        repeat(3) { append(chars[random.nextInt(16)]) }
        append('-')
        append(chars[8 + random.nextInt(4)])
        repeat(3) { append(chars[random.nextInt(16)]) }
        append('-')
        repeat(12) { append(chars[random.nextInt(16)]) }
    }
}