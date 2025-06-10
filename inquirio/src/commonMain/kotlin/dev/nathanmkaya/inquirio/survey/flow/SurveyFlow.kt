package dev.nathanmkaya.inquirio.survey.flow

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.NavigationError
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError
import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.data.sink.SurveySink
import dev.nathanmkaya.inquirio.question.model.Question
import dev.nathanmkaya.inquirio.question.model.QuestionNode
import dev.nathanmkaya.inquirio.response.model.QuestionResponse
import dev.nathanmkaya.inquirio.survey.model.Survey
import dev.nathanmkaya.inquirio.survey.model.SurveyResponse

interface SurveyNavigator {
    fun canNavigateNext(currentNode: QuestionNode, responses: Map<QuestionId, QuestionResponse>): Boolean
    fun canNavigatePrevious(currentNode: QuestionNode): Boolean
    fun getNextQuestion(currentNode: QuestionNode, responses: Map<QuestionId, QuestionResponse>): Option<QuestionNode>
    fun getPreviousQuestion(currentNode: QuestionNode): Option<QuestionNode>
}

class DefaultSurveyNavigator : SurveyNavigator {
    override fun canNavigateNext(currentNode: QuestionNode, responses: Map<QuestionId, QuestionResponse>): Boolean {
        val question = currentNode.question
        val response = responses[question.id]
        return !question.isRequired || response != null
    }
    override fun canNavigatePrevious(currentNode: QuestionNode): Boolean = currentNode.previous != null
    override fun getNextQuestion(currentNode: QuestionNode, responses: Map<QuestionId, QuestionResponse>): Option<QuestionNode> =
        currentNode.nextResolver(responses).toOption()
    override fun getPreviousQuestion(currentNode: QuestionNode): Option<QuestionNode> =
        currentNode.previous.toOption()
}

class SurveyFlow(
    private val survey: Survey,
    private val navigator: SurveyNavigator,
    private val sink: SurveySink? = null // Optional sink for auto-saving
) {
    private var currentNodeOpt: Option<QuestionNode> = survey.firstQuestion.toOption()
    private var surveyResponse: SurveyResponse = SurveyResponse.create(survey.id)

    val currentQuestion: Option<Question> get() = currentNodeOpt.map { it.question }
    val currentResponses: Map<QuestionId, QuestionResponse> get() = surveyResponse.responses

    fun addResponse(response: QuestionResponse): Either<NonEmptyList<ResponseValidationError>, SurveyFlow> = either {
        val question = currentQuestion.getOrElse { 
            raise(nonEmptyListOf(ResponseValidationError.InvalidResponseType(response.questionId, "Question", "None")))
        }
        ensure(question.id == response.questionId) { nonEmptyListOf(ResponseValidationError.InvalidResponseType(response.questionId, "Response for current question", "Response for other question")) }

        question.validate(response).bind()
        surveyResponse = surveyResponse.addResponse(response)
        this@SurveyFlow
    }

    suspend fun next(): Either<NavigationError, SurveyFlow> = either {
        val currentNode = currentNodeOpt.getOrElse { raise(NavigationError.NoNextQuestion) }
        ensure(navigator.canNavigateNext(currentNode, surveyResponse.responses)) {
            NavigationError.NavigationBlocked("Response required for the current question.")
        }
        currentNodeOpt = navigator.getNextQuestion(currentNode, surveyResponse.responses)
            .getOrElse { raise(NavigationError.NoNextQuestion) }.toOption()

        sink?.saveResponse(surveyResponse)
        this@SurveyFlow
    }

    suspend fun previous(): Either<NavigationError, SurveyFlow> = either {
        val currentNode = currentNodeOpt.getOrElse { raise(NavigationError.NoPreviousQuestion) }
        ensure(survey.settings.allowBackNavigation) { NavigationError.NavigationBlocked("Backward navigation is disabled.") }
        ensure(navigator.canNavigatePrevious(currentNode)) { NavigationError.NoPreviousQuestion }
        currentNodeOpt = navigator.getPreviousQuestion(currentNode)
            .getOrElse { raise(NavigationError.NoPreviousQuestion) }.toOption()

        sink?.saveResponse(surveyResponse)
        this@SurveyFlow
    }
}