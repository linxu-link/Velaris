/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujia.foundation.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class AudioMediaItemTest {
    @Test
    fun requireUniqueAudioIds_acceptsUniqueIds() {
        listOf(
            AudioMediaItem(id = "wind", uri = "file://wind.mp3"),
            AudioMediaItem(id = "rain", uri = "file://rain.mp3"),
        ).requireUniqueAudioIds()
    }

    @Test
    fun requireUniqueAudioIds_rejectsDuplicateIds() {
        assertThrows(IllegalArgumentException::class.java) {
            listOf(
                AudioMediaItem(id = "wind", uri = "file://wind.mp3"),
                AudioMediaItem(id = "wind", uri = "file://other-wind.mp3"),
            ).requireUniqueAudioIds()
        }
    }

    @Test
    fun coercePlayerVolume_clampsToPlayerRange() {
        assertEquals(0f, (-1f).coercePlayerVolume())
        assertEquals(0.5f, 0.5f.coercePlayerVolume())
        assertEquals(1f, 2f.coercePlayerVolume())
    }

    @Test
    fun audioMediaItem_usesNonLoopingDefaults() {
        val item = AudioMediaItem(id = "wind", uri = "file://wind.mp3")

        assertFalse(item.loop)
        assertFalse(item.stopAllOnEnd)
    }
}
