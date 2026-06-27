package com.wujia.feature.lock.impl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wujia.foundation.domain.scene.ObserveSceneResourcesUseCase
import com.wujia.foundation.model.scene.SceneResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    observeSceneResources: ObserveSceneResourcesUseCase,
) : ViewModel() {

    val scene: StateFlow<SceneResource?> = observeSceneResources()
        .map { scenes -> scenes.firstOrNull { it.video != null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
