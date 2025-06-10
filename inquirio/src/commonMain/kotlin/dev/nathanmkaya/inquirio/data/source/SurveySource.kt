package dev.nathanmkaya.inquirio.data.source

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.SurveyDataError
import dev.nathanmkaya.inquirio.core.id.SurveyId
import dev.nathanmkaya.inquirio.survey.model.Survey

interface SurveySource {
    suspend fun loadSurvey(surveyId: SurveyId): Either<SurveyDataError, Survey>
    val priority: Int get() = 0
}

class CompositeSurveySource(private val sources: NonEmptyList<SurveySource>) : SurveySource {
    override val priority: Int = sources.maxOf { it.priority }

    override suspend fun loadSurvey(surveyId: SurveyId): Either<SurveyDataError, Survey> = either {
        val errors = mutableListOf<SurveyDataError>()
        sources.sortedByDescending { it.priority }.forEach { source ->
            source.loadSurvey(surveyId).onRight { survey -> return@either survey }
                .onLeft { error -> errors.add(error) }
        }
        raise(SurveyDataError.CompositeError(errors.last(), errors))
    }
}