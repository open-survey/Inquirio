package dev.nathanmkaya.inquirio.survey.settings

data class SurveySettings(
    val allowBackNavigation: Boolean = true,
    val showProgressIndicator: Boolean = true,
    val randomizeQuestions: Boolean = false,
    val submitOnComplete: Boolean = true,
    val saveProgressLocally: Boolean = true,
    val validationStrategy: ValidationStrategy = ValidationStrategy.IMMEDIATE
)

enum class ValidationStrategy { 
    IMMEDIATE, ON_SUBMIT, HYBRID 
}