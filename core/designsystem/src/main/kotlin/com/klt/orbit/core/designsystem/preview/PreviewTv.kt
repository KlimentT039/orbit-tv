package com.klt.orbit.core.designsystem.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/** Previews a composable on a 1080p TV canvas over the app's background color. */
@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF04060C)
annotation class PreviewTv
