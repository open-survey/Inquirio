package dev.nathanmkaya.inquirio.data.remote

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.nathanmkaya.inquirio.core.error.SurveyDataError
import dev.nathanmkaya.inquirio.survey.model.SurveyResponse

/**
 * Console debug remote for development and testing
 */
class ConsoleDebugRemote(override val identifier: String = "console-debug") : SurveyRemote {
    override suspend fun submitResponse(response: SurveyResponse): Either<SurveyDataError, Unit> = Either.Right(
        println("📤 Console Remote [$identifier]: Submitting response ${response.responseId} for survey ${response.surveyId}")
    )
    
    override suspend fun submitBatch(responses: List<SurveyResponse>): Either<SurveyDataError, Unit> {
        println("📤 Console Remote [$identifier]: Submitting batch of ${responses.size} responses")
        responses.forEach { 
            submitResponse(it).fold(
                { error -> return error.left() },
                { /* continue */ }
            )
        }
        return Unit.right()
    }
}