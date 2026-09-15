package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.SkinPackViewModel
import com.example.ui.components.ExportSuccessDialog
import com.example.ui.screens.PackDetailScreen
import com.example.ui.screens.PackListScreen
import com.example.ui.screens.PresetLibraryScreen
import com.example.ui.screens.SkinEditorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SkinPackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: SkinPackViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allPacks by viewModel.allPacksWithSkins.collectAsStateWithLifecycle()
    val selectedPackId by viewModel.selectedPackId.collectAsStateWithLifecycle()

    val selectedPackWithSkins = allPacks.find { it.pack.id == selectedPackId }

    // Import Feedback State
    val importError by viewModel.importError.collectAsStateWithLifecycle()
    val importSuccessMessage by viewModel.importSuccessMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(importError) {
        importError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearImportFeedback()
        }
    }

    LaunchedEffect(importSuccessMessage) {
        importSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearImportFeedback()
        }
    }

    // Export Dialog
    val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()
    val exportResult by viewModel.exportResult.collectAsStateWithLifecycle()
    val exportedPack by viewModel.exportedPack.collectAsStateWithLifecycle()

    if (showExportDialog && exportResult != null && exportResult?.success == true) {
        ExportSuccessDialog(
            exportResult = exportResult!!,
            packName = exportedPack?.name ?: "Skin Paketi",
            onOpenInMinecraft = {
                viewModel.openExportedInMinecraft(context)
            },
            onShare = {
                viewModel.shareExportedPack(context)
            },
            onDismiss = {
                viewModel.dismissExportDialog()
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                AppScreen.PACK_LIST -> {
                    PackListScreen(
                        packs = allPacks,
                        onSelectPack = { packId ->
                            viewModel.navigateToPackDetail(packId)
                        },
                        onCreatePack = { name, desc, ver ->
                            viewModel.createPack(name, desc, ver)
                        },
                        onDeletePack = { pack ->
                            viewModel.deletePack(pack)
                        },
                        onExportPack = { pack, skins ->
                            viewModel.exportPack(context, pack, skins)
                        },
                        onImportMcPack = { uri ->
                            viewModel.importMcPack(context, uri)
                        },
                        onUpdatePack = { pack, name, desc, ver ->
                            viewModel.updatePackInfo(pack, name, desc, ver)
                        }
                    )
                }

                AppScreen.PACK_DETAIL -> {
                    BackHandler {
                        viewModel.navigateToPackList()
                    }

                    PackDetailScreen(
                        packWithSkins = selectedPackWithSkins,
                        onBack = {
                            viewModel.navigateToPackList()
                        },
                        onStartNewSkin = { isSlim ->
                            viewModel.startNewSkin(isSlim)
                        },
                        onEditSkin = { skin ->
                            viewModel.editSkin(skin)
                        },
                        onDeleteSkin = { skinId ->
                            viewModel.deleteSkin(skinId)
                        },
                        onOpenPresets = {
                            viewModel.navigateToPresetLibrary()
                        },
                        onImportBitmap = { bitmap, name, isSlim ->
                            viewModel.importSkinFromBitmap(bitmap, name, isSlim)
                        },
                        onImportFromMcPack = { uri ->
                            viewModel.importSkinsFromMcPackIntoCurrentPack(context, uri)
                        },
                        onExportMcPack = {
                            if (selectedPackWithSkins != null) {
                                viewModel.exportPack(
                                    context,
                                    selectedPackWithSkins.pack,
                                    selectedPackWithSkins.skins
                                )
                            }
                        },
                        onUpdatePack = { name, desc, ver ->
                            if (selectedPackWithSkins != null) {
                                viewModel.updatePackInfo(selectedPackWithSkins.pack, name, desc, ver)
                            }
                        },
                        onRenameSkin = { skinId, newName ->
                            viewModel.renameSkin(skinId, newName)
                        }
                    )
                }

                AppScreen.SKIN_EDITOR -> {
                    BackHandler {
                        viewModel.navigateToPackDetail(selectedPackId ?: 0)
                    }

                    val skinName by viewModel.skinName.collectAsStateWithLifecycle()
                    val isSlim by viewModel.isSlim.collectAsStateWithLifecycle()
                    val pixels by viewModel.pixels.collectAsStateWithLifecycle()
                    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
                    val selectedColor by viewModel.selectedColor.collectAsStateWithLifecycle()
                    val selectedPart by viewModel.selectedPart.collectAsStateWithLifecycle()
                    val selectedFace by viewModel.selectedFace.collectAsStateWithLifecycle()
                    val selectedLayer by viewModel.selectedLayer.collectAsStateWithLifecycle()
                    val previewIsBack by viewModel.previewIsBack.collectAsStateWithLifecycle()
                    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
                    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()

                    SkinEditorScreen(
                        skinName = skinName,
                        onSkinNameChange = { viewModel.setSkinName(it) },
                        isSlim = isSlim,
                        onIsSlimChange = { viewModel.setIsSlim(it) },
                        pixels = pixels,
                        selectedTool = selectedTool,
                        onToolSelected = { viewModel.setSelectedTool(it) },
                        selectedColor = selectedColor,
                        onColorSelected = { viewModel.setSelectedColor(it) },
                        selectedPart = selectedPart,
                        onPartSelected = { viewModel.setSelectedPart(it) },
                        selectedFace = selectedFace,
                        onFaceSelected = { viewModel.setSelectedFace(it) },
                        selectedLayer = selectedLayer,
                        onLayerSelected = { viewModel.setSelectedLayer(it) },
                        previewIsBack = previewIsBack,
                        onTogglePreviewAngle = { viewModel.togglePreviewAngle() },
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onClearFace = { viewModel.clearCurrentFace() },
                        onPixelClicked = { x, y -> viewModel.onPixelClicked(x, y) },
                        onPixelDragged = { x, y -> viewModel.onPixelDragged(x, y) },
                        onStartDrag = { viewModel.startDragGesture() },
                        onSave = { viewModel.saveSkin() },
                        onBack = {
                            viewModel.navigateToPackDetail(selectedPackId ?: 0)
                        }
                    )
                }

                AppScreen.PRESET_LIBRARY -> {
                    BackHandler {
                        viewModel.navigateToPackDetail(selectedPackId ?: 0)
                    }

                    PresetLibraryScreen(
                        onBack = {
                            viewModel.navigateToPackDetail(selectedPackId ?: 0)
                        },
                        onAddPreset = { preset ->
                            viewModel.importPresetSkin(preset)
                        },
                        onEditPreset = { preset ->
                            viewModel.editPresetSkinInEditor(preset)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
