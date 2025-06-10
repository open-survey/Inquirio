package dev.nathanmkaya.inquirio.core.validation

import arrow.core.Either
import arrow.core.NonEmptyList
import dev.nathanmkaya.inquirio.core.error.ResponseValidationError

/**
 * Type alias for validation results that can accumulate multiple errors
 * CUPID: Composable error accumulation
 */
typealias ValidationResult<T> = Either<NonEmptyList<ResponseValidationError>, T>