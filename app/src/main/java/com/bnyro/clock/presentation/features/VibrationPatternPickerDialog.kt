package com.bnyro.clock.presentation.features

import android.content.Context
import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.clock.R
import com.bnyro.clock.domain.model.VibrationPattern
import com.bnyro.clock.presentation.features.model.VibrationPickerModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibrationPatternPickerDialog(
    onDismissRequest: () -> Unit,
    onSelectPattern: (VibrationPattern) -> Unit,
    selectedPattern: String
) {
    val viewModel: VibrationPickerModel = viewModel(factory = VibrationPickerModel.Factory)
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface {
            Scaffold(topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.select_vibration_pattern)) }
                )
            }
            ) {
                VibrationPatternGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    patterns = viewModel.vibrationPatterns,
                    selectedPattern = selectedPattern,
                    onSelectPattern = onSelectPattern
                )
            }
        }
    }
}

@Composable
fun VibrationPatternVisualizer(
    modifier: Modifier = Modifier,
    pattern: VibrationPattern
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val path = Path()
        path.moveTo(0f, size.height)
        pattern.fractionalCumulative.forEachIndexed { index, l ->
            if (index % 2 == 0) {
                path.lineTo(size.width * l, size.height)
                path.lineTo(size.width * l, 0f)
            } else {
                path.lineTo(size.width * l, 0f)
                path.lineTo(size.width * l, size.height)
            }
        }
        drawPath(path, color = color, style = Stroke(width = 5f))
    }
}

@Composable
fun VibrationPreviewCard(
    pattern: VibrationPattern,
    selected: Boolean = false,
    onSelectPattern: (VibrationPattern) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelectPattern(pattern)
            },
        border = BorderStroke(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.primary)
    ) {
        val context = LocalContext.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(CardDefaults.shape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                VibrationPatternVisualizer(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    pattern = pattern
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pattern.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(pattern.pattern.map(Int::toLong).toLongArray(), -1)
                }) {
                    Text(text = stringResource(R.string.preview))
                }
            }
        }

    }
}

@Composable
fun VibrationPatternGrid(
    modifier: Modifier = Modifier,
    patterns: List<VibrationPattern>,
    selectedPattern: String,
    onSelectPattern: (VibrationPattern) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(350.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(patterns, key = { it.name }) { pattern ->
            VibrationPreviewCard(
                pattern = pattern,
                selected = pattern.name == selectedPattern,
                onSelectPattern = onSelectPattern
            )
        }
    }
}