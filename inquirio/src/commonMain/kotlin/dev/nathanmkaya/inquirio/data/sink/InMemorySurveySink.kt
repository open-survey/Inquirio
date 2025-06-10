package dev.nathanmkaya.inquirio.data.sink

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.nathanmkaya.inquirio.core.error.SurveyDataError
import dev.nathanmkaya.inquirio.core.id.ResponseId
import dev.nathanmkaya.inquirio.core.id.SurveyId
import dev.nathanmkaya.inquirio.survey.model.Survey
import dev.nathanmkaya.inquirio.survey.model.SurveyResponse

/**
 * In-memory survey sink for demos and testing
 */
class InMemorySurveySink : SurveySink {
    private val surveys = mutableMapOf<SurveyId, Survey>()
    private val responses = mutableMapOf<ResponseId, SurveyResponse>()
    
    override val isPersistent: Boolean = false
    
    override suspend fun saveSurvey(survey: Survey): Either<SurveyDataError, Unit> = 
        Either.Right(surveys.put(survey.id, survey).let { })
        
    override suspend fun saveResponse(response: SurveyResponse): Either<SurveyDataError, Unit> = 
        Either.Right(responses.put(response.responseId, response).let { })
        
    override suspend fun loadResponse(responseId: ResponseId): Either<SurveyDataError, SurveyResponse> =
        responses[responseId]?.right() ?: SurveyDataError.ResponseNotFound(responseId).left()
        
    override suspend fun loadAllResponses(surveyId: SurveyId): Either<SurveyDataError, List<SurveyResponse>> =
        Either.Right(responses.values.filter { it.surveyId == surveyId })
        
    override suspend fun deleteResponse(responseId: ResponseId): Either<SurveyDataError, Unit> =
        Either.Right(responses.remove(responseId).let { })
}