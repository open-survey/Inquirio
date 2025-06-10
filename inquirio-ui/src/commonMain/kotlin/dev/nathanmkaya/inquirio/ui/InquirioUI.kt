/**
 * # Inquirio Compose UI - Main API
 * 
 * A comprehensive Jetpack Compose UI library for the Inquirio survey system.
 * This module provides ready-to-use composables, customizable renderers, and
 * a flexible architecture for building survey interfaces.
 * 
 * ## Key Features
 * - **Ready-to-Use Components**: Complete survey screen with navigation
 * - **Customizable Renderers**: Override any question type's UI implementation
 * - **Material Design 3**: Modern, accessible UI following Material guidelines
 * - **Type-Safe Registry**: Compile-time safety for question-to-renderer mapping
 * - **Responsive Design**: Adapts to different screen sizes and orientations
 * 
 * ## Quick Start
 * ```kotlin
 * // 1. Create a survey using the core library
 * val survey = Survey(
 *     id = "feedback",
 *     title = "User Feedback",
 *     questions = listOf(
 *         FreeTextQuestion(id = "name", text = "Your name?", isRequired = true),
 *         BooleanQuestion(id = "satisfied", text = "Are you satisfied?")
 *     )
 * ).getOrThrow()
 * 
 * // 2. Set up the survey system
 * val engine = SurveyEngine(surveySystem {
 *     source(InMemorySurveySource())
 *     sink(InMemorySurveySink())
 * }.getOrThrow())
 * 
 * // 3. Display the survey
 * val flow = engine.startSurvey(survey)
 * SurveyScreen(
 *     flow = flow,
 *     onSurveyComplete = { response ->
 *         println("Survey completed: ${response.responses.size} responses")
 *     }
 * )
 * ```
 * 
 * ## Customization Example
 * ```kotlin
 * // Custom renderer for boolean questions
 * val customRenderers = defaultQuestionRenderers() + mapOf(
 *     BooleanQuestion::class to { question, response, onResponse ->
 *         MyCustomBooleanRenderer(question as BooleanQuestion, response, onResponse)
 *     }
 * )
 * 
 * val registry = QuestionRendererRegistry(customRenderers)
 * SurveyScreen(flow = flow, rendererRegistry = registry)
 * ```
 * 
 * @since 1.0.0
 */
@file:Suppress("UNUSED")
package dev.nathanmkaya.inquirio.ui

// Re-export main UI components for easy access

/**
 * Question renderer registry for mapping question types to UI components.
 * Enables type-safe, customizable question rendering.
 */
typealias QuestionRendererRegistry = dev.nathanmkaya.inquirio.ui.renderer.QuestionRendererRegistry

/**
 * Type alias for question renderer functions.
 * Defines the signature for composable functions that render questions.
 */
typealias QuestionRenderer = dev.nathanmkaya.inquirio.ui.renderer.QuestionRenderer

// Note: The main components are imported directly:
// - import dev.nathanmkaya.inquirio.ui.screen.SurveyScreen
// - import dev.nathanmkaya.inquirio.ui.renderer.BooleanQuestionRenderer
// - import dev.nathanmkaya.inquirio.ui.renderer.FreeTextQuestionRenderer  
// - import dev.nathanmkaya.inquirio.ui.renderer.MultipleChoiceQuestionRenderer
// - import dev.nathanmkaya.inquirio.ui.renderer.defaultQuestionRenderers