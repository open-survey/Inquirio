package dev.nathanmkaya.inquirio.question.config

/**
 * Configuration for text questions
 */
data class TextConfig(
    val placeholder: String? = null,
    val maxLength: Int? = null,
    val multiline: Boolean = false,
    val inputType: TextInputType = TextInputType.PLAIN
)

/**
 * Text input types
 */
enum class TextInputType { 
    PLAIN, EMAIL, URL, PHONE 
}

/**
 * Numeric types for numeric questions
 */
enum class NumericType {
    INTEGER, FLOAT, DOUBLE
}

/**
 * Date types
 */
enum class DateType {
    DATE, TIME, DATETIME
}

/**
 * Question option for multiple/single choice questions
 */
data class QuestionOption(
    val id: String,
    val text: String,
    val value: Any? = null,
    val metadata: Map<String, Any> = emptyMap()
)