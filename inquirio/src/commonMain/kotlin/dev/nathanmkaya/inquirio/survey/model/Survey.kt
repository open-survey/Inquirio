package dev.nathanmkaya.inquirio.survey.model

import arrow.core.*
import arrow.core.raise.*
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError
import dev.nathanmkaya.inquirio.core.error.SurveyValidationError
import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.core.id.ResponseId
import dev.nathanmkaya.inquirio.core.id.SurveyId
import dev.nathanmkaya.inquirio.platform.getCurrentTimeMillis
import dev.nathanmkaya.inquirio.question.model.Question
import dev.nathanmkaya.inquirio.question.model.QuestionNode
import dev.nathanmkaya.inquirio.response.model.QuestionResponse
import dev.nathanmkaya.inquirio.survey.settings.SurveySettings

/**
 * Survey with validation and smart constructor
 * CUPID: Composable, Domain-based
 */
data class Survey private constructor(
    val id: SurveyId,
    val title: String,
    val description: String? = null,
    val version: String = "1.0",
    val firstQuestion: QuestionNode? = null,
    private val allQuestions: List<Question> = emptyList(), // For validation and lookup
    val settings: SurveySettings = SurveySettings(),
    val metadata: Map<String, Any> = emptyMap()
) {
    companion object {
        /**
         * Smart constructor with validation using Raise DSL.
         * It validates properties and checks for duplicate question IDs.
         */
        operator fun invoke(
            id: String,
            title: String,
            description: String? = null,
            version: String = "1.0",
            questions: List<Question> = emptyList(),
            settings: SurveySettings = SurveySettings(),
            metadata: Map<String, Any> = emptyMap()
        ): Either<NonEmptyList<SurveyValidationError>, Survey> = either {
            val errors = mutableListOf<SurveyValidationError>()

            val validId = SurveyId(id).getOrElse {
                errors.addAll(it)
                SurveyId.unsafe("invalid-id") // Provide a dummy to continue validation
            }

            if (title.isBlank()) {
                errors.add(SurveyValidationError.EmptySurveyTitle)
            }
            if (questions.isEmpty()) {
                errors.add(SurveyValidationError.NoQuestions)
            }

            val questionIds = questions.map { it.id }
            val duplicateIds = questionIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            duplicateIds.forEach { errors.add(SurveyValidationError.DuplicateQuestionId(it)) }

            ensure(errors.isEmpty()) { errors.toNonEmptyListOrNull()!! }

            Survey(
                id = validId,
                title = title,
                description = description,
                version = version,
                firstQuestion = QuestionNode.linearChain(questions),
                allQuestions = questions,
                settings = settings,
                metadata = metadata
            )
        }
    }

    fun findQuestion(id: QuestionId): Question? = allQuestions.find { it.id == id }

    fun getAllQuestionsAsSequence(): Sequence<Question> = allQuestions.asSequence()

    fun getVisibleQuestionsAsSequence(responses: Map<QuestionId, QuestionResponse>): Sequence<Question> =
        generateSequence(firstQuestion) { node ->
            node.nextResolver(responses)
        }.map { it.question }
}

/**
 * Complete survey response containing all answers
 */
data class SurveyResponse private constructor(
    val surveyId: SurveyId,
    val responseId: ResponseId,
    val startTime: Long,
    val endTime: Long?,
    val isComplete: Boolean,
    val responses: Map<QuestionId, QuestionResponse>,
    val metadata: Map<String, Any>
) {
    companion object {
        fun create(surveyId: SurveyId): SurveyResponse = SurveyResponse(
            surveyId = surveyId,
            responseId = ResponseId.generate(),
            startTime = getCurrentTimeMillis(),
            endTime = null,
            isComplete = false,
            responses = emptyMap(),
            metadata = emptyMap()
        )
    }

    fun addResponse(response: QuestionResponse): SurveyResponse =
        copy(responses = responses + (response.questionId to response))

    fun removeResponse(questionId: QuestionId): SurveyResponse =
        copy(responses = responses - questionId)

    fun markComplete(): SurveyResponse =
        copy(isComplete = true, endTime = getCurrentTimeMillis())

    fun getResponse(questionId: QuestionId): Option<QuestionResponse> =
        responses[questionId].toOption()

    fun validateWith(survey: Survey): Either<NonEmptyList<ResponseValidationError>, SurveyResponse> = either {
        val allErrors = survey.getAllQuestionsAsSequence().mapNotNull { question ->
            val response = responses[question.id]
            // Only validate if a response exists or if the question is required
            if (response != null || question.isRequired) {
                question.validate(response).leftOrNull()
            } else {
                null
            }
        }.flatten().toList()

        ensure(allErrors.isEmpty()) { allErrors.toNonEmptyListOrNull()!! }
        this@SurveyResponse
    }
}