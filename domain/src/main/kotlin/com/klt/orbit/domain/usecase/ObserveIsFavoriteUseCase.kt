package com.klt.orbit.domain.usecase

import com.klt.orbit.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveIsFavoriteUseCase
  @Inject
  constructor(
    private val favoritesRepository: FavoritesRepository,
  ) {
    operator fun invoke(videoId: String): Flow<Boolean> = favoritesRepository.observeIsFavorite(videoId)
  }
