package dev.nathanmkaya.inquirio.data.source

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.nathanmkaya.inquirio.core.error.SurveyDataError
import dev.nathanmkaya.inquirio.core.id.SurveyId
import dev.nathanmkaya.inquirio.survey.model.Survey

/**
 * In-memory survey source for demos and testing
 */
class InMemorySurveySource(
    private val surveys: Map<SurveyId, Survey>, 
    override val priority: Int = 100
) : SurveySource {
    override suspend fun loadSurvey(surveyId: SurveyId): Either<SurveyDataError, Survey> =
        surveys[surveyId]?.right() ?: SurveyDataError.SurveyNotFound(surveyId).left()
}