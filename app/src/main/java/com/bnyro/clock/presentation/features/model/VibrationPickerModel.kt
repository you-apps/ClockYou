package com.bnyro.clock.presentation.features.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bnyro.clock.domain.model.VibrationPattern
import com.bnyro.clock.util.VibrationPatternHelper

class VibrationPickerModel(context: Context) : ViewModel() {
    val vibrationPatterns: List<VibrationPattern> =
        VibrationPatternHelper().getVibrationPatterns(context)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                VibrationPickerModel((this[APPLICATION_KEY] as Application).applicationContext)
            }
        }
    }
}