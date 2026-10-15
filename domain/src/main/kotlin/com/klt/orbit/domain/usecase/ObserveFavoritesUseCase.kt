package com.klt.orbit.domain.usecase

import com.klt.orbit.core.model.Video
import com.klt.orbit.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase
  @Inject
  constructor(
    private val favoritesRepository: FavoritesRepository,
  ) {
    operator fun invoke(): Flow<List<Video>> = favoritesRepository.observeFavorites()
  }
