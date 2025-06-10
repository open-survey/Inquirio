package dev.nathanmkaya.inquirio.data.remote

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.SurveyDataError
import dev.nathanmkaya.inquirio.core.id.SurveyId
import dev.nathanmkaya.inquirio.survey.model.Survey
import dev.nathanmkaya.inquirio.survey.model.SurveyResponse

interface SurveyRemote {
    suspend fun submitResponse(response: SurveyResponse): Either<SurveyDataError, Unit>
    suspend fun submitBatch(responses: List<SurveyResponse>): Either<SurveyDataError, Unit>
    suspend fun fetchSurvey(surveyId: SurveyId): Either<SurveyDataError, Survey> =
        SurveyDataError.NetworkError(UnsupportedOperationException("Fetching not supported by this remote")).left()
    val identifier: String
}

class CompositeSurveyRemote(private val remotes: NonEmptyList<SurveyRemote>) : SurveyRemote {
    override val identifier: String = "composite[${remotes.map { it.identifier }.joinToString(",")}]"

    override suspend fun submitResponse(response: SurveyResponse): Either<SurveyDataError, Unit> = either {
        val errors = mutableListOf<SurveyDataError>()
        var successCount = 0
        remotes.forEach { remote ->
            remote.submitResponse(response).fold(
                { error -> errors.add(error) },
                { successCount++ }
            )
        }
        ensure(successCount > 0) { SurveyDataError.CompositeError(errors.first(), errors) }
    }

    override suspend fun submitBatch(responses: List<SurveyResponse>): Either<SurveyDataError, Unit> = either {
        remotes.forEach { it.submitBatch(responses).bind() }
    }
}