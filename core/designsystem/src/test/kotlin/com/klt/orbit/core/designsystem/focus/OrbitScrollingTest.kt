package com.klt.orbit.core.designsystem.focus

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OrbitScrollingTest {
  private val container = 1000f

  @Test
  fun `page does not scroll when the focused item is comfortably visible`() {
    assertThat(pageScrollDistance(offset = 500f, size = 100f, containerSize = container)).isEqualTo(0f)
  }

  @Test
  fun `page moves an item near the bottom edge up to the pivot`() {
    assertThat(pageScrollDistance(offset = 850f, size = 150f, containerSize = container)).isEqualTo(550f)
  }

  @Test
  fun `page moves an item above the safe area down to the pivot`() {
    assertThat(pageScrollDistance(offset = -200f, size = 150f, containerSize = container)).isEqualTo(-500f)
  }

  @Test
  fun `page shows the start of an item taller than the comfortable band`() {
    assertThat(pageScrollDistance(offset = 300f, size = 900f, containerSize = container)).isEqualTo(250f)
  }

  @Test
  fun `row does not scroll when the card is inside the safe area`() {
    assertThat(rowScrollDistance(offset = 50f, size = 200f, containerSize = container)).isEqualTo(0f)
  }

  @Test
  fun `row scrolls the minimum to keep a card at the end inside the safe area`() {
    assertThat(rowScrollDistance(offset = 900f, size = 200f, containerSize = container)).isEqualTo(150f)
  }

  @Test
  fun `row scrolls back so a card at the start lines up with the safe area`() {
    assertThat(rowScrollDistance(offset = -100f, size = 200f, containerSize = container)).isEqualTo(-150f)
  }
}
