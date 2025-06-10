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
 * A complete survey with questions and metadata.
 * 
 * Survey is the main aggregate root that contains a collection of questions
 * and manages their flow and validation. It uses validation by construction
 * to ensure all surveys are created in a valid state.
 * 
 * ## Key Features
 * - **Smart Constructor**: Validates all properties during creation
 * - **Question Flow**: Automatically creates a navigation chain from questions
 * - **Type Safety**: Uses domain identifiers to prevent ID mix-ups
 * - **Extensible**: Supports custom metadata and settings
 * 
 * ## Example Usage
 * ```kotlin
 * val survey = Survey(
 *     id = "user-feedback",
 *     title = "User Feedback Survey",
 *     description = "Help us improve our service",
 *     questions = listOf(
 *         FreeTextQuestion(id = "name", text = "Your name?", isRequired = true),
 *         BooleanQuestion(id = "satisfied", text = "Are you satisfied?")
 *     )
 * ).getOrElse { errors ->
 *     // Handle validation errors
 *     println("Survey creation failed: $errors")
 *     return
 * }
 * ```
 * 
 * @param id Unique identifier for the survey
 * @param title Display title for the survey
 * @param description Optional description explaining the survey purpose
 * @param version Version string for survey versioning (default: "1.0")
 * @param firstQuestion Entry point for survey navigation (auto-generated from questions)
 * @param allQuestions Internal list of all questions for validation and lookup
 * @param settings Survey behavior configuration
 * @param metadata Additional custom data attached to the survey
 * @since 1.0.0
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
         * Smart constructor that validates all survey properties.
         * 
         * Creates a survey with comprehensive validation including:
         * - Survey ID format validation
         * - Non-empty title requirement
         * - At least one question requirement
         * - Duplicate question ID detection
         * - Automatic question navigation chain creation
         * 
         * All validation errors are collected and returned together, allowing
         * the caller to address multiple issues at once.
         * 
         * @param id String identifier that will be converted to a type-safe [SurveyId]
         * @param title Display title for the survey (must not be blank)
         * @param description Optional description explaining the survey purpose
         * @param version Version string for survey versioning
         * @param questions List of questions to include in the survey
         * @param settings Survey behavior configuration
         * @param metadata Additional custom data to attach to the survey
         * @return Either a list of validation errors or a valid Survey instance
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