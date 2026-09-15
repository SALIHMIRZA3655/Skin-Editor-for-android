package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableFloatStateOf(AndroidColor.red(initialColor) / 255f) }
    var green by remember { mutableFloatStateOf(AndroidColor.green(initialColor) / 255f) }
    var blue by remember { mutableFloatStateOf(AndroidColor.blue(initialColor) / 255f) }

    val currentColorInt = AndroidColor.rgb(
        (red * 255).toInt().coerceIn(0, 255),
        (green * 255).toInt().coerceIn(0, 255),
        (blue * 255).toInt().coerceIn(0, 255)
    )

    val quickColors = listOf(
        // Skin tones
        "#FAD7A0", "#F5CBA7", "#E59866", "#D35400", "#BA4A00", "#873600",
        // Hair & Eyes
        "#2C3E50", "#4A235A", "#1B4F72", "#145A32", "#7D6608", "#641E16",
        // Minecraft iconic colors
        "#2ECC71", "#10B981", "#00D2D3", "#3498DB", "#9B59B6", "#E74C3C",
        "#E67E22", "#F1C40F", "#ECF0F1", "#95A5A6", "#34495E", "#111827"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Renk Seçici",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Current color preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(currentColorInt))
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    )
                    Text(
                        text = String.format("#%06X", (0xFFFFFF and currentColorInt)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // RGB Sliders
                Text("Kırmızı: ${(red * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                )

                Text("Yeşil: ${(green * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                )

                Text("Mavi: ${(blue * 255).toInt()}", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hızlı Renkler:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickColors.forEach { hexColor ->
                        val parsed = AndroidColor.parseColor(hexColor)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(parsed))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    red = AndroidColor.red(parsed) / 255f
                                    green = AndroidColor.green(parsed) / 255f
                                    blue = AndroidColor.blue(parsed) / 255f
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColorInt)
                    onDismiss()
                },
                modifier = Modifier.testTag("color_picker_select_btn")
            ) {
                Text("Seç")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
