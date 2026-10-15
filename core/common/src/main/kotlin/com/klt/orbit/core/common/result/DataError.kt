package com.klt.orbit.core.common.result

/** Why a data operation failed, in terms the UI can turn into a message and a Retry action. */
sealed interface DataError {
  /** No connection, DNS failure, timeout. Usually fixed by retrying once online. */
  data object Network : DataError

  /** The server answered with an unexpected HTTP status. */
  data class Http(
    val code: Int,
  ) : DataError

  /** The requested item does not exist (HTTP 404, or an empty lookup). */
  data object NotFound : DataError

  /** The video exists but has no MP4 rendition we can play. */
  data object NoPlayableMedia : DataError

  /** The response could not be understood. */
  data object InvalidData : DataError

  /** Local storage failed. */
  data object Storage : DataError

  data object Unknown : DataError
}

/**
 * Carries a [DataError] through APIs that can only fail with a Throwable, such as Paging's
 * `LoadState.Error`. Map it back with [toDataError].
 */
class DataErrorException(
  val error: DataError,
  cause: Throwable? = null,
) : Exception("Data error: $error", cause)

fun Throwable.toDataError(): DataError = (this as? DataErrorException)?.error ?: DataError.Unknown
