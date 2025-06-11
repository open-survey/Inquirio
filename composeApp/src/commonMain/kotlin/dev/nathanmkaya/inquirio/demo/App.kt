package dev.nathanmkaya.inquirio.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import arrow.core.*
import dev.nathanmkaya.inquirio.*
import dev.nathanmkaya.inquirio.engine.surveySystem
import dev.nathanmkaya.inquirio.question.config.QuestionOption
import dev.nathanmkaya.inquirio.question.config.TextConfig
import dev.nathanmkaya.inquirio.question.config.TextInputType
import dev.nathanmkaya.inquirio.ui.screen.SurveyScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf<DemoScreen>(DemoScreen.Home) }
        
        when (currentScreen) {
            is DemoScreen.Home -> {
                HomeScreen(
                    onStartSurvey = { currentScreen = DemoScreen.Survey }
                )
            }
            is DemoScreen.Survey -> {
                SurveyDemoScreen(
                    onBackToHome = { currentScreen = DemoScreen.Home },
                    onSurveyComplete = { responses -> 
                        currentScreen = DemoScreen.Results(responses)
                    }
                )
            }
            is DemoScreen.Results -> {
                val resultsScreen = currentScreen as DemoScreen.Results
                ResultsScreen(
                    responses = resultsScreen.responses,
                    onBackToHome = { currentScreen = DemoScreen.Home }
                )
            }
        }
    }
}

sealed class DemoScreen {
    data object Home : DemoScreen()
    data object Survey : DemoScreen()
    data class Results(val responses: Map<QuestionId, QuestionResponse>) : DemoScreen()
}

@Composable
fun HomeScreen(
    onStartSurvey: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍 Inquirio Demo",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "A type-safe, functional Kotlin Multiplatform survey library",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Demo Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val features = listOf(
                    "✅ Boolean questions with custom labels",
                    "✅ Text input with validation",
                    "✅ Multiple choice questions",
                    "✅ Navigation with validation",
                    "✅ Error handling",
                    "✅ Functional programming with Arrow-kt"
                )
                
                features.forEach { feature ->
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartSurvey,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Survey Demo")
        }
    }
}

@Composable
fun SurveyDemoScreen(
    onBackToHome: () -> Unit,
    onSurveyComplete: (Map<QuestionId, QuestionResponse>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var surveyState by remember { mutableStateOf<SurveyState>(SurveyState.Loading) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val survey = createDemoSurvey()
                val engine = createSurveyEngine()
                val flow = engine.startSurvey(survey)
                surveyState = SurveyState.Ready(survey, engine, flow)
            } catch (e: Exception) {
                surveyState = SurveyState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    when (val state = surveyState) {
        is SurveyState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Setting up survey...")
                }
            }
        }
        
        is SurveyState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackToHome) {
                        Text("Back to Home")
                    }
                }
            }
        }
        
        is SurveyState.Ready -> {
            Column {
                // Header with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onBackToHome) {
                        Text("← Back")
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "User Feedback Survey",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Survey content
                SurveyScreen(
                    flow = state.flow,
                    onSurveyComplete = { responses ->
                        // Submit to engine in background
                        scope.launch {
                            try {
                                state.engine.submitResponse(state.survey, 
                                    SurveyResponse.create(state.survey.id).copy(responses = responses).markComplete()
                                ).fold(
                                    ifLeft = { error -> 
                                        println("Submission error: $error")
                                    },
                                    ifRight = { 
                                        println("Survey submitted successfully!")
                                    }
                                )
                            } catch (e: Exception) {
                                println("Error during submission: ${e.message}")
                            }
                        }
                        
                        // Navigate to results
                        onSurveyComplete(responses)
                    },
                    onError = { error ->
                        println("Survey error: $error")
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ResultsScreen(
    responses: Map<QuestionId, QuestionResponse>,
    onBackToHome: () -> Unit
) {
    // Create simple question mapping for display
    val questionTexts = mapOf(
        "full-name" to "Full Name",
        "email" to "Email Address", 
        "satisfied" to "Satisfied with Inquirio",
        "experience-level" to "Experience Level",
        "features-used" to "Interested Features",
        "additional-feedback" to "Additional Feedback"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "🎉 Survey Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Thank you for your feedback. Here's what you shared:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        responses.forEach { (questionId, response) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = questionTexts[questionId.value] ?: questionId.value,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatResponse(response),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}

sealed class SurveyState {
    data object Loading : SurveyState()
    data class Error(val message: String) : SurveyState()
    data class Ready(
        val survey: Survey,
        val engine: SurveyEngine,
        val flow: SurveyFlow
    ) : SurveyState()
}

fun createDemoSurvey(): Survey {
    return Survey(
        id = "user-feedback-demo",
        title = "User Feedback Survey",
        description = "Help us understand your experience with Inquirio",
        questions = listOf(
            // Text question with validation
            FreeTextQuestion(
                id = "full-name",
                text = "What is your full name?",
                description = "Please enter your first and last name",
                isRequired = true,
                config = TextConfig(
                    placeholder = "Enter your full name",
                    maxLength = 100,
                    inputType = TextInputType.PLAIN
                )
            ).getOrElse { throw Exception("Failed to create name question") },
            
            // Email question
            FreeTextQuestion(
                id = "email",
                text = "What is your email address?",
                description = "We'll use this to follow up if needed",
                isRequired = true,
                config = TextConfig(
                    placeholder = "your.email@example.com",
                    maxLength = 255,
                    inputType = TextInputType.EMAIL
                )
            ).getOrElse { throw Exception("Failed to create email question") },
            
            // Boolean question
            BooleanQuestion(
                id = "satisfied",
                text = "Are you satisfied with Inquirio?",
                description = "Your honest feedback helps us improve",
                isRequired = true,
                trueLabel = "Very Satisfied",
                falseLabel = "Needs Improvement"
            ).getOrElse { throw Exception("Failed to create satisfaction question") },
            
            // Multiple choice question (single selection)
            MultipleChoiceQuestion(
                id = "experience-level",
                text = "How would you describe your experience with survey libraries?",
                isRequired = true,
                options = listOf(
                    QuestionOption("beginner", "Beginner - First time using survey libraries"),
                    QuestionOption("intermediate", "Intermediate - Some experience"),
                    QuestionOption("advanced", "Advanced - Extensive experience"),
                    QuestionOption("expert", "Expert - I build survey libraries myself")
                ),
                minSelections = 1,
                maxSelections = 1
            ).getOrElse { throw Exception("Failed to create experience question") },
            
            // Multiple choice question (multiple selections)
            MultipleChoiceQuestion(
                id = "features-used",
                text = "Which features are you most interested in?",
                description = "Select all that apply",
                isRequired = false,
                options = listOf(
                    QuestionOption("type-safety", "Type-safe question definitions"),
                    QuestionOption("functional", "Functional programming with Arrow-kt"),
                    QuestionOption("multiplatform", "Kotlin Multiplatform support"),
                    QuestionOption("ui-customization", "Customizable UI renderers"),
                    QuestionOption("validation", "Built-in validation"),
                    QuestionOption("navigation", "Smart navigation flow")
                ),
                minSelections = 0,
                maxSelections = 6
            ).getOrElse { throw Exception("Failed to create features question") },
            
            // Optional feedback
            FreeTextQuestion(
                id = "additional-feedback",
                text = "Any additional feedback or suggestions?",
                description = "Optional - share any thoughts or ideas",
                isRequired = false,
                config = TextConfig(
                    placeholder = "Your feedback helps us improve...",
                    maxLength = 500,
                    multiline = true
                )
            ).getOrElse { throw Exception("Failed to create feedback question") }
        )
    ).getOrElse { errors ->
        throw Exception("Failed to create survey: $errors")
    }
}

fun createSurveyEngine(): SurveyEngine {
    val config = surveySystem {
        source(InMemorySurveySource(surveys = emptyMap()))
        sink(InMemorySurveySink())
        remote(ConsoleDebugRemote())
        settings { copy(cacheEnabled = true, debugMode = true) }
    }.getOrElse { error ->
        throw Exception("Failed to create survey system: $error")
    }
    
    return SurveyEngine(config)
}

fun formatResponse(response: QuestionResponse): String {
    return when (response) {
        is BooleanResponse -> if (response.value) "Yes" else "No"
        is TextResponse -> response.value.ifEmpty { "(No response)" }
        is MultipleChoiceResponse -> response.selectedOptionIds.joinToString(", ")
        else -> response.toString()
    }
}