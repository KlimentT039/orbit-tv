package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.core.common.result.map
import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Adds or removes [Video] from favorites; succeeds with the new state (true = now a favorite). */
class ToggleFavoriteUseCase
  @Inject
  constructor(
    private val favoritesRepository: FavoritesRepository,
  ) {
    suspend operator fun invoke(video: Video): Outcome<Boolean> {
      val isFavorite = favoritesRepository.observeIsFavorite(video.id).first()
      return if (isFavorite) {
        favoritesRepository.removeFavorite(video.id).map { false }
      } else {
        favoritesRepository.addFavorite(video).map { true }
      }
    }
  }
