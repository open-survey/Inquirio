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
    implementation(project(":inquirio-ui")) // For Compose UI components
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

### 4. Display the Survey UI

```kotlin
import androidx.compose.runtime.*
import dev.nathanmkaya.inquirio.ui.screen.SurveyScreen

@Composable
fun MyApp() {
    val flow = engine.startSurvey(survey)
    
    SurveyScreen(
        flow = flow,
        onSurveyComplete = { responses ->
            println("Survey completed with ${responses.size} responses")
            // Submit the responses
            CoroutineScope(Dispatchers.Main).launch {
                engine.submitResponse(survey, SurveyResponse.create(survey.id).copy(responses = responses))
                    .fold(
                        ifLeft = { error -> println("Submission failed: $error") },
                        ifRight = { println("Survey submitted successfully!") }
                    )
            }
        },
        onError = { error ->
            println("Survey error: $error")
        }
    )
}
```

### 5. Customize Question Renderers (Optional)

```kotlin
import dev.nathanmkaya.inquirio.ui.renderer.*

@Composable 
fun CustomBooleanRenderer(
    question: BooleanQuestion,
    response: QuestionResponse?,
    onResponse: (QuestionResponse) -> Unit
) {
    // Your custom UI implementation
    MyCustomToggleSwitch(
        question = question,
        onToggle = { value -> onResponse(BooleanResponse(question.id, value)) }
    )
}

val customRenderers = defaultQuestionRenderers() + mapOf(
    BooleanQuestion::class to { q, r, onR -> 
        CustomBooleanRenderer(q as BooleanQuestion, r, onR) 
    }
)

val registry = QuestionRendererRegistry(customRenderers)
SurveyScreen(flow = flow, rendererRegistry = registry)
```

## Architecture

Inquirio follows domain-driven design principles with a modular architecture:

### Core Library (`inquirio`)
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

### UI Library (`inquirio-ui`)
```
inquirio-ui/
├── renderer/       # Question renderer registry and default renderers
├── screen/         # Main survey screen composable
└── InquirioUI.kt   # Public API facade
```

The architecture maintains clean separation:
- **Headless Core**: Pure logic library with no UI dependencies
- **UI Layer**: Compose-based UI that depends on core
- **Extensible Renderers**: Type-safe registry for customizing question UI

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