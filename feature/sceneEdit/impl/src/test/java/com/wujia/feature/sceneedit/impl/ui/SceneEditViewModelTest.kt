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
package com.wujia.feature.sceneedit.impl.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.wujia.feature.sceneedit.impl.ui.viewmodel.EditEvent
import com.wujia.feature.sceneedit.impl.ui.viewmodel.MaterialPreset
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SceneEditStep
import com.wujia.feature.sceneedit.impl.ui.viewmodel.SceneEditViewModel
import com.wujia.foundation.domain.background.GetBackgroundResourcesUseCase
import com.wujia.foundation.domain.noise.GetNoiseResourcesUseCase
import com.wujia.foundation.domain.particle.GetParticleResourcesUseCase
import com.wujia.foundation.domain.scene.GetEditableSceneUseCase
import com.wujia.foundation.domain.scene.SaveSceneEditUseCase
import com.wujia.foundation.domain.video.GetVideoResourcesUseCase
import com.wujia.foundation.model.ProjectsIds
import com.wujia.foundation.model.background.BackgroundResource
import com.wujia.foundation.model.background.BackgroundResourceRepository
import com.wujia.foundation.model.noise.NoiseCategory
import com.wujia.foundation.model.noise.NoiseResource
import com.wujia.foundation.model.noise.NoiseResourceRepository
import com.wujia.foundation.model.particle.ParticleCategory
import com.wujia.foundation.model.particle.ParticleResource
import com.wujia.foundation.model.particle.ParticleResourceRepository
import com.wujia.foundation.model.scene.EditableScene
import com.wujia.foundation.model.scene.SceneCategory
import com.wujia.foundation.model.scene.SceneControlSettings
import com.wujia.foundation.model.scene.SceneEditAudio
import com.wujia.foundation.model.video.VideoResource
import com.wujia.foundation.model.video.VideoResourceRepository
import com.wujia.foundation.testing.FakeSceneResourceRepository
import com.wujia.foundation.testing.MainDispatcherRule
import com.wujia.foundation.ui.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SceneEditViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val defaultSceneTitle: String
        get() = runCatching {
            ApplicationProvider.getApplicationContext<Context>()
                .getString(com.wujia.feature.sceneedit.impl.R.string.scene_edit_custom_scene)
        }.getOrDefault("\u81ea\u5b9a\u4e49\u573a\u666f")

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Test
    fun initializeNewScene_hasNoSelectedMaterialOrSound() = runTest(testDispatcher) {
        val viewModel = viewModel()

        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.SLEEP)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(SceneCategory.SLEEP, state.category)
        assertEquals(SceneEditStep.Material, state.currentStep)
        assertEquals(listOf("image", "video"), state.materialState.presets.map { it.typeLabel }.distinct())
        assertNull(state.selectedMaterial)
        assertNull(state.soundState.selectedSound)
        assertEquals(defaultSceneTitle, state.title)
        assertEquals("", state.description)
    }

    @Test
    fun initializeExistingScene_loadsEditableScene() = runTest(testDispatcher) {
        val repository = FakeSceneResourceRepository(
            editableScene = EditableScene(
                id = "scene-1",
                title = "Existing",
                description = "Stored",
                category = SceneCategory.SLEEP,
                backgroundResName = ProjectsIds.Background.UNDER_MOON,
                backgroundUri = null,
                videoUri = null,
                audioTracks = listOf(SceneEditAudio(id = ProjectsIds.Noise.RAIN, title = "雨声", uri = "audio://rain")),
                controlSettings = SceneControlSettings(brightness = 0.3f),
            ),
        )
        val viewModel = viewModel(repository)

        viewModel.setSceneParameters(sceneId = "scene-1", category = SceneCategory.FOCUS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("scene-1", state.sceneId)
        assertEquals("Existing", state.title)
        assertEquals("Stored", state.description)
        assertEquals(SceneCategory.SLEEP, state.category)
        assertEquals(listOf(ProjectsIds.Noise.RAIN), state.soundState.selectedSoundIds)
        assertEquals(0.3f, state.controlSettings.brightness)
    }

    @Test
    fun nextClick_requiresValidMaterial() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onNextClick()

        val state = viewModel.uiState.value
        assertEquals(SceneEditStep.Material, state.currentStep)
        assertTrue(viewModel.events.first() is EditEvent.ShowToast)
    }

    @Test
    fun nextClick_requiresValidSound() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onMaterialSelected(morningMistMaterial(viewModel))
        viewModel.onNextClick()
        viewModel.onNextClick()

        val state = viewModel.uiState.value
        assertEquals(SceneEditStep.Sound, state.currentStep)
        assertTrue(viewModel.events.first() is EditEvent.ShowToast)
    }

    @Test
    fun save_requiresTitle() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onMaterialSelected(morningMistMaterial(viewModel))
        viewModel.onNextClick()
        viewModel.onSoundSelected(ProjectsIds.Noise.RAIN)
        viewModel.onNextClick()
        viewModel.onTitleChanged("")
        viewModel.onNextClick() // navigate to Preview
        viewModel.onNextClick() // trigger save on Preview

        val state = viewModel.uiState.value
        assertEquals(SceneEditStep.Preview, state.currentStep)
        assertTrue(viewModel.events.first() is EditEvent.ShowToast)
    }

    @Test
    fun navigateToPreview_doesNotAutoSave() = runTest(testDispatcher) {
        val repository = FakeSceneResourceRepository(savedId = "saved-1")
        val viewModel = viewModel(repository)
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onMaterialSelected(morningMistMaterial(viewModel))
        viewModel.onNextClick()
        viewModel.onSoundSelected(ProjectsIds.Noise.RAIN)
        viewModel.onNextClick()
        viewModel.onNextClick() // navigate to Preview only

        assertNull(repository.savedInput)
        assertEquals(SceneEditStep.Preview, viewModel.uiState.value.currentStep)
    }

    @Test
    fun newSceneSave_savesDirectly() = runTest(testDispatcher) {
        val repository = FakeSceneResourceRepository(savedId = "saved-1")
        val viewModel = viewModel(repository)
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onMaterialSelected(morningMistMaterial(viewModel))
        viewModel.onNextClick()
        viewModel.onSoundSelected(ProjectsIds.Noise.RAIN)
        viewModel.onNextClick()
        viewModel.onTitleChanged("Custom")
        viewModel.onDescriptionChanged("   ")
        viewModel.onNextClick()
        viewModel.onNextClick()
        advanceUntilIdle()

        assertNotNull(repository.savedInput)
    }

    @Test
    fun newSceneSave_savesWithExpectedInputAndEmitsSuccessEvent() = runTest(testDispatcher) {
        val repository = FakeSceneResourceRepository(savedId = "saved-1")
        val viewModel = viewModel(repository)
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onMaterialSelected(morningMistMaterial(viewModel))
        viewModel.onNextClick()
        viewModel.onSoundSelected(ProjectsIds.Noise.RAIN)
        viewModel.onNextClick()
        viewModel.onTitleChanged("  Custom  ")
        viewModel.onDescriptionChanged("   ")
        viewModel.onNextClick()
        viewModel.onNextClick()
        advanceUntilIdle()

        val input = repository.savedInput
        assertNotNull(input)
        assertEquals("Custom", input?.title)
        assertEquals("", input?.description)
        assertEquals(SceneCategory.FOCUS, input?.category)
        assertEquals(ProjectsIds.Background.MORNING_MIST, input?.backgroundResName)
        assertNull(input?.backgroundUri)
        assertNull(input?.videoUri)
        assertEquals(listOf(ProjectsIds.Noise.RAIN), input?.audioTracks?.map { it.id })
        assertEquals(EditEvent.ShowToast(UiText.StringResource(com.wujia.foundation.ui.R.string.scene_edit_success)), viewModel.events.first())
        assertEquals(EditEvent.SaveSucceeded, viewModel.events.first())
    }

    @Test
    fun existingSceneSave_savesDirectly() = runTest(testDispatcher) {
        val repository = FakeSceneResourceRepository(
            editableScene = EditableScene(
                id = "scene-1",
                title = "Existing",
                description = "Stored",
                category = SceneCategory.FOCUS,
                backgroundResName = ProjectsIds.Background.MORNING_MIST,
                backgroundUri = null,
                videoUri = null,
                audioTracks = listOf(SceneEditAudio(id = ProjectsIds.Noise.RAIN, title = "雨声", uri = "audio://rain")),
                controlSettings = SceneControlSettings(),
            ),
            savedId = "scene-1",
        )
        val viewModel = viewModel(repository)
        viewModel.setSceneParameters(sceneId = "scene-1", category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onNextClick()
        viewModel.onNextClick()
        viewModel.onNextClick()
        viewModel.onNextClick()
        advanceUntilIdle()

        assertNotNull(repository.savedInput)
        assertEquals("scene-1", repository.savedInput?.id)
    }

    @Test
    fun saveCurrentScene_failureShowsErrorAndAllowsRetryState() = runTest(testDispatcher) {
        val repository = FakeSceneResourceRepository(saveFailure = IllegalStateException("boom"))
        val viewModel = viewModel(repository)
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onMaterialSelected(morningMistMaterial(viewModel))
        viewModel.onNextClick()
        viewModel.onSoundSelected(ProjectsIds.Noise.RAIN)
        viewModel.onNextClick()
        viewModel.onTitleChanged("Custom")
        viewModel.onDescriptionChanged("Description")
        viewModel.onNextClick()
        viewModel.onNextClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertEquals(EditEvent.ShowToast(UiText.DynamicString("boom")), viewModel.events.first())
    }

    @Test
    fun soundCategorySelection_filtersSoundsWithoutRecommendedCategory() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onSoundCategorySelected(2)

        val state = viewModel.uiState.value
        assertEquals(listOf("自然", "治愈", "专注", "助眠", "本地"), state.soundState.categories)
        assertEquals(listOf(ProjectsIds.Noise.PIANO), state.soundState.filteredPresets.map { it.id })
        assertEquals(emptyList<String>(), state.soundState.selectedSoundIds)
    }

    @Test
    fun soundSelection_togglesAndLimitsToThreeSounds() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onSoundSelected(ProjectsIds.Noise.RAIN)
        viewModel.onSoundSelected(ProjectsIds.Noise.FIREPLACE)
        viewModel.onSoundSelected(ProjectsIds.Noise.WIND)
        assertEquals(
            listOf(ProjectsIds.Noise.RAIN, ProjectsIds.Noise.FIREPLACE, ProjectsIds.Noise.WIND),
            viewModel.uiState.value.soundState.selectedSoundIds,
        )

        viewModel.onSoundSelected(ProjectsIds.Noise.OCEAN)
        assertEquals(
            listOf(ProjectsIds.Noise.RAIN, ProjectsIds.Noise.FIREPLACE, ProjectsIds.Noise.WIND),
            viewModel.uiState.value.soundState.selectedSoundIds,
        )
        assertTrue(viewModel.events.first() is EditEvent.ShowToast)

        viewModel.onSoundSelected(ProjectsIds.Noise.FIREPLACE)
        assertEquals(listOf(ProjectsIds.Noise.RAIN, ProjectsIds.Noise.WIND), viewModel.uiState.value.soundState.selectedSoundIds)

        viewModel.onSoundSelected(ProjectsIds.Noise.OCEAN)
        assertEquals(
            listOf(ProjectsIds.Noise.RAIN, ProjectsIds.Noise.WIND, ProjectsIds.Noise.OCEAN),
            viewModel.uiState.value.soundState.selectedSoundIds,
        )
    }

    @Test
    fun saveCurrentScene_sendsSelectedSounds() = runTest(testDispatcher) {
        val repository = FakeSceneResourceRepository(savedId = "saved-1")
        val viewModel = viewModel(repository)
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onMaterialSelected(morningMistMaterial(viewModel))
        viewModel.onSoundSelected(ProjectsIds.Noise.RAIN)
        viewModel.onSoundSelected(ProjectsIds.Noise.FIREPLACE)
        viewModel.onSoundSelected(ProjectsIds.Noise.WIND)
        viewModel.onNextClick()
        viewModel.onNextClick()
        viewModel.onTitleChanged("Custom")
        viewModel.onDescriptionChanged("Description")
        viewModel.onNextClick()
        viewModel.onNextClick()
        advanceUntilIdle()

        assertEquals(
            listOf(ProjectsIds.Noise.RAIN, ProjectsIds.Noise.FIREPLACE, ProjectsIds.Noise.WIND),
            repository.savedInput?.audioTracks?.map { it.id },
        )
    }

    @Test
    fun editorInactive_clearsSessionStateAndAllowsSameSceneToReinitialize() = runTest(testDispatcher) {
        val viewModel = viewModel()
        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        viewModel.onNextClick()
        viewModel.onTitleChanged("Draft title")
        viewModel.onSoundPreviewToggle()
        viewModel.onEditorInactive()

        val inactiveState = viewModel.uiState.value
        assertFalse(inactiveState.isLoading)
        assertEquals(SceneEditStep.Material, inactiveState.currentStep)
        assertEquals(defaultSceneTitle, inactiveState.title)
        assertNull(inactiveState.materialState.selectedMaterialId)
        assertFalse(inactiveState.soundState.isPreviewPlaying)

        viewModel.setSceneParameters(sceneId = null, category = SceneCategory.FOCUS)
        advanceUntilIdle()

        val reinitializedState = viewModel.uiState.value
        assertFalse(reinitializedState.isLoading)
        assertEquals(SceneEditStep.Material, reinitializedState.currentStep)
        assertEquals(defaultSceneTitle, reinitializedState.title)
        assertNull(reinitializedState.materialState.selectedMaterialId)
        assertFalse(reinitializedState.soundState.isPreviewPlaying)
    }

    private fun viewModel(
        repository: FakeSceneResourceRepository = FakeSceneResourceRepository(),
    ): SceneEditViewModel {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return SceneEditViewModel(
            savedStateHandle = SavedStateHandle(),
            context = context,
            getEditableScene = GetEditableSceneUseCase(repository),
            saveSceneEdit = SaveSceneEditUseCase(repository),
            localMediaStore = LocalSceneEditMediaStore(context),
            getBackgroundResources = GetBackgroundResourcesUseCase(FakeBackgroundResourceRepository()),
            getVideoResources = GetVideoResourcesUseCase(FakeVideoResourceRepository()),
            getNoiseResources = GetNoiseResourcesUseCase(FakeNoiseResourceRepository()),
            getParticleResources = GetParticleResourcesUseCase(FakeParticleResourceRepository()),
        )
    }
}

private fun morningMistMaterial(viewModel: SceneEditViewModel): MaterialPreset = viewModel.uiState.value.materialState.presets.first {
    it.backgroundResName == ProjectsIds.Background.MORNING_MIST
}

private class FakeBackgroundResourceRepository : BackgroundResourceRepository {
    private val backgrounds = listOf(
        BackgroundResource(
            id = ProjectsIds.Background.MORNING_MIST,
            title = "雾隐山居",
            description = "薄雾穿林",
            uri = "android.resource://test/drawable/morning_mist",
        ),
        BackgroundResource(
            id = ProjectsIds.Background.UNDER_MOON,
            title = "月下静湖",
            description = "湖面映月",
            uri = "android.resource://test/drawable/under_moon",
        ),
    )

    override fun observeBackgroundResources(): Flow<List<BackgroundResource>> = flowOf(backgrounds)

    override suspend fun getBackgroundResources(): List<BackgroundResource> = backgrounds

    override suspend fun getBackgroundResource(id: String): BackgroundResource? = backgrounds.firstOrNull { it.id == id }
}

private class FakeVideoResourceRepository : VideoResourceRepository {
    private val videos = listOf(
        VideoResource(
            id = ProjectsIds.Video.SLEEP_TRAIN_NIGHT,
            title = "风雪夜归人",
            description = "风雪入夜",
            uri = "android.resource://test/raw/video1",
        ),
    )

    override fun observeVideoResources(): Flow<List<VideoResource>> = flowOf(videos)

    override suspend fun getVideoResources(): List<VideoResource> = videos

    override suspend fun getVideoResource(id: String): VideoResource? = videos.firstOrNull { it.id == id }
}

private class FakeNoiseResourceRepository : NoiseResourceRepository {
    private val noises = listOf(
        NoiseResource(ProjectsIds.Noise.RAIN, "雨声", "雨声轻敲窗棂，沉浸书页之间", NoiseCategory.NATURE, "audio://rain"),
        NoiseResource(ProjectsIds.Noise.FIREPLACE, "炉火", "火焰噼啪作响，温暖安心", NoiseCategory.HEALING, "audio://fireplace"),
        NoiseResource(ProjectsIds.Noise.WIND, "风声", "林间微风穿过叶隙，清新宁静", NoiseCategory.NATURE, "audio://wind"),
        NoiseResource(ProjectsIds.Noise.OCEAN, "海浪", "潮声往复，舒缓放松", NoiseCategory.SLEEP, "audio://ocean"),
        NoiseResource(ProjectsIds.Noise.PIANO, "钢琴", "缓慢琴声铺底，适合专注", NoiseCategory.FOCUS, "audio://piano"),
        NoiseResource(ProjectsIds.Noise.THUNDER, "雷声", "远雷低鸣，雨夜氛围", NoiseCategory.SLEEP, "audio://thunder"),
    )

    override fun observeNoiseResources(): Flow<List<NoiseResource>> = flowOf(noises)

    override suspend fun getNoiseResources(): List<NoiseResource> = noises

    override suspend fun getNoiseResource(id: String): NoiseResource? = noises.firstOrNull { it.id == id }

    override suspend fun getNoiseResourcesByCategory(category: NoiseCategory): List<NoiseResource> = noises.filter { it.category == category }
}

private class FakeParticleResourceRepository : ParticleResourceRepository {
    override fun observeParticleResources(): Flow<List<ParticleResource>> = flowOf(emptyList())
    override suspend fun getParticleResources(): List<ParticleResource> = emptyList()
    override suspend fun getParticleResource(id: String): ParticleResource? = null
    override suspend fun getParticleResourcesByCategory(category: ParticleCategory): List<ParticleResource> = emptyList()
}
