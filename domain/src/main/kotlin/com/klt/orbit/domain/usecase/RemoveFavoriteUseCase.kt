package com.klt.orbit.domain.usecase

import com.klt.orbit.core.common.result.Outcome
import com.klt.orbit.domain.repository.FavoritesRepository
import javax.inject.Inject

class RemoveFavoriteUseCase
  @Inject
  constructor(
    private val favoritesRepository: FavoritesRepository,
  ) {
    suspend operator fun invoke(videoId: String): Outcome<Unit> = favoritesRepository.removeFavorite(videoId)
  }
