package com.bnyro.clock.presentation.screens.permission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bnyro.clock.domain.model.Permission

class PermissionModel(application: Application) :
    AndroidViewModel(application) {
    val requiredPermissions = allPermissions.filter {
        !it.hasPermission(application)
    }

    companion object {
        val allPermissions = listOf(
            Permission.AlarmPermission,
            Permission.NotificationPermission
        )
    }
}