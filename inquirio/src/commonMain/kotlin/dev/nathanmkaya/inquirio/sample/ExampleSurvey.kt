package dev.nathanmkaya.inquirio.sample

import arrow.core.Either
import arrow.core.NonEmptyList
import dev.nathanmkaya.inquirio.core.error.SurveyValidationError
import dev.nathanmkaya.inquirio.question.config.QuestionOption
import dev.nathanmkaya.inquirio.question.type.BooleanQuestion
import dev.nathanmkaya.inquirio.question.type.MultipleChoiceQuestion
import dev.nathanmkaya.inquirio.survey.model.Survey

/**
 * Example survey creation for demos and testing
 */
fun createSimpleExampleSurvey(): Either<NonEmptyList<SurveyValidationError>, Survey> {
    val q1 = BooleanQuestion(
        id = "satisfied",
        text = "Are you satisfied with our service?",
        isRequired = true
    ).getOrNull()!! // Using !! for brevity in example

    val q2 = MultipleChoiceQuestion(
        id = "improvements",
        text = "What areas need improvement?",
        options = listOf(
            QuestionOption("ui", "User Interface"),
            QuestionOption("perf", "Performance"),
            QuestionOption("support", "Customer Support")
        ),
        minSelections = 1,
        maxSelections = 2
    ).getOrNull()!!

    return Survey(
        id = "customer-feedback-2024",
        title = "Customer Feedback Survey",
        questions = listOf(q1, q2)
    )
}