package com.klt.orbit.core.network.repository

import com.klt.orbit.core.model.Video
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recently listed videos by id (LRU). Lets Details open instantly for anything already shown on
 * Home, in a row or in search results, without another round trip.
 */
@Singleton
internal class VideoMemoryCache
  @Inject
  constructor() {
    private val videos =
      object : LinkedHashMap<String, Video>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Video>?): Boolean = size > MAX_ENTRIES
      }

    @Synchronized
    fun get(id: String): Video? = videos[id]

    @Synchronized
    fun putAll(items: List<Video>) {
      items.forEach { videos[it.id] = it }
    }

    private companion object {
      const val MAX_ENTRIES = 500
      const val INITIAL_CAPACITY = 64
      const val LOAD_FACTOR = 0.75f
    }
  }
