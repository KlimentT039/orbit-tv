package com.klt.orbit.core.network

import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/** Runs a network call, turning any failure into a typed [DataError]. Never swallows cancellation. */
internal suspend fun <T> networkCall(block: suspend () -> T): Outcome<T> =
  try {
    Outcome.Success(block())
  } catch (e: CancellationException) {
    throw e
  } catch (
    @Suppress("TooGenericExceptionCaught") e: Exception,
  ) {
    Outcome.Failure(e.toNetworkDataError())
  }

internal fun Throwable.toNetworkDataError(): DataError =
  when (this) {
    is IOException -> DataError.Network
    is HttpException -> if (code() == HTTP_NOT_FOUND) DataError.NotFound else DataError.Http(code())
    is SerializationException -> DataError.InvalidData
    else -> DataError.Unknown
  }

internal const val HTTP_NOT_FOUND = 404
internal const val HTTP_BAD_REQUEST = 400
