/**
 * Main survey screen composable for displaying and managing survey flows.
 * 
 * The SurveyScreen is the primary UI component that orchestrates the entire
 * survey experience. It manages question rendering, navigation, validation,
 * and completion handling while providing a smooth, accessible user experience.
 * 
 * @since 1.0.0
 */
package dev.nathanmkaya.inquirio.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.nathanmkaya.inquirio.core.error.NavigationError
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError
import dev.nathanmkaya.inquirio.core.id.QuestionId
import dev.nathanmkaya.inquirio.response.model.QuestionResponse
import dev.nathanmkaya.inquirio.survey.flow.SurveyFlow
import dev.nathanmkaya.inquirio.ui.renderer.QuestionRendererRegistry
import dev.nathanmkaya.inquirio.ui.renderer.defaultQuestionRenderers
import kotlinx.coroutines.launch

/**
 * Main survey screen that manages the complete survey experience.
 * 
 * @param flow The survey flow managing questions and responses
 * @param rendererRegistry Registry containing renderers for each question type
 * @param onSurveyComplete Callback invoked when the survey is successfully completed
 * @param onError Optional callback for handling errors (errors are also displayed inline)
 * @param modifier Optional modifier for customizing the screen layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    rendererRegistry: QuestionRendererRegistry = QuestionRendererRegistry(defaultQuestionRenderers()),
    onSurveyComplete: (Map<QuestionId, QuestionResponse>) -> Unit,
    onError: ((Throwable) -> Unit)? = null,
    modifier: Modifier = Modifier,
    flow: SurveyFlow
) {

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State management
    var currentQuestion by remember { mutableStateOf(flow.currentQuestion) }
    var currentResponses by remember { mutableStateOf(flow.currentResponses) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Update state when flow changes
    LaunchedEffect(flow.currentQuestion, flow.currentResponses) {
        currentQuestion = flow.currentQuestion
        currentResponses = flow.currentResponses
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Survey content
        currentQuestion.fold(
            ifEmpty = {
                // Survey completed
                SurveyCompletedContent(
                    onComplete = { 
                        onSurveyComplete(currentResponses)
                    }
                )
            },
            ifSome = { question ->
                // Active survey content
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Error display
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    
                    // Question content
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // Required indicator
                            if (question.isRequired) {
                                Text(
                                    text = "Required *",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            // Render the question using the registry
                            rendererRegistry.Render(
                                question = question,
                                response = currentResponses[question.id],
                                onResponse = { response ->
                                    scope.launch {
                                        try {
                                            flow.addResponse(response).fold(
                                                ifLeft = { errors ->
                                                    errorMessage = formatValidationErrors(errors)
                                                },
                                                ifRight = {
                                                    errorMessage = null
                                                    currentResponses = flow.currentResponses
                                                }
                                            )
                                        } catch (e: Exception) {
                                            errorMessage = "Error adding response: ${e.message}"
                                            onError?.invoke(e)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    // Navigation controls
                    SurveyNavigationControls(
                        isLoading = isLoading,
                        onPrevious = {
                            scope.launch {
                                isLoading = true
                                try {
                                    flow.previous().fold(
                                        ifLeft = { error ->
                                            errorMessage = formatNavigationError(error)
                                        },
                                        ifRight = {
                                            errorMessage = null
                                            currentQuestion = flow.currentQuestion
                                        }
                                    )
                                } catch (e: Exception) {
                                    errorMessage = "Navigation error: ${e.message}"
                                    onError?.invoke(e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onNext = {
                            scope.launch {
                                isLoading = true
                                try {
                                    flow.next().fold(
                                        ifLeft = { error ->
                                            if (error == NavigationError.NoNextQuestion) {
                                                // Survey is complete
                                                currentQuestion = flow.currentQuestion
                                            } else {
                                                errorMessage = formatNavigationError(error)
                                            }
                                        },
                                        ifRight = {
                                            errorMessage = null
                                            currentQuestion = flow.currentQuestion
                                        }
                                    )
                                } catch (e: Exception) {
                                    errorMessage = "Navigation error: ${e.message}"
                                    onError?.invoke(e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}

/**
 * Navigation controls for moving between questions.
 */
@Composable
private fun SurveyNavigationControls(
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Previous button
        OutlinedButton(
            onClick = onPrevious,
            enabled = !isLoading
        ) {
            Text("Previous")
        }
        
        // Next button
        Button(
            onClick = onNext,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Next")
            }
        }
    }
}

/**
 * Content displayed when the survey is completed.
 */
@Composable
private fun SurveyCompletedContent(
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Survey Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Thank you for your responses. Your feedback is valuable to us.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(
            onClick = onComplete,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Submit Survey")
        }
    }
}

private fun formatValidationErrors(errors: arrow.core.NonEmptyList<ResponseValidationError>): String {
    return errors.joinToString("\n") { error ->
        when (error) {
            is ResponseValidationError.RequiredFieldMissing -> "This field is required"
            is ResponseValidationError.TextTooLong -> "Text is too long (max ${error.maxLength} characters)"
            is ResponseValidationError.ValueOutOfRange -> "Value must be within ${error.range}"
            is ResponseValidationError.InvalidResponseType -> "Invalid response type"
            is ResponseValidationError.InvalidSelection -> error.reason
            is ResponseValidationError.InvalidFileType -> "Invalid file type. Allowed: ${error.allowed.joinToString()}"
        }
    }
}

private fun formatNavigationError(error: NavigationError): String {
    return when (error) {
        is NavigationError.NoNextQuestion -> "No next question available"
        is NavigationError.NoPreviousQuestion -> "No previous question available"
        is NavigationError.NavigationBlocked -> error.reason
    }
}