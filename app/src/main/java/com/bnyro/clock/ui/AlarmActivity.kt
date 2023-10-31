package com.bnyro.clock.ui

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bnyro.clock.db.DatabaseHolder
import com.bnyro.clock.obj.Alarm
import com.bnyro.clock.services.AlarmService
import com.bnyro.clock.ui.screens.AlarmAlertScreen
import com.bnyro.clock.util.AlarmHelper
import kotlinx.coroutines.runBlocking

class AlarmActivity : ComponentActivity() {
    private var alarm by mutableStateOf(Alarm(0, 0))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.addFlags(windowFlags)

        setContent {
            AlarmAlertScreen(onDismiss = {
                stopService(
                    Intent(
                        this@AlarmActivity.applicationContext,
                        AlarmService::class.java
                    )
                )
                this@AlarmActivity.finish()
            }, onSnooze = {
                /*TODO*/
            }, label = alarm.label)
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val id = intent.getLongExtra(AlarmHelper.EXTRA_ID, -1).takeIf { it != -1L } ?: return
        this.alarm = runBlocking {
            DatabaseHolder.instance.alarmsDao().findById(id)
        }
    }

    companion object {
        private const val windowFlags =
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    }
}
