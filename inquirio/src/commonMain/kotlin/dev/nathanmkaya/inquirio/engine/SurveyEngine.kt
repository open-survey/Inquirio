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
 * Survey engine - main entry point
 */
class SurveyEngine(private val config: SurveySystemConfig) {
    private val compositeRemote: CompositeSurveyRemote? = config.remotes.toNonEmptyListOrNull()?.let { CompositeSurveyRemote(it) }

    suspend fun loadSurvey(surveyId: SurveyId): Either<SurveyDataError, Survey> =
        config.source.loadSurvey(surveyId).onRight { survey ->
            if (config.settings.cacheEnabled) {
                config.sink.saveSurvey(survey)
            }
        }

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

    fun startSurvey(survey: Survey): SurveyFlow {
        val navigator = DefaultSurveyNavigator() // Could be made configurable
        return SurveyFlow(survey, navigator, config.sink)
    }
}

/**
 * System configuration
 */
data class SurveySystemConfig(
    val source: SurveySource,
    val sink: SurveySink,
    val remotes: List<SurveyRemote>,
    val settings: SurveySystemSettings
)

data class SurveySystemSettings(
    val cacheEnabled: Boolean = true,
    val debugMode: Boolean = false
)

/**
 * DSL for survey system configuration
 */
@DslMarker
annotation class SurveySystemDsl

@SurveySystemDsl
class SurveySystemBuilder {
    private var source: SurveySource? = null
    private var sink: SurveySink? = null
    private val remotes = mutableListOf<SurveyRemote>()
    private var settings = SurveySystemSettings()

    fun source(source: SurveySource) { this.source = source }
    fun sink(sink: SurveySink) { this.sink = sink }
    fun remote(remote: SurveyRemote) { remotes.add(remote) }
    fun settings(block: SurveySystemSettings.() -> SurveySystemSettings) { settings = settings.block() }

    fun build(): Either<String, SurveySystemConfig> {
        val validSource = source ?: return "Survey source must be configured.".left()
        val validSink = sink ?: return "Survey sink must be configured.".left()
        return SurveySystemConfig(validSource, validSink, remotes, settings).right()
    }
}

fun surveySystem(block: SurveySystemBuilder.() -> Unit): Either<String, SurveySystemConfig> =
    SurveySystemBuilder().apply(block).build()