package dev.nathanmkaya.inquirio.core.error

import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.core.id.ResponseId
import dev.nathanmkaya.inquirio.core.id.SurveyId

/**
 * Root sealed interface for all survey-related errors
 * CUPID: Domain-based error modeling
 */
sealed interface SurveyError

/**
 * Survey data loading, persistence, and network errors
 */
sealed interface SurveyDataError : SurveyError {
    data class SurveyNotFound(val surveyId: SurveyId) : SurveyDataError
    data class ResponseNotFound(val responseId: ResponseId) : SurveyDataError
    data class InvalidSurveyData(val reason: String) : SurveyDataError
    data class PersistenceError(val cause: Throwable) : SurveyDataError
    data class NetworkError(val cause: Throwable) : SurveyDataError
    data class CompositeError(val primary: SurveyDataError, val all: List<SurveyDataError>) : SurveyDataError
}

/**
 * Navigation errors within a survey flow
 */
sealed interface NavigationError : SurveyError {
    data object NoNextQuestion : NavigationError
    data object NoPreviousQuestion : NavigationError
    data class NavigationBlocked(val reason: String) : NavigationError
}

/**
 * Validation errors for survey construction
 */
sealed interface SurveyValidationError : SurveyError {
    data object EmptySurveyTitle : SurveyValidationError
    data object EmptySurveyId : SurveyValidationError
    data object NoQuestions : SurveyValidationError
    data class DuplicateQuestionId(val questionId: QuestionId) : SurveyValidationError
}

/**
 * Question validation errors
 */
sealed interface QuestionValidationError : SurveyError {
    data class EmptyQuestionText(val questionId: QuestionId) : QuestionValidationError
    data class EmptyQuestionId(val questionIndex: Int) : QuestionValidationError
    data class InvalidQuestionConfig(val questionId: QuestionId, val reason: String) : QuestionValidationError
}

/**
 * Response validation errors
 */
sealed interface ResponseValidationError : SurveyError {
    data class RequiredFieldMissing(val questionId: QuestionId) : ResponseValidationError
    data class InvalidResponseType(val questionId: QuestionId, val expected: String, val actual: String) : ResponseValidationError
    data class ValueOutOfRange(val questionId: QuestionId, val value: Any, val range: String) : ResponseValidationError
    data class TextTooLong(val questionId: QuestionId, val length: Int, val maxLength: Int) : ResponseValidationError
    data class InvalidSelection(val questionId: QuestionId, val reason: String) : ResponseValidationError
    data class InvalidFileType(val questionId: QuestionId, val fileType: String, val allowed: Set<String>) : ResponseValidationError
}