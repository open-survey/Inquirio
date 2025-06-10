/**
 * # Inquirio - Type-Safe Survey Library
 * 
 * A functional Kotlin Multiplatform survey library powered by Arrow-kt.
 * 
 * ## Key Features
 * - **Functional**: Built with Arrow-kt for explicit error handling using `Either` types
 * - **Type-Safe**: Smart constructors and domain identifiers prevent invalid states  
 * - **Multiplatform**: Share survey logic across Android, iOS, and more
 * - **Domain-Driven**: Clean architecture with proper separation of concerns
 * 
 * ## Basic Usage
 * 
 * ```kotlin
 * import dev.nathanmkaya.inquirio.*
 * import dev.nathanmkaya.inquirio.engine.surveySystem
 * 
 * // Create a survey
 * val survey = Survey(
 *     id = "feedback",
 *     title = "User Feedback",
 *     questions = listOf(
 *         FreeTextQuestion(id = "name", text = "Your name?", isRequired = true),
 *         BooleanQuestion(id = "satisfied", text = "Are you satisfied?")
 *     )
 * ).getOrElse { errors -> 
 *     // Handle validation errors
 *     return
 * }
 * 
 * // Configure system
 * val engine = SurveyEngine(surveySystem {
 *     source(InMemorySurveySource())
 *     sink(InMemorySurveySink()) 
 *     remote(ConsoleDebugRemote())
 * }.getOrThrow())
 * 
 * // Run survey flow
 * val flow = engine.startSurvey(survey)
 * ```
 * 
 * @since 1.0.0
 */
package dev.nathanmkaya.inquirio

// Re-export main API classes for easy access

/**
 * Core domain identifiers for type safety.
 * These prevent mixing different types of IDs accidentally.
 */

/** Type-safe identifier for surveys */
typealias SurveyId = dev.nathanmkaya.inquirio.core.id.SurveyId

/** Type-safe identifier for questions */
typealias QuestionId = dev.nathanmkaya.inquirio.core.id.QuestionId

/** Type-safe identifier for responses */
typealias ResponseId = dev.nathanmkaya.inquirio.core.id.ResponseId

/**
 * Error types for comprehensive error handling.
 * All operations return `Either<Error, Success>` for explicit error handling.
 */

/** Root error type for all survey-related errors */
typealias SurveyError = dev.nathanmkaya.inquirio.core.error.SurveyError

/** Errors related to data loading, persistence, and network operations */
typealias SurveyDataError = dev.nathanmkaya.inquirio.core.error.SurveyDataError

/** Errors that occur during survey navigation */
typealias NavigationError = dev.nathanmkaya.inquirio.core.error.NavigationError

/** Errors that occur during survey construction and validation */
typealias SurveyValidationError = dev.nathanmkaya.inquirio.core.error.SurveyValidationError

/** Errors that occur during question validation */
typealias QuestionValidationError = dev.nathanmkaya.inquirio.core.error.QuestionValidationError

/** Errors that occur during response validation */
typealias ResponseValidationError = dev.nathanmkaya.inquirio.core.error.ResponseValidationError

/**
 * Question types and models.
 * Questions are the building blocks of surveys.
 */

/** Base interface for all question types */
typealias Question = dev.nathanmkaya.inquirio.question.model.Question

/** Navigation node that connects questions in a flow */
typealias QuestionNode = dev.nathanmkaya.inquirio.question.model.QuestionNode

/** Question that accepts true/false responses */
typealias BooleanQuestion = dev.nathanmkaya.inquirio.question.type.BooleanQuestion

/** Question that offers multiple predefined choices */
typealias MultipleChoiceQuestion = dev.nathanmkaya.inquirio.question.type.MultipleChoiceQuestion

/** Question that accepts free-form text input */
typealias FreeTextQuestion = dev.nathanmkaya.inquirio.question.type.FreeTextQuestion

/**
 * Response types for capturing user input.
 * Each question type has a corresponding response type.
 */

/** Base interface for all response types */
typealias QuestionResponse = dev.nathanmkaya.inquirio.response.model.QuestionResponse

/** Response containing a boolean value */
typealias BooleanResponse = dev.nathanmkaya.inquirio.response.type.BooleanResponse

/** Response containing text input */
typealias TextResponse = dev.nathanmkaya.inquirio.response.type.TextResponse

/** Response containing selected choices */
typealias MultipleChoiceResponse = dev.nathanmkaya.inquirio.response.type.MultipleChoiceResponse

/**
 * Survey aggregates and configuration.
 * Surveys contain questions and manage their flow.
 */

/** A complete survey with questions and metadata */
typealias Survey = dev.nathanmkaya.inquirio.survey.model.Survey

/** A user's responses to a survey */
typealias SurveyResponse = dev.nathanmkaya.inquirio.survey.model.SurveyResponse

/** Configuration options for survey behavior */
typealias SurveySettings = dev.nathanmkaya.inquirio.survey.settings.SurveySettings

/** Strategy for when to validate responses */
typealias ValidationStrategy = dev.nathanmkaya.inquirio.survey.settings.ValidationStrategy

/**
 * Survey flow and navigation.
 * Handles the user journey through questions.
 */

/** Manages the flow of questions and responses */
typealias SurveyFlow = dev.nathanmkaya.inquirio.survey.flow.SurveyFlow

/** Interface for custom navigation logic */
typealias SurveyNavigator = dev.nathanmkaya.inquirio.survey.flow.SurveyNavigator

/** Default implementation of survey navigation */
typealias DefaultSurveyNavigator = dev.nathanmkaya.inquirio.survey.flow.DefaultSurveyNavigator

/**
 * Data access layer.
 * Pluggable interfaces for storage and remote submission.
 */

/** Interface for loading surveys from storage */
typealias SurveySource = dev.nathanmkaya.inquirio.data.source.SurveySource

/** Interface for saving survey data locally */
typealias SurveySink = dev.nathanmkaya.inquirio.data.sink.SurveySink

/** Interface for submitting surveys to remote endpoints */
typealias SurveyRemote = dev.nathanmkaya.inquirio.data.remote.SurveyRemote

/**
 * System orchestration and configuration.
 * Main entry points for using the library.
 */

/** Main orchestrator for survey operations */
typealias SurveyEngine = dev.nathanmkaya.inquirio.engine.SurveyEngine

/** Configuration for the survey system */
typealias SurveySystemConfig = dev.nathanmkaya.inquirio.engine.SurveySystemConfig

/** System-level settings and preferences */
typealias SurveySystemSettings = dev.nathanmkaya.inquirio.engine.SurveySystemSettings

// DSL Functions
// Note: Import the function directly: import dev.nathanmkaya.inquirio.engine.surveySystem

/**
 * Configuration types for questions.
 */

/** Option for multiple choice questions */
typealias QuestionOption = dev.nathanmkaya.inquirio.question.config.QuestionOption

/** Configuration for text input questions */
typealias TextConfig = dev.nathanmkaya.inquirio.question.config.TextConfig

/** Type of text input (email, phone, etc.) */
typealias TextInputType = dev.nathanmkaya.inquirio.question.config.TextInputType

/**
 * Default implementations provided out-of-the-box.
 * These can be used for testing or simple use cases.
 */

/** In-memory implementation of SurveySource */
typealias InMemorySurveySource = dev.nathanmkaya.inquirio.data.source.InMemorySurveySource

/** In-memory implementation of SurveySink */
typealias InMemorySurveySink = dev.nathanmkaya.inquirio.data.sink.InMemorySurveySink

/** Console logging implementation of SurveyRemote for debugging */
typealias ConsoleDebugRemote = dev.nathanmkaya.inquirio.data.remote.ConsoleDebugRemote

// Samples
// Note: Import the function directly: import dev.nathanmkaya.inquirio.sample.createSimpleExampleSurvey