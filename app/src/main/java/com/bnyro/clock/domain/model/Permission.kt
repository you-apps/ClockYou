package com.bnyro.clock.domain.model

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.bnyro.clock.BuildConfig
import com.bnyro.clock.R

sealed class Permission(
    @StringRes
    val titleRes: Int,
    @StringRes
    val descriptionRes: Int,
    @DrawableRes
    val iconRes: Int
) {
    abstract fun hasPermission(context: Context): Boolean
    abstract fun requestPermission(activity: Activity)

    object NotificationPermission :
        Permission(
            titleRes = R.string.notification_permission_title,
            descriptionRes = R.string.notification_permission_description,
            iconRes = R.drawable.ic_alarm
        ) {
        override fun hasPermission(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        override fun requestPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }

    object AlarmPermission : Permission(
        titleRes = R.string.alarm_permission_title,
        descriptionRes = R.string.alarm_permission_description,
        iconRes = R.drawable.ic_alarm
    ) {

        override fun hasPermission(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }

        override fun requestPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
            val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:${BuildConfig.APPLICATION_ID}".toUri()
            }
            activity.startActivity(intent)
        }
    }
}