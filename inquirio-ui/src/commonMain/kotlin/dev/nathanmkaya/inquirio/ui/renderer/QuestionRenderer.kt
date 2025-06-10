/**
 * # Inquirio Compose UI - Question Renderer System
 * 
 * A flexible, type-safe system for rendering survey questions using Jetpack Compose.
 * The renderer system uses a registry pattern that allows complete customization
 * and extension of question UI components.
 * 
 * ## Key Features
 * - **Type Safety**: Uses Kotlin reflection for type-safe question to renderer mapping
 * - **Extensible**: Users can easily override default renderers or add custom ones
 * - **Composable**: Built entirely with Jetpack Compose for modern UI
 * - **Functional**: Integrates seamlessly with the functional core library
 * 
 * ## Example Usage
 * ```kotlin
 * val customRenderers = defaultQuestionRenderers() + mapOf(
 *     BooleanQuestion::class to { question, response, onResponse ->
 *         MyCustomBooleanRenderer(question as BooleanQuestion, response, onResponse)
 *     }
 * )
 * 
 * val registry = QuestionRendererRegistry(customRenderers)
 * SurveyScreen(flow = flow, rendererRegistry = registry)
 * ```
 * 
 * @since 1.0.0
 */
package dev.nathanmkaya.inquirio.ui.renderer

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import dev.nathanmkaya.inquirio.question.model.Question
import dev.nathanmkaya.inquirio.response.model.QuestionResponse
import kotlin.reflect.KClass

/**
 * Type alias for question renderer functions.
 * 
 * A QuestionRenderer is a Composable function that:
 * - Takes a [Question] of any type
 * - Takes the current [QuestionResponse] (nullable)
 * - Provides a callback for when the user provides a response
 * - Renders the appropriate UI for that question type
 * 
 * @param question The question to render
 * @param response The current response (null if no response yet)
 * @param onResponse Callback when user provides/changes their response
 */
typealias QuestionRenderer = @Composable (
    question: Question, 
    response: QuestionResponse?, 
    onResponse: (QuestionResponse) -> Unit
) -> Unit

/**
 * Registry that maps question types to their corresponding UI renderers.
 * 
 * The registry uses Kotlin reflection to provide type-safe mapping between
 * question classes and their renderer functions. This allows for complete
 * customization of the UI while maintaining type safety.
 * 
 * ## Features
 * - **Type-Safe Mapping**: Uses KClass reflection for accurate type matching
 * - **Fallback Support**: Provides sensible defaults for unregistered types
 * - **Immutable**: Registry is immutable once created, promoting functional design
 * - **Extensible**: Easy to create new registries with custom renderers
 * 
 * ## Example Usage
 * ```kotlin
 * // Create custom renderers
 * val myRenderers = mapOf(
 *     BooleanQuestion::class to { q, r, onR -> 
 *         MyBooleanRenderer(q as BooleanQuestion, r, onR) 
 *     },
 *     FreeTextQuestion::class to { q, r, onR -> 
 *         MyTextRenderer(q as FreeTextQuestion, r, onR) 
 *     }
 * )
 * 
 * val registry = QuestionRendererRegistry(myRenderers)
 * 
 * // Use in Compose
 * registry.Render(question = myQuestion, response = currentResponse) { newResponse ->
 *     // Handle response
 * }
 * ```
 * 
 * @param renderers Map of question types to their renderer functions
 * @since 1.0.0
 */
data class QuestionRendererRegistry(
    private val renderers: Map<KClass<out Question>, QuestionRenderer>
) {
    
    /**
     * Render a question using the appropriate registered renderer.
     * 
     * Looks up the renderer for the given question type and invokes it.
     * If no renderer is found, displays a fallback message indicating
     * the unsupported question type.
     * 
     * @param question The question to render
     * @param response The current response (null if no response provided yet)
     * @param onResponse Callback invoked when the user provides or changes their response
     */
    @Composable
    fun Render(
        question: Question, 
        response: QuestionResponse?, 
        onResponse: (QuestionResponse) -> Unit
    ) {
        val renderer = renderers[question::class]
        if (renderer != null) {
            renderer(question, response, onResponse)
        } else {
            // Fallback for unregistered question types
            UnsupportedQuestionRenderer(question)
        }
    }
    
    /**
     * Create a new registry with additional or overridden renderers.
     * 
     * This is useful for extending the default registry with custom renderers
     * or for overriding specific question types with custom implementations.
     * 
     * @param additionalRenderers Map of additional or override renderers
     * @return A new registry with the combined renderers
     */
    fun withAdditionalRenderers(
        additionalRenderers: Map<KClass<out Question>, QuestionRenderer>
    ): QuestionRendererRegistry {
        return QuestionRendererRegistry(renderers + additionalRenderers)
    }
    
    /**
     * Check if a renderer is registered for the given question type.
     * 
     * @param questionClass The question class to check
     * @return true if a renderer is registered, false otherwise
     */
    fun hasRenderer(questionClass: KClass<out Question>): Boolean {
        return renderers.containsKey(questionClass)
    }
}

/**
 * Default fallback renderer for unsupported question types.
 * 
 * Displays a message indicating that the question type is not supported
 * by the current renderer registry. This helps developers identify
 * missing renderers during development.
 * 
 * @param question The unsupported question
 */
@Composable
private fun UnsupportedQuestionRenderer(question: Question) {
    Text(
        text = "Unsupported question type: ${question::class.simpleName}\n" +
                "Question: ${question.text}",
        color = androidx.compose.material3.MaterialTheme.colorScheme.error
    )
}