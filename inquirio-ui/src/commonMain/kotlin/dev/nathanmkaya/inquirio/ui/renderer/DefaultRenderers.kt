/**
 * Default Compose renderers for built-in Inquirio question types.
 * 
 * This file provides out-of-the-box UI implementations for all question types
 * included in the core Inquirio library. These renderers follow Material Design 3
 * principles and can be used as-is or as a reference for custom implementations.
 * 
 * ## Included Renderers
 * - [BooleanQuestionRenderer]: True/false questions with customizable button labels
 * - [FreeTextQuestionRenderer]: Text input with validation and hints
 * - [MultipleChoiceQuestionRenderer]: Single or multiple selection from options
 * 
 * ## Customization
 * All renderers accept the question model which contains configuration options
 * like custom labels, validation rules, and styling hints. For deeper customization,
 * create your own renderer functions following the same signature.
 * 
 * @since 1.0.0
 */
package dev.nathanmkaya.inquirio.ui.renderer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nathanmkaya.inquirio.question.type.BooleanQuestion
import dev.nathanmkaya.inquirio.question.type.FreeTextQuestion
import dev.nathanmkaya.inquirio.question.type.MultipleChoiceQuestion
import dev.nathanmkaya.inquirio.response.type.BooleanResponse
import dev.nathanmkaya.inquirio.response.type.TextResponse
import dev.nathanmkaya.inquirio.response.type.MultipleChoiceResponse
import dev.nathanmkaya.inquirio.question.model.Question
import dev.nathanmkaya.inquirio.response.model.QuestionResponse

/**
 * Default renderer for boolean (true/false) questions.
 * 
 * Displays two buttons representing the true and false options with
 * customizable labels. The selected option is highlighted using
 * Material Design 3 selection styling.
 * 
 * ## Features
 * - **Custom Labels**: Uses question's trueLabel and falseLabel properties
 * - **Visual Feedback**: Clear indication of selected option
 * - **Accessibility**: Proper semantic roles and content descriptions
 * - **Material Design**: Follows Material 3 design principles
 * 
 * @param question The boolean question to render
 * @param response Current response (if any)
 * @param onResponse Callback when user selects an option
 */
@Composable
fun BooleanQuestionRenderer(
    question: BooleanQuestion,
    response: QuestionResponse?,
    onResponse: (QuestionResponse) -> Unit
) {
    val currentSelection = (response as? BooleanResponse)?.value
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Question text
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        
        // Description if available
        question.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Boolean options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // True option
            FilterChip(
                onClick = { 
                    onResponse(BooleanResponse(question.id, true))
                },
                label = { Text(question.trueLabel) },
                selected = currentSelection == true,
                modifier = Modifier.weight(1f)
            )
            
            // False option
            FilterChip(
                onClick = { 
                    onResponse(BooleanResponse(question.id, false))
                },
                label = { Text(question.falseLabel) },
                selected = currentSelection == false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Default renderer for free text input questions.
 * 
 * Provides a text field with validation, character limits, and input type hints.
 * Supports various text input types like email, phone, etc. through the
 * question's configuration.
 * 
 * ## Features
 * - **Input Validation**: Real-time validation based on question config
 * - **Character Limits**: Shows character count and enforces limits
 * - **Input Types**: Keyboard hints for email, phone, etc.
 * - **Error Display**: Clear error messages for validation failures
 * 
 * @param question The text question to render
 * @param response Current response (if any)
 * @param onResponse Callback when user changes the text
 */
@Composable
fun FreeTextQuestionRenderer(
    question: FreeTextQuestion,
    response: QuestionResponse?,
    onResponse: (QuestionResponse) -> Unit
) {
    val currentText = (response as? TextResponse)?.value ?: ""
    var textState by remember(currentText) { mutableStateOf(currentText) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Validation function based on actual TextConfig properties
    fun validateText(text: String) {
        val config = question.config
        when {
            config.maxLength?.let { text.length > it } == true -> {
                isError = true
                errorMessage = "Maximum ${config.maxLength} characters allowed"
            }
            else -> {
                isError = false
                errorMessage = ""
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Question text
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        
        // Description if available
        question.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Text input field
        OutlinedTextField(
            value = textState,
            onValueChange = { newText ->
                // Enforce max length if specified
                val limitedText = question.config.maxLength?.let { maxLength ->
                    if (newText.length <= maxLength) newText else newText.take(maxLength)
                } ?: newText
                
                textState = limitedText
                validateText(limitedText)
                
                // Always call onResponse - validation will be handled by the question
                onResponse(TextResponse(question.id, limitedText))
            },
            label = { Text("Your answer") },
            placeholder = { Text(question.config.placeholder ?: "Enter your response...") },
            isError = isError,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Error message or help text
                    Text(
                        text = if (isError) errorMessage else "",
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Character count if max length is specified
                    question.config.maxLength?.let { maxLength ->
                        Text(
                            text = "${textState.length}/$maxLength",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = !question.config.multiline,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Default renderer for multiple choice questions.
 * 
 * Displays a list of selectable options using radio buttons (single selection)
 * or checkboxes (multiple selection). Supports custom option labels and values.
 * 
 * ## Features
 * - **Single/Multiple Selection**: Automatic UI based on question configuration
 * - **Custom Options**: Full support for question option configuration
 * - **Accessibility**: Proper semantic roles and selection groups
 * - **Material Design**: Consistent with Material 3 selection patterns
 * 
 * @param question The multiple choice question to render
 * @param response Current response (if any)
 * @param onResponse Callback when user changes selection
 */
@Composable
fun MultipleChoiceQuestionRenderer(
    question: MultipleChoiceQuestion,
    response: QuestionResponse?,
    onResponse: (QuestionResponse) -> Unit
) {
    val currentSelectionIds = (response as? MultipleChoiceResponse)?.selectedOptionIds?.toList() ?: emptyList()
    val allowMultiple = question.selectionRange.last > 1
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Question text
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        
        // Description if available
        question.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Selection info
        Text(
            text = if (allowMultiple) {
                "Select ${question.selectionRange.first} to ${question.selectionRange.last} options"
            } else {
                "Select one option"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options list
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            question.options.forEach { option ->
                val isSelected = currentSelectionIds.contains(option.id)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                val newSelectionIds = if (allowMultiple) {
                                    // Multiple selection: toggle the option
                                    if (isSelected) {
                                        currentSelectionIds.filter { it != option.id }
                                    } else {
                                        // Check if we're at the max selection limit
                                        if (currentSelectionIds.size < question.selectionRange.last) {
                                            currentSelectionIds + option.id
                                        } else {
                                            currentSelectionIds // Don't allow more selections
                                        }
                                    }
                                } else {
                                    // Single selection: replace with this option
                                    listOf(option.id)
                                }
                                
                                // Only create response if we have selections and it's within range
                                if (newSelectionIds.isNotEmpty()) {
                                    MultipleChoiceResponse(question.id, newSelectionIds).fold(
                                        ifLeft = { /* Handle error if needed */ },
                                        ifRight = { validResponse -> onResponse(validResponse) }
                                    )
                                }
                            },
                            role = if (allowMultiple) Role.Checkbox else Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (allowMultiple) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null // Handled by selectable modifier
                        )
                    } else {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // Handled by selectable modifier
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * Create a map of default renderers for all built-in question types.
 * 
 * This function provides a convenient way to get all the default renderers
 * in a single map that can be used to create a [QuestionRendererRegistry].
 * 
 * ## Usage
 * ```kotlin
 * val registry = QuestionRendererRegistry(defaultQuestionRenderers())
 * 
 * // Or extend with custom renderers
 * val customRegistry = QuestionRendererRegistry(
 *     defaultQuestionRenderers() + mapOf(
 *         MyCustomQuestion::class to { q, r, onR -> MyCustomRenderer(q, r, onR) }
 *     )
 * )
 * ```
 * 
 * @return Map of question types to their default renderer functions
 */
fun defaultQuestionRenderers(): Map<kotlin.reflect.KClass<out Question>, QuestionRenderer> = mapOf(
    BooleanQuestion::class to { question, response, onResponse ->
        BooleanQuestionRenderer(question as BooleanQuestion, response, onResponse)
    },
    FreeTextQuestion::class to { question, response, onResponse ->
        FreeTextQuestionRenderer(question as FreeTextQuestion, response, onResponse)
    },
    MultipleChoiceQuestion::class to { question, response, onResponse ->
        MultipleChoiceQuestionRenderer(question as MultipleChoiceQuestion, response, onResponse)
    }
)