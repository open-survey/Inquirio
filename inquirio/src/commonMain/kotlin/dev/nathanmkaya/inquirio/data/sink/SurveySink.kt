package dev.nathanmkaya.inquirio.data.sink

import arrow.core.Either
import dev.nathanmkaya.inquirio.core.error.SurveyDataError
import dev.nathanmkaya.inquirio.core.id.ResponseId
import dev.nathanmkaya.inquirio.core.id.SurveyId
import dev.nathanmkaya.inquirio.survey.model.Survey
import dev.nathanmkaya.inquirio.survey.model.SurveyResponse

interface SurveySink {
    suspend fun saveSurvey(survey: Survey): Either<SurveyDataError, Unit>
    suspend fun saveResponse(response: SurveyResponse): Either<SurveyDataError, Unit>
    suspend fun loadResponse(responseId: ResponseId): Either<SurveyDataError, SurveyResponse>
    suspend fun loadAllResponses(surveyId: SurveyId): Either<SurveyDataError, List<SurveyResponse>>
    suspend fun deleteResponse(responseId: ResponseId): Either<SurveyDataError, Unit>
    val isPersistent: Boolean get() = false
}