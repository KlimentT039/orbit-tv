package com.klt.orbit.core.database

import android.database.SQLException
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.common.result.Outcome
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/** Runs a local write, turning storage failures into [DataError.Storage]. */
internal suspend fun localWrite(block: suspend () -> Unit): Outcome<Unit> =
  try {
    block()
    Outcome.Success(Unit)
  } catch (e: CancellationException) {
    throw e
  } catch (_: SQLException) {
    Outcome.Failure(DataError.Storage)
  } catch (_: IOException) {
    Outcome.Failure(DataError.Storage)
  } catch (_: IllegalStateException) {
    // Room throws this for a closed or corrupted database.
    Outcome.Failure(DataError.Storage)
  }

/** A local read that falls back to [fallback] instead of crashing the collector on storage errors. */
internal fun <T> Flow<T>.orOnStorageError(fallback: T): Flow<T> =
  catch { error ->
    if (error is SQLException || error is IOException || error is IllegalStateException) emit(fallback) else throw error
  }
