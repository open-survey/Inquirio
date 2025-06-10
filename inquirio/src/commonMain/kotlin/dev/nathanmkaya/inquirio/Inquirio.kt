package dev.nathanmkaya.inquirio

// Re-export main API classes for easy access

// Core
typealias SurveyId = dev.nathanmkaya.inquirio.core.id.SurveyId
typealias QuestionId = dev.nathanmkaya.inquirio.core.id.QuestionId
typealias ResponseId = dev.nathanmkaya.inquirio.core.id.ResponseId

// Errors
typealias SurveyError = dev.nathanmkaya.inquirio.core.error.SurveyError
typealias SurveyDataError = dev.nathanmkaya.inquirio.core.error.SurveyDataError
typealias NavigationError = dev.nathanmkaya.inquirio.core.error.NavigationError
typealias SurveyValidationError = dev.nathanmkaya.inquirio.core.error.SurveyValidationError
typealias QuestionValidationError = dev.nathanmkaya.inquirio.core.error.QuestionValidationError
typealias ResponseValidationError = dev.nathanmkaya.inquirio.core.error.ResponseValidationError

// Questions
typealias Question = dev.nathanmkaya.inquirio.question.model.Question
typealias QuestionNode = dev.nathanmkaya.inquirio.question.model.QuestionNode
typealias BooleanQuestion = dev.nathanmkaya.inquirio.question.type.BooleanQuestion
typealias MultipleChoiceQuestion = dev.nathanmkaya.inquirio.question.type.MultipleChoiceQuestion
typealias FreeTextQuestion = dev.nathanmkaya.inquirio.question.type.FreeTextQuestion

// Responses
typealias QuestionResponse = dev.nathanmkaya.inquirio.response.model.QuestionResponse
typealias BooleanResponse = dev.nathanmkaya.inquirio.response.type.BooleanResponse
typealias TextResponse = dev.nathanmkaya.inquirio.response.type.TextResponse
typealias MultipleChoiceResponse = dev.nathanmkaya.inquirio.response.type.MultipleChoiceResponse

// Survey
typealias Survey = dev.nathanmkaya.inquirio.survey.model.Survey
typealias SurveyResponse = dev.nathanmkaya.inquirio.survey.model.SurveyResponse
typealias SurveySettings = dev.nathanmkaya.inquirio.survey.settings.SurveySettings
typealias ValidationStrategy = dev.nathanmkaya.inquirio.survey.settings.ValidationStrategy

// Flow
typealias SurveyFlow = dev.nathanmkaya.inquirio.survey.flow.SurveyFlow
typealias SurveyNavigator = dev.nathanmkaya.inquirio.survey.flow.SurveyNavigator
typealias DefaultSurveyNavigator = dev.nathanmkaya.inquirio.survey.flow.DefaultSurveyNavigator

// Data Layer
typealias SurveySource = dev.nathanmkaya.inquirio.data.source.SurveySource
typealias SurveySink = dev.nathanmkaya.inquirio.data.sink.SurveySink
typealias SurveyRemote = dev.nathanmkaya.inquirio.data.remote.SurveyRemote

// Engine
typealias SurveyEngine = dev.nathanmkaya.inquirio.engine.SurveyEngine
typealias SurveySystemConfig = dev.nathanmkaya.inquirio.engine.SurveySystemConfig
typealias SurveySystemSettings = dev.nathanmkaya.inquirio.engine.SurveySystemSettings

// DSL Functions
// Note: Import the function directly: import dev.nathanmkaya.inquirio.engine.surveySystem

// Config Types
typealias QuestionOption = dev.nathanmkaya.inquirio.question.config.QuestionOption
typealias TextConfig = dev.nathanmkaya.inquirio.question.config.TextConfig
typealias TextInputType = dev.nathanmkaya.inquirio.question.config.TextInputType

// Default Implementations
typealias InMemorySurveySource = dev.nathanmkaya.inquirio.data.source.InMemorySurveySource
typealias InMemorySurveySink = dev.nathanmkaya.inquirio.data.sink.InMemorySurveySink
typealias ConsoleDebugRemote = dev.nathanmkaya.inquirio.data.remote.ConsoleDebugRemote

// Samples
// Note: Import the function directly: import dev.nathanmkaya.inquirio.sample.createSimpleExampleSurvey