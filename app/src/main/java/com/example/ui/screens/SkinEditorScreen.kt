package com.example.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CharacterPart
import com.example.model.LayerType
import com.example.model.PartFace
import com.example.model.PartRect
import com.example.model.SkinTextureHelper
import com.example.ui.DrawingTool
import com.example.ui.components.ColorPickerDialog
import com.example.ui.theme.CraftGreen
import com.example.ui.theme.DiamondCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinEditorScreen(
    skinName: String,
    onSkinNameChange: (String) -> Unit,
    isSlim: Boolean,
    onIsSlimChange: (Boolean) -> Unit,
    pixels: IntArray,
    selectedTool: DrawingTool,
    onToolSelected: (DrawingTool) -> Unit,
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    selectedPart: CharacterPart,
    onPartSelected: (CharacterPart) -> Unit,
    selectedFace: PartFace,
    onFaceSelected: (PartFace) -> Unit,
    selectedLayer: LayerType,
    onLayerSelected: (LayerType) -> Unit,
    previewIsBack: Boolean,
    onTogglePreviewAngle: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearFace: () -> Unit,
    onPixelClicked: (x: Int, y: Int) -> Unit,
    onPixelDragged: (x: Int, y: Int) -> Unit,
    onStartDrag: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    val livePreviewBitmap = remember(pixels, isSlim, previewIsBack) {
        SkinTextureHelper.createCompositePreview(pixels, isSlim, isBackView = previewIsBack, scale = 4)
    }

    val currentFaceRect = remember(selectedPart, selectedFace, selectedLayer, isSlim) {
        SkinTextureHelper.getPartRect(selectedPart, selectedFace, selectedLayer, isSlim)
    }

    val presetSwatches = listOf(
        "#B88764", "#4A3324", "#FAD7A0", // skin/hair
        "#009193", "#253B82", "#565656", // clothes
        "#10B981", "#06B6D4", "#EF4444", // vibrant
        "#F59E0B", "#8E24AA", "#FFFFFF", "#111827" // utility
    )

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = { onColorSelected(it) },
            onDismiss = { showColorPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = skinName,
                            onValueChange = onSkinNameChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("skin_name_input")
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("skin_editor_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSave,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = CraftGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("skin_editor_save_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Kaydet")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Model selector (Classic vs Slim) & Live 3D preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Model Selector Chips
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Karakter Modeli:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isSlim,
                            onClick = { onIsSlimChange(false) },
                            label = { Text("Klasik (4px)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CraftGreen.copy(alpha = 0.2f),
                                selectedLabelColor = CraftGreen
                            ),
                            modifier = Modifier.testTag("model_classic_chip")
                        )

                        FilterChip(
                            selected = isSlim,
                            onClick = { onIsSlimChange(true) },
                            label = { Text("Slim (3px)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiamondCyan.copy(alpha = 0.2f),
                                selectedLabelColor = DiamondCyan
                            ),
                            modifier = Modifier.testTag("model_slim_chip")
                        )
                    }
                }

                // Live Character Preview Box with toggle front/back
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clickable(onClick = onTogglePreviewAngle)
                        .testTag("preview_toggle_box")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = livePreviewBitmap.asImageBitmap(),
                            contentDescription = "Canlı Önizleme",
                            modifier = Modifier.size(width = 38.dp, height = 56.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Döndür",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (previewIsBack) "Arka" else "Ön",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Parts Selection Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CharacterPart.values().forEach { part ->
                    FilterChip(
                        selected = selectedPart == part,
                        onClick = { onPartSelected(part) },
                        label = { Text(part.displayName, fontSize = 12.sp) },
                        modifier = Modifier.testTag("part_chip_${part.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Face Selection (Ön, Arka, Sol, Sağ, Üst, Alt) & Layer Selector (Ana vs Dış)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PartFace.values().forEach { face ->
                    FilterChip(
                        selected = selectedFace == face,
                        onClick = { onFaceSelected(face) },
                        label = { Text(face.displayName, fontSize = 11.sp) },
                        modifier = Modifier.testTag("face_chip_${face.name}")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Layer Toggle
                FilterChip(
                    selected = selectedLayer == LayerType.OVERLAY,
                    onClick = {
                        onLayerSelected(
                            if (selectedLayer == LayerType.BASE) LayerType.OVERLAY else LayerType.BASE
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (selectedLayer == LayerType.OVERLAY) "Dış Katman" else "Ana Katman",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.testTag("layer_toggle_chip")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Pixel Grid Canvas
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${selectedPart.displayName} - ${selectedFace.displayName} (${currentFaceRect.width}x${currentFaceRect.height} px)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val faceWidth = currentFaceRect.width
                    val faceHeight = currentFaceRect.height
                    val aspectRatio = faceWidth.toFloat() / faceHeight.toFloat()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(aspectRatio)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .testTag("pixel_grid_canvas")
                            .pointerInput(currentFaceRect, selectedTool, selectedColor) {
                                detectTapGestures { offset ->
                                    val cellWidth = size.width / faceWidth
                                    val cellHeight = size.height / faceHeight
                                    val px = (offset.x / cellWidth).toInt().coerceIn(0, faceWidth - 1)
                                    val py = (offset.y / cellHeight).toInt().coerceIn(0, faceHeight - 1)
                                    onPixelClicked(px, py)
                                }
                            }
                            .pointerInput(currentFaceRect, selectedTool, selectedColor) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        onStartDrag()
                                        val cellWidth = size.width / faceWidth
                                        val cellHeight = size.height / faceHeight
                                        val px = (offset.x / cellWidth).toInt().coerceIn(0, faceWidth - 1)
                                        val py = (offset.y / cellHeight).toInt().coerceIn(0, faceHeight - 1)
                                        onPixelDragged(px, py)
                                    },
                                    onDrag = { change, _ ->
                                        val cellWidth = size.width / faceWidth
                                        val cellHeight = size.height / faceHeight
                                        val px = (change.position.x / cellWidth).toInt().coerceIn(0, faceWidth - 1)
                                        val py = (change.position.y / cellHeight).toInt().coerceIn(0, faceHeight - 1)
                                        onPixelDragged(px, py)
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellWidth = size.width / faceWidth
                            val cellHeight = size.height / faceHeight

                            // 1. Draw Checkerboard background for transparency
                            for (y in 0 until faceHeight) {
                                for (x in 0 until faceWidth) {
                                    val isDark = (x + y) % 2 == 0
                                    drawRect(
                                        color = if (isDark) Color(0xFF2A2E39) else Color(0xFF1E222D),
                                        topLeft = Offset(x * cellWidth, y * cellHeight),
                                        size = Size(cellWidth, cellHeight)
                                    )
                                }
                            }

                            // 2. Draw actual pixels
                            for (y in 0 until faceHeight) {
                                for (x in 0 until faceWidth) {
                                    val gx = currentFaceRect.x + x
                                    val gy = currentFaceRect.y + y
                                    if (gx in 0 until 64 && gy in 0 until 64) {
                                        val pixelColor = pixels[gy * 64 + gx]
                                        if (AndroidColor.alpha(pixelColor) > 0) {
                                            drawRect(
                                                color = Color(pixelColor),
                                                topLeft = Offset(x * cellWidth, y * cellHeight),
                                                size = Size(cellWidth, cellHeight)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Draw grid lines
                            val gridColor = Color(0x33FFFFFF)
                            for (x in 0..faceWidth) {
                                drawLine(
                                    color = gridColor,
                                    start = Offset(x * cellWidth, 0f),
                                    end = Offset(x * cellWidth, size.height),
                                    strokeWidth = 1f
                                )
                            }
                            for (y in 0..faceHeight) {
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y * cellHeight),
                                    end = Offset(size.width, y * cellHeight),
                                    strokeWidth = 1f
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tool Bar: Kalem, Silgi, Damlalık, Kova, Geri Al, İleri Al, Temizle
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToolSelected(DrawingTool.PENCIL) },
                        colors = if (selectedTool == DrawingTool.PENCIL) {
                            IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else IconButtonDefaults.iconButtonColors(),
                        modifier = Modifier.testTag("tool_pencil_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Kalem")
                    }

                    IconButton(
                        onClick = { onToolSelected(DrawingTool.ERASER) },
                        colors = if (selectedTool == DrawingTool.ERASER) {
                            IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else IconButtonDefaults.iconButtonColors(),
                        modifier = Modifier.testTag("tool_eraser_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Silgi"
                        )
                    }

                    IconButton(
                        onClick = { onToolSelected(DrawingTool.EYEDROPPER) },
                        colors = if (selectedTool == DrawingTool.EYEDROPPER) {
                            IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else IconButtonDefaults.iconButtonColors(),
                        modifier = Modifier.testTag("tool_eyedropper_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Colorize, contentDescription = "Damlalık")
                    }

                    IconButton(
                        onClick = { onToolSelected(DrawingTool.BUCKET) },
                        colors = if (selectedTool == DrawingTool.BUCKET) {
                            IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else IconButtonDefaults.iconButtonColors(),
                        modifier = Modifier.testTag("tool_bucket_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FormatColorFill, contentDescription = "Boya Kovası")
                    }

                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.testTag("tool_undo_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Geri Al")
                    }

                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier.testTag("tool_redo_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Redo, contentDescription = "İleri Al")
                    }

                    IconButton(
                        onClick = onClearFace,
                        modifier = Modifier.testTag("tool_clear_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Yüzeyi Temizle", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color Palette Row
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Active Color Big Indicator
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor))
                            .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            .clickable { showColorPicker = true }
                            .testTag("active_color_indicator")
                    )

                    // Swatches
                    presetSwatches.forEach { hexColor ->
                        val colorInt = AndroidColor.parseColor(hexColor)
                        val isSelected = selectedColor == colorInt
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
                                    CircleShape
                                )
                                .clickable { onColorSelected(colorInt) }
                        )
                    }

                    // Open Full Color Picker
                    IconButton(
                        onClick = { showColorPicker = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("open_color_picker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Daha Fazla Renk",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
