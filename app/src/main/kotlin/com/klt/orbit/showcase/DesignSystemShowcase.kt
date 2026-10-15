package com.klt.orbit.showcase

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.klt.orbit.R
import com.klt.orbit.core.designsystem.component.ContentRow
import com.klt.orbit.core.designsystem.component.ErrorState
import com.klt.orbit.core.designsystem.component.HeroBackdrop
import com.klt.orbit.core.designsystem.component.HeroBanner
import com.klt.orbit.core.designsystem.component.LoadingShimmer
import com.klt.orbit.core.designsystem.component.VideoCard
import com.klt.orbit.core.designsystem.focus.OrbitScrolling
import com.klt.orbit.core.designsystem.icon.OrbitIcons
import com.klt.orbit.core.designsystem.theme.OrbitTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Phase 1 placeholder: exercises every design-system component with sample data so focus, scale,
 * glow and typography can be checked on a TV emulator. Replaced by the real Home in Phase 3.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesignSystemShowcase(modifier: Modifier = Modifier) {
  val heroFocusRequester = remember { FocusRequester() }
  var focusedTitle by rememberSaveable { mutableStateOf<String?>(null) }

  Box(modifier = modifier.fillMaxSize().background(OrbitTheme.colors.background)) {
    HeroBackdrop(imageUrl = null, modifier = Modifier.fillMaxSize())
    CompositionLocalProvider(LocalBringIntoViewSpec provides OrbitScrolling.Page) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp),
      ) {
        item(key = "hero") {
          HeroBanner(
            title = focusedTitle ?: stringResource(R.string.showcase_hero_title),
            description = stringResource(R.string.showcase_hero_description),
            imageUrl = null,
            overline = stringResource(R.string.showcase_hero_overline),
            actionLabel = stringResource(R.string.showcase_play),
            onAction = {},
            actionModifier = Modifier.focusRequester(heroFocusRequester),
            showBackdrop = false,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        items(items = SampleRows, key = { it.title }) { row ->
          ContentRow(
            title = row.title,
            items = row.videos,
            key = { it.title },
          ) { video ->
            VideoCard(
              title = video.title,
              subtitle = video.subtitle,
              imageUrl = null,
              progress = video.progress,
              onClick = {},
              onFocus = { focusedTitle = video.title },
            )
          }
        }
        item(key = "loading") {
          LoadingShimmer(rowCount = 1)
        }
        item(key = "error") {
          ErrorState(
            message = stringResource(R.string.showcase_error_message),
            icon = OrbitIcons.CloudOff,
            onRetry = {},
            requestFocusOnShow = false,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }

  // First launch focuses the hero, as the real Home will.
  LaunchedEffect(heroFocusRequester) { heroFocusRequester.requestFocus() }
}

private const val SAMPLE_ROW_SIZE = 8

private data class SampleVideo(
  val title: String,
  val subtitle: String,
  val progress: Float? = null,
)

private data class SampleRow(
  val title: String,
  val videos: ImmutableList<SampleVideo>,
)

private val SampleRows: ImmutableList<SampleRow> =
  persistentListOf(
    SampleRow(
      title = "Continue Watching",
      videos =
        persistentListOf(
          SampleVideo("Hubble: 30 Years of Discovery", "Goddard", 0.35f),
          SampleVideo("Perseverance Landing", "JPL", 0.7f),
          SampleVideo("ISS Spacewalk 84", "Johnson", 0.15f),
        ),
    ),
    sampleRow("Apollo"),
    sampleRow("Mars"),
    sampleRow("Hubble"),
    sampleRow("International Space Station"),
    sampleRow("Earth"),
  )

private fun sampleRow(topic: String) =
  SampleRow(
    title = topic,
    videos = List(SAMPLE_ROW_SIZE) { SampleVideo("$topic video ${it + 1}", "NASA") }.toImmutableList(),
  )
