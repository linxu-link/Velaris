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
package com.wujia.foundation.data

import com.wujia.foundation.data.noise.defaultLocalNoises
import com.wujia.foundation.data.particle.defaultLocalParticles
import com.wujia.foundation.data.scene.defaultLocalScenes
import com.wujia.foundation.model.R
import com.wujia.foundation.model.noise.NoiseCategory
import com.wujia.foundation.model.particle.ParticleCategory
import com.wujia.foundation.model.particle.ParticleEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneSeedDataTest {
    @Test
    fun defaultLocalScenes_returnsModelBackedScenes() {
        val scenes = defaultLocalScenes()

        assertEquals(10, scenes.size)
        assertTrue(scenes.map { it.id }.distinct().size == scenes.size)
    }

    @Test
    fun firstScene_containsWindChimeAudioTrack() {
        val firstScene = defaultLocalScenes().first()
        val track = firstScene.audioTracks.firstOrNull { it.id == "wind-chime" }

        assertNotNull(track)
        assertEquals(R.raw.wind_chime_1, track?.rawResId)
        assertTrue(track?.loop == true)
    }
}

class NoiseResourceLocalDataSourceTest {
    @Test
    fun defaultLocalNoises_returnsUniqueNoiseResources() {
        val noises = defaultLocalNoises()

        assertEquals(16, noises.size)
        assertTrue(noises.map { it.id }.distinct().size == noises.size)
        assertEquals(R.raw.rain_1, noises.first { it.id == "rain" }.rawResId)
    }

    @Test
    fun defaultLocalNoises_canBeFilteredByCategory() {
        val noises = defaultLocalNoises()
        val focusNoises = noises.filter { it.category == NoiseCategory.FOCUS }

        assertEquals(listOf("insects", "page-turn", "typing", "writing"), focusNoises.map { it.id })
        assertEquals(16, noises.size)
    }
}

class ParticleResourceLocalDataSourceTest {
    @Test
    fun defaultLocalParticles_returnsUniqueParticleResources() {
        val particles = defaultLocalParticles()

        assertEquals(7, particles.size)
        assertTrue(particles.map { it.id }.distinct().size == particles.size)
    }

    @Test
    fun defaultLocalParticles_containsRainEffects() {
        val particles = defaultLocalParticles()
        val rainParticles = particles.filter { it.effect == ParticleEffect.RAIN }

        assertEquals(3, rainParticles.size)
        assertTrue(rainParticles.any { it.id == "light-rain" })
        assertTrue(rainParticles.any { it.id == "moderate-rain" })
        assertTrue(rainParticles.any { it.id == "heavy-rain" })
    }

    @Test
    fun defaultLocalParticles_containsSnowEffects() {
        val particles = defaultLocalParticles()
        val snowParticles = particles.filter { it.effect == ParticleEffect.SNOW }

        assertEquals(3, snowParticles.size)
        assertTrue(snowParticles.any { it.id == "light-snow" })
        assertTrue(snowParticles.any { it.id == "moderate-snow" })
        assertTrue(snowParticles.any { it.id == "blizzard" })
    }

    @Test
    fun defaultLocalParticles_canBeFilteredByCategory() {
        val particles = defaultLocalParticles()
        val stormParticles = particles.filter { it.category == ParticleCategory.STORM }

        assertEquals(2, stormParticles.size)
        assertTrue(stormParticles.any { it.id == "heavy-rain" })
        assertTrue(stormParticles.any { it.id == "blizzard" })
    }

    @Test
    fun defaultLocalParticles_containsFireflies() {
        val particles = defaultLocalParticles()
        val fireflies = particles.find { it.effect == ParticleEffect.FIREFLIES }

        assertNotNull(fireflies)
        assertEquals("fireflies", fireflies?.id)
    }
}
