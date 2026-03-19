package com.peekr.ui.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("peekr_prefs")
private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isOnboardingDone = context.dataStore.data
        .map { it[ONBOARDING_DONE] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun markDone() {
        viewModelScope.launch {
            context.dataStore.edit { it[ONBOARDING_DONE] = true }
        }
    }
}
