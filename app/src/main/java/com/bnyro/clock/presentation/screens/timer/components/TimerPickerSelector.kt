package com.bnyro.clock.presentation.screens.timer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.clock.domain.model.NumberKeypadOperation
import com.bnyro.clock.domain.model.PickerStyle
import com.bnyro.clock.presentation.components.ClockTimePicker
import com.bnyro.clock.presentation.components.ScrollTimerPicker

@Composable
fun TimerPickerSelector(
    pickerStyle: PickerStyle,
    seconds: Int,
    onSecondsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        TimerPicker(pickerStyle, seconds, onSecondsChanged)
    }
}

@Composable
private fun TimerPicker(
    pickerStyle: PickerStyle,
    seconds: Int,
    onSecondsChanged: (Int) -> Unit
) {
    when (pickerStyle) {
        PickerStyle.WHEEL -> ScrollTimerPicker(
            seconds = seconds,
            onSecondsChanged = onSecondsChanged
        )

        PickerStyle.NUMBER_PAD -> NumberPadTimerPicker(
            seconds = seconds,
            onSecondsChanged = onSecondsChanged
        )

        PickerStyle.CLOCK -> {
            var chosenHours by remember { mutableIntStateOf(seconds / 3600) }
            var chosenMinutes by remember { mutableIntStateOf(seconds % 3600 / 60) }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ClockTimePicker(
                    initialHours = chosenHours,
                    initialMinutes = chosenMinutes,
                    is24Hour = true,
                    onHoursChanged = {
                        chosenHours = it
                        onSecondsChanged(chosenHours * 3600 + chosenMinutes * 60)
                    },
                    onMinutesChanged = {
                        chosenMinutes = it
                        onSecondsChanged(chosenHours * 3600 + chosenMinutes * 60)
                    }
                )
            }
        }
    }
}

@Composable
private fun NumberPadTimerPicker(
    seconds: Int,
    onSecondsChanged: (Int) -> Unit
) {
    // the digits are typed right to left, so they are held as they read: HHMMSS
    var digits by remember {
        mutableIntStateOf(seconds / 3600 * 10000 + seconds % 3600 / 60 * 100 + seconds % 60)
    }
    val chosenHours = digits / 10000 % 100
    val chosenMinutes = digits / 100 % 100
    val chosenSeconds = digits % 100

    val pushDigits = { newDigits: Int ->
        digits = newDigits
        onSecondsChanged(
            newDigits / 10000 % 100 * 3600 + newDigits / 100 % 100 * 60 + newDigits % 100
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormattedTimerTime(
            modifier = Modifier.padding(vertical = 24.dp),
            hours = chosenHours,
            minutes = chosenMinutes,
            seconds = chosenSeconds
        )
        NumberKeypad(
            onOperation = { operation ->
                when (operation) {
                    // don't do anything if all necessary/possible numbers have been entered already
                    is NumberKeypadOperation.AddNumber -> {
                        if (chosenHours * 3600 + chosenMinutes * 60 + chosenSeconds < 10 * 3600) {
                            pushDigits(
                                if (operation.number == "00") {
                                    digits * 100
                                } else {
                                    digits * 10 + operation.number.toInt()
                                }
                            )
                        }
                    }

                    is NumberKeypadOperation.Delete -> pushDigits(digits / 10)
                    is NumberKeypadOperation.Clear -> pushDigits(0)
                }
            }
        )
    }
}
