package com.klt.orbit.core.designsystem.error

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.klt.orbit.core.common.result.DataError
import com.klt.orbit.core.designsystem.R
import com.klt.orbit.core.designsystem.component.ErrorState
import com.klt.orbit.core.designsystem.icon.OrbitIcons

/** A user-facing explanation for [error], shared so every screen words failures the same way. */
@Composable
fun dataErrorMessage(error: DataError): String =
  if (error is DataError.Http) stringResource(error.messageRes(), error.code) else stringResource(error.messageRes())

/** Same as [dataErrorMessage], for places outside composition such as a Toast. */
fun dataErrorMessage(
  context: Context,
  error: DataError,
): String =
  if (error is DataError.Http) {
    context.getString(
      error.messageRes(),
      error.code,
    )
  } else {
    context.getString(error.messageRes())
  }

@StringRes
private fun DataError.messageRes(): Int =
  when (this) {
    DataError.Network -> R.string.designsystem_error_network
    is DataError.Http -> R.string.designsystem_error_http
    DataError.NotFound -> R.string.designsystem_error_not_found
    DataError.NoPlayableMedia -> R.string.designsystem_error_no_playable_media
    DataError.InvalidData -> R.string.designsystem_error_invalid_data
    DataError.Storage -> R.string.designsystem_error_storage
    DataError.Unknown -> R.string.designsystem_error_unknown
  }

fun dataErrorIcon(error: DataError): ImageVector =
  if (error ==
    DataError.Network
  ) {
    OrbitIcons.CloudOff
  } else {
    OrbitIcons.Error
  }

/** [ErrorState] for a [DataError], with the matching message and icon. */
@Composable
fun DataErrorState(
  error: DataError,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
  requestFocusOnShow: Boolean = true,
) {
  ErrorState(
    message = dataErrorMessage(error),
    icon = dataErrorIcon(error),
    onRetry = onRetry,
    modifier = modifier,
    requestFocusOnShow = requestFocusOnShow,
  )
}
