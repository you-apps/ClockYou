package com.bnyro.clock.presentation.screens.timer.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TimeFieldContainerWidth = 96.0.dp
val TimeFieldContainerHeight = 72.0.dp

data class KeyboardPickerState(
    var hours: Int = 0,
    var minutes: Int = 0
) {
    val millis get() = (hours * 60 + minutes) * 60 * 1000
}

@Composable
fun KeyboardTimePicker(state: KeyboardPickerState) {
    var hoursValue by remember {
        mutableStateOf(state.hours.toString())
    }
    var minutesValue by remember {
        mutableStateOf(state.hours.toString())
    }

    val hoursFocus = remember {
        FocusRequester()
    }
    val minutesFocus = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit) {
        hoursFocus.requestFocus()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TimePickerTextField(
            value = hoursValue,
            onValueChange = { str ->
                state.hours = if (str.isBlank()) 0 else {
                    str.toIntOrNull()?.takeIf { it in 0..23 } ?: return@TimePickerTextField
                }
                hoursValue = str
            },
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(
                onNext = { minutesFocus.requestFocus() }
            ),
            focusRequester = hoursFocus
        )

        Spacer(modifier = Modifier.width(10.dp))

        TimePickerTextField(
            value = minutesValue,
            onValueChange = { str ->
                state.minutes = if (str.isBlank()) 0 else {
                    str.toIntOrNull()?.takeIf { it in 0..59 } ?: return@TimePickerTextField
                }
                minutesValue = str
            },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { minutesFocus.freeFocus() }
            ),
            focusRequester = minutesFocus
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester = FocusRequester(),
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .focusRequester(focusRequester)
            .size(TimeFieldContainerWidth, TimeFieldContainerHeight),
        interactionSource = interactionSource,
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        ),
        enabled = true,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        cursorBrush = Brush.verticalGradient(
            0.00f to Color.Transparent,
            0.10f to Color.Transparent,
            0.10f to MaterialTheme.colorScheme.primary,
            0.90f to MaterialTheme.colorScheme.primary,
            0.90f to Color.Transparent,
            1.00f to Color.Transparent
        )
    ) {
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            visualTransformation = VisualTransformation.None,
            innerTextField = it,
            singleLine = true,
            colors = textFieldColors,
            enabled = true,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(0.dp),
            container = {
                OutlinedTextFieldDefaults.ContainerBox(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(8.dp),
                    colors = textFieldColors,
                )
            }
        )
    }
}
