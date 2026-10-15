package com.klt.orbit.core.common.result

/** Result of an operation that fails with a typed [DataError] instead of throwing. */
sealed interface Outcome<out T> {
  data class Success<out T>(
    val value: T,
  ) : Outcome<T>

  data class Failure(
    val error: DataError,
  ) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> =
  when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
  }

fun <T> Outcome<T>.getOrNull(): T? = (this as? Outcome.Success)?.value

fun <T> Outcome<T>.errorOrNull(): DataError? = (this as? Outcome.Failure)?.error
