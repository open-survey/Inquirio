# Inquirio

A type-safe, functional Kotlin Multiplatform survey library powered by Arrow-kt.

## Why Inquirio?

- **🔥 Functional First**: Built with Arrow-kt for explicit error handling using `Either` types
- **📱 Kotlin Multiplatform**: Share survey logic across Android, iOS, and more
- **🛡️ Type-Safe**: Smart constructors and validation by construction prevent invalid states
- **🏗️ Domain-Driven**: Clean architecture with proper separation of concerns
- **⚡ Headless Core**: Pure logic library with no UI dependencies
- **🔗 Flexible Navigation**: Support for linear and conditional survey flows

## Features

### Core Capabilities
- **Question Types**: Boolean, Multiple Choice, Free Text (extensible)
- **Smart Validation**: Validation by construction with detailed error reporting
- **Flow Management**: Navigate forward/backward with conditional logic
- **Data Persistence**: Pluggable storage with source/sink abstraction
- **Remote Submission**: Configurable remote endpoints with composite support

### Developer Experience
- **Type-Safe APIs**: Domain identifiers prevent mixing survey/question/response IDs
- **Comprehensive Errors**: Detailed error hierarchy for robust error handling
- **DSL Configuration**: Intuitive system configuration with builder pattern
- **Clean API**: Simple facade hiding internal complexity

## Getting Started

### 1. Add Dependencies

```kotlin
// In your module's build.gradle.kts
dependencies {
    implementation("io.arrow-kt:arrow-core:2.1.2")
    implementation(project(":inquirio"))
}
```

### 2. Create a Survey

```kotlin
import dev.nathanmkaya.inquirio.*
import dev.nathanmkaya.inquirio.engine.surveySystem

// Define questions
val nameQuestion = FreeTextQuestion(
    id = "name",
    text = "What's your name?",
    isRequired = true
)

val ageQuestion = MultipleChoiceQuestion(
    id = "age-group", 
    text = "Select your age group:",
    options = listOf(
        QuestionOption("under-18", "Under 18"),
        QuestionOption("18-30", "18-30"),
        QuestionOption("over-30", "Over 30")
    ),
    isRequired = true
)

// Create survey
val survey = Survey(
    id = "user-feedback",
    title = "User Feedback Survey",
    description = "Help us improve our service",
    questions = listOf(nameQuestion, ageQuestion)
).getOrElse { errors ->
    // Handle validation errors
    println("Survey creation failed: $errors")
    return
}
```

### 3. Configure the System

```kotlin
val systemConfig = surveySystem {
    source(InMemorySurveySource())
    sink(InMemorySurveySink())
    remote(ConsoleDebugRemote())
    settings { copy(cacheEnabled = true) }
}.getOrElse { error ->
    println("System configuration failed: $error")
    return
}

val engine = SurveyEngine(systemConfig)
```

### 4. Run a Survey Flow

```kotlin
suspend fun runSurvey() {
    // Start the survey
    val flow = engine.startSurvey(survey)
    
    while (flow.currentQuestion.isSome()) {
        val question = flow.currentQuestion.getOrNull()!!
        println("Question: ${question.text}")
        
        // Collect user input (example with text response)
        val userInput = readLine() ?: ""
        val response = TextResponse(
            questionId = question.id,
            value = userInput
        )
        
        // Add response and navigate
        flow.addResponse(response).fold(
            ifLeft = { errors -> println("Invalid response: $errors") },
            ifRight = { 
                flow.next().fold(
                    ifLeft = { error -> 
                        if (error == NavigationError.NoNextQuestion) {
                            println("Survey complete!")
                            // Submit responses
                            engine.submitResponse(survey, flow.currentResponses)
                                .fold(
                                    ifLeft = { error -> println("Submission failed: $error") },
                                    ifRight = { println("Survey submitted successfully!") }
                                )
                        } else {
                            println("Navigation error: $error")
                        }
                    },
                    ifRight = { /* Continue to next question */ }
                )
            }
        )
    }
}
```

## Architecture

Inquirio follows domain-driven design principles:

```
inquirio/
├── core/           # Domain identifiers, errors, validation
├── question/       # Question models, types, and configuration  
├── response/       # Response models and types
├── survey/         # Survey aggregates, flow, and settings
├── data/           # Data access layer (sources, sinks, remotes)
├── engine/         # System configuration and orchestration
└── platform/       # Platform-specific implementations
```

## Error Handling

All operations return `Either<Error, Success>` for explicit error handling:

```kotlin
survey.validate().fold(
    ifLeft = { errors: NonEmptyList<SurveyValidationError> ->
        errors.forEach { error ->
            when (error) {
                is SurveyValidationError.EmptySurveyTitle -> // Handle empty title
                is SurveyValidationError.DuplicateQuestionId -> // Handle duplicate ID
                // ... handle other errors
            }
        }
    },
    ifRight = { validSurvey -> /* Use valid survey */ }
)
```

## Platform Support

- **Android**: Native Android integration
- **iOS**: Native iOS integration  
- **JVM**: Desktop and server applications
- **Future**: Web (Kotlin/JS), Native (Kotlin/Native)

## Contributing

This project uses functional programming principles with Arrow-kt. Please ensure:

- All public functions are properly documented with KDoc
- Error cases are handled explicitly with `Either` types
- Domain models use smart constructors for validation
- Tests cover both success and error scenarios

## License

MIT License - see LICENSE file for details.