package dev.nathanmkaya.inquirio.engine

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.SurveyDataError
import dev.nathanmkaya.inquirio.core.error.SurveyError
import dev.nathanmkaya.inquirio.core.id.SurveyId
import dev.nathanmkaya.inquirio.data.remote.CompositeSurveyRemote
import dev.nathanmkaya.inquirio.data.remote.SurveyRemote
import dev.nathanmkaya.inquirio.data.sink.SurveySink
import dev.nathanmkaya.inquirio.data.source.SurveySource
import dev.nathanmkaya.inquirio.survey.flow.DefaultSurveyNavigator
import dev.nathanmkaya.inquirio.survey.flow.SurveyFlow
import dev.nathanmkaya.inquirio.survey.model.Survey
import dev.nathanmkaya.inquirio.survey.model.SurveyResponse

/**
 * Main orchestrator for survey operations.
 * 
 * The SurveyEngine is the primary entry point for all survey-related operations.
 * It coordinates between data sources, sinks, and remote endpoints to provide
 * a unified interface for survey management.
 * 
 * ## Features
 * - Load surveys from configured sources with optional caching
 * - Submit responses with validation and remote synchronization
 * - Start survey flows with navigation support
 * - Handle errors gracefully with detailed error information
 * 
 * ## Example Usage
 * ```kotlin
 * val engine = SurveyEngine(surveySystem {
 *     source(InMemorySurveySource())
 *     sink(InMemorySurveySink())
 *     remote(ConsoleDebugRemote())
 * }.getOrThrow())
 * 
 * // Load a survey
 * val survey = engine.loadSurvey(SurveyId("feedback")).getOrNull()
 * 
 * // Start survey flow
 * val flow = engine.startSurvey(survey)
 * ```
 * 
 * @param config System configuration including data sources and settings
 * @since 1.0.0
 */
class SurveyEngine(private val config: SurveySystemConfig) {
    private val compositeRemote: CompositeSurveyRemote? = config.remotes.toNonEmptyListOrNull()?.let { CompositeSurveyRemote(it) }

    /**
     * Load a survey from the configured source.
     * 
     * If caching is enabled in the system settings, the loaded survey will be
     * automatically saved to the configured sink for faster future access.
     * 
     * @param surveyId The unique identifier of the survey to load
     * @return Either a [SurveyDataError] if loading fails, or the loaded [Survey]
     */
    suspend fun loadSurvey(surveyId: SurveyId): Either<SurveyDataError, Survey> =
        config.source.loadSurvey(surveyId).onRight { survey ->
            if (config.settings.cacheEnabled) {
                config.sink.saveSurvey(survey)
            }
        }

    /**
     * Submit a completed survey response.
     * 
     * This method performs a three-step process:
     * 1. **Validate** the response against the survey rules
     * 2. **Save** the response to the local sink
     * 3. **Submit** the response to configured remote endpoints
     * 
     * All validation errors are preserved in their original form, allowing
     * the caller to handle specific validation failures appropriately.
     * 
     * @param survey The survey definition to validate against
     * @param response The user's responses to submit
     * @return Either a [SurveyError] if any step fails, or [Unit] on success
     */
    suspend fun submitResponse(survey: Survey, response: SurveyResponse): Either<SurveyError, Unit> = either {
        // 1. Validate. If it fails, the `NonEmptyList<ResponseValidationError>` is preserved.
        val validatedResponse = response.validateWith(survey)
            .mapLeft { it as SurveyError } // Map to the common supertype
            .bind()
        val finalResponse = validatedResponse.markComplete()

        // 2. Save. If it fails, the `SurveyDataError` is preserved.
        config.sink.saveResponse(finalResponse)
            .mapLeft { it as SurveyError } // Map to the common supertype
            .bind()

        // 3. Submit. If it fails, the `SurveyDataError` is preserved.
        compositeRemote?.submitResponse(finalResponse)
            ?.mapLeft { it as SurveyError } // Map to the common supertype
            ?.bind()
    }

    /**
     * Start a new survey flow for the given survey.
     * 
     * Creates a [SurveyFlow] instance that manages the user's journey through
     * the survey questions, handles navigation, and tracks responses.
     * 
     * @param survey The survey to start a flow for
     * @return A new [SurveyFlow] instance ready for user interaction
     */
    fun startSurvey(survey: Survey): SurveyFlow {
        val navigator = DefaultSurveyNavigator() // Could be made configurable
        return SurveyFlow(survey, navigator, config.sink)
    }
}

/**
 * Configuration for the survey system.
 * 
 * Contains all the components needed to run a survey system, including
 * data sources, persistence, remote endpoints, and system settings.
 * 
 * @param source Where to load surveys from
 * @param sink Where to save survey data locally
 * @param remotes List of remote endpoints for submission (optional)
 * @param settings System-level configuration options
 */
data class SurveySystemConfig(
    val source: SurveySource,
    val sink: SurveySink,
    val remotes: List<SurveyRemote>,
    val settings: SurveySystemSettings
)

/**
 * System-level settings and preferences.
 * 
 * @param cacheEnabled Whether to cache loaded surveys in the sink
 * @param debugMode Whether to enable debug logging and validation
 */
data class SurveySystemSettings(
    val cacheEnabled: Boolean = true,
    val debugMode: Boolean = false
)

/**
 * DSL marker for survey system configuration.
 * Ensures type-safe DSL usage and prevents nesting of configuration blocks.
 */
@DslMarker
annotation class SurveySystemDsl

/**
 * Builder for survey system configuration using a type-safe DSL.
 * 
 * Provides a fluent API for configuring survey system components:
 * ```kotlin
 * val config = surveySystem {
 *     source(InMemorySurveySource())
 *     sink(InMemorySurveySink())
 *     remote(HttpSurveyRemote("https://api.example.com"))
 *     settings { copy(cacheEnabled = false) }
 * }
 * ```
 */
@SurveySystemDsl
class SurveySystemBuilder {
    private var source: SurveySource? = null
    private var sink: SurveySink? = null
    private val remotes = mutableListOf<SurveyRemote>()
    private var settings = SurveySystemSettings()

    /** Configure the survey source for loading surveys */
    fun source(source: SurveySource) { this.source = source }
    
    /** Configure the survey sink for local persistence */
    fun sink(sink: SurveySink) { this.sink = sink }
    
    /** Add a remote endpoint for survey submission */
    fun remote(remote: SurveyRemote) { remotes.add(remote) }
    
    /** Configure system settings */
    fun settings(block: SurveySystemSettings.() -> SurveySystemSettings) { settings = settings.block() }

    /**
     * Build the survey system configuration.
     * 
     * Validates that required components (source and sink) are configured.
     * 
     * @return Either an error message if validation fails, or the built configuration
     */
    fun build(): Either<String, SurveySystemConfig> {
        val validSource = source ?: return "Survey source must be configured.".left()
        val validSink = sink ?: return "Survey sink must be configured.".left()
        return SurveySystemConfig(validSource, validSink, remotes, settings).right()
    }
}

/**
 * Create a survey system configuration using a type-safe DSL.
 * 
 * ## Example Usage
 * ```kotlin
 * val config = surveySystem {
 *     source(InMemorySurveySource())
 *     sink(InMemorySurveySink())
 *     remote(ConsoleDebugRemote())
 *     settings { copy(cacheEnabled = true, debugMode = false) }
 * }.getOrElse { error ->
 *     throw IllegalStateException("Failed to configure survey system: $error")
 * }
 * 
 * val engine = SurveyEngine(config)
 * ```
 * 
 * @param block Configuration block using the DSL
 * @return Either an error message if configuration is invalid, or the built [SurveySystemConfig]
 */
fun surveySystem(block: SurveySystemBuilder.() -> Unit): Either<String, SurveySystemConfig> =
    SurveySystemBuilder().apply(block).build()