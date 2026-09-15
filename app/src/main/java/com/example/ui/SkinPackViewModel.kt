package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SkinEntity
import com.example.data.SkinPackDatabase
import com.example.data.SkinPackEntity
import com.example.data.SkinPackRepository
import com.example.data.SkinPackWithSkins
import com.example.export.McPackExporter
import com.example.export.McPackImporter
import com.example.model.CharacterPart
import com.example.model.LayerType
import com.example.model.PartFace
import com.example.model.PartRect
import com.example.model.PresetSkin
import com.example.model.PresetSkinLibrary
import com.example.model.SkinTextureHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    PACK_LIST,
    PACK_DETAIL,
    SKIN_EDITOR,
    PRESET_LIBRARY
}

enum class DrawingTool(val displayName: String) {
    PENCIL("Kalem"),
    ERASER("Silgi"),
    EYEDROPPER("Damlalık"),
    BUCKET("Kova")
}

class SkinPackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SkinPackRepository

    init {
        val db = SkinPackDatabase.getDatabase(application)
        repository = SkinPackRepository(db.skinPackDao(), application)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val allPacksWithSkins: StateFlow<List<SkinPackWithSkins>> = repository.allPacksWithSkins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.PACK_LIST)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedPackId = MutableStateFlow<Long?>(null)
    val selectedPackId: StateFlow<Long?> = _selectedPackId.asStateFlow()

    // Skin Editor State
    private val _editingSkinId = MutableStateFlow<Long?>(null)
    val editingSkinId: StateFlow<Long?> = _editingSkinId.asStateFlow()

    private val _skinName = MutableStateFlow("Yeni Karakter")
    val skinName: StateFlow<String> = _skinName.asStateFlow()

    private val _isSlim = MutableStateFlow(false)
    val isSlim: StateFlow<Boolean> = _isSlim.asStateFlow()

    private val _pixels = MutableStateFlow(SkinTextureHelper.createEmptyPixels())
    val pixels: StateFlow<IntArray> = _pixels.asStateFlow()

    private val _selectedTool = MutableStateFlow(DrawingTool.PENCIL)
    val selectedTool: StateFlow<DrawingTool> = _selectedTool.asStateFlow()

    private val _selectedColor = MutableStateFlow(Color.parseColor("#3498DB"))
    val selectedColor: StateFlow<Int> = _selectedColor.asStateFlow()

    private val _selectedPart = MutableStateFlow(CharacterPart.HEAD)
    val selectedPart: StateFlow<CharacterPart> = _selectedPart.asStateFlow()

    private val _selectedFace = MutableStateFlow(PartFace.FRONT)
    val selectedFace: StateFlow<PartFace> = _selectedFace.asStateFlow()

    private val _selectedLayer = MutableStateFlow(LayerType.BASE)
    val selectedLayer: StateFlow<LayerType> = _selectedLayer.asStateFlow()

    private val _previewIsBack = MutableStateFlow(false)
    val previewIsBack: StateFlow<Boolean> = _previewIsBack.asStateFlow()

    // Undo / Redo history
    private val undoStack = mutableListOf<IntArray>()
    private val redoStack = mutableListOf<IntArray>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Export Dialog State
    private val _exportResult = MutableStateFlow<McPackExporter.ExportResult?>(null)
    val exportResult: StateFlow<McPackExporter.ExportResult?> = _exportResult.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _exportedPack = MutableStateFlow<SkinPackEntity?>(null)
    val exportedPack: StateFlow<SkinPackEntity?> = _exportedPack.asStateFlow()

    // Import Feedback State
    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    private val _importSuccessMessage = MutableStateFlow<String?>(null)
    val importSuccessMessage: StateFlow<String?> = _importSuccessMessage.asStateFlow()

    fun clearImportFeedback() {
        _importError.value = null
        _importSuccessMessage.value = null
    }

    // Navigation Actions
    fun navigateToPackDetail(packId: Long) {
        _selectedPackId.value = packId
        _currentScreen.value = AppScreen.PACK_DETAIL
    }

    fun navigateToPackList() {
        _selectedPackId.value = null
        _currentScreen.value = AppScreen.PACK_LIST
    }

    fun navigateToPresetLibrary() {
        _currentScreen.value = AppScreen.PRESET_LIBRARY
    }

    // Pack Actions
    fun createPack(name: String, description: String, version: String) {
        viewModelScope.launch {
            val newId = repository.createPack(name, description, version)
            _selectedPackId.value = newId
            _currentScreen.value = AppScreen.PACK_DETAIL
        }
    }

    fun deletePack(pack: SkinPackEntity) {
        viewModelScope.launch {
            repository.deletePack(pack)
            if (_selectedPackId.value == pack.id) {
                navigateToPackList()
            }
        }
    }

    fun updatePackInfo(pack: SkinPackEntity, name: String, description: String, version: String) {
        viewModelScope.launch {
            val updated = pack.copy(
                name = name.trim().ifEmpty { "Özel Skin Paketi" },
                description = description.trim().ifEmpty { pack.description },
                version = version.trim().ifEmpty { "1.0.0" }
            )
            repository.updatePack(updated)
            _importSuccessMessage.value = "\"${updated.name}\" güncellendi."
        }
    }

    fun renameSkin(skinId: Long, newName: String) {
        viewModelScope.launch {
            val name = newName.trim().ifEmpty { "Karakter" }
            repository.renameSkin(skinId, name)
            _importSuccessMessage.value = "Karakter adı değiştirildi: $name"
        }
    }

    // Skin Editor Start / Actions
    fun startNewSkin(isSlimModel: Boolean = false) {
        _editingSkinId.value = null
        _skinName.value = if (isSlimModel) "Yeni Karakter (Slim)" else "Yeni Karakter (Klasik)"
        _isSlim.value = isSlimModel
        _pixels.value = SkinTextureHelper.createEmptyPixels()
        _selectedPart.value = CharacterPart.HEAD
        _selectedFace.value = PartFace.FRONT
        _selectedLayer.value = LayerType.BASE
        _previewIsBack.value = false
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoStates()
        _currentScreen.value = AppScreen.SKIN_EDITOR
    }

    fun editSkin(skin: SkinEntity) {
        _editingSkinId.value = skin.id
        _skinName.value = skin.name
        _isSlim.value = skin.isSlim
        _pixels.value = SkinTextureHelper.base64ToPixels(skin.textureBase64)
        _selectedPart.value = CharacterPart.HEAD
        _selectedFace.value = PartFace.FRONT
        _selectedLayer.value = LayerType.BASE
        _previewIsBack.value = false
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoStates()
        _currentScreen.value = AppScreen.SKIN_EDITOR
    }

    fun setSkinName(name: String) {
        _skinName.value = name
    }

    fun setIsSlim(isSlimModel: Boolean) {
        _isSlim.value = isSlimModel
    }

    fun setSelectedTool(tool: DrawingTool) {
        _selectedTool.value = tool
    }

    fun setSelectedColor(color: Int) {
        _selectedColor.value = color
    }

    fun setSelectedPart(part: CharacterPart) {
        _selectedPart.value = part
    }

    fun setSelectedFace(face: PartFace) {
        _selectedFace.value = face
    }

    fun setSelectedLayer(layer: LayerType) {
        _selectedLayer.value = layer
    }

    fun togglePreviewAngle() {
        _previewIsBack.value = !_previewIsBack.value
    }

    // Drawing Logic
    private fun saveUndoState() {
        if (undoStack.size >= 25) {
            undoStack.removeAt(0)
        }
        undoStack.add(_pixels.value.clone())
        redoStack.clear()
        updateUndoRedoStates()
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_pixels.value.clone())
            val previous = undoStack.removeAt(undoStack.lastIndex)
            _pixels.value = previous
            updateUndoRedoStates()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_pixels.value.clone())
            val next = redoStack.removeAt(redoStack.lastIndex)
            _pixels.value = next
            updateUndoRedoStates()
        }
    }

    fun clearCurrentFace() {
        saveUndoState()
        val current = _pixels.value.clone()
        val rect = getCurrentFaceRect()
        for (dy in 0 until rect.height) {
            for (dx in 0 until rect.width) {
                val px = rect.x + dx
                val py = rect.y + dy
                if (px in 0 until 64 && py in 0 until 64) {
                    current[py * 64 + px] = Color.TRANSPARENT
                }
            }
        }
        _pixels.value = current
    }

    fun getCurrentFaceRect(): PartRect {
        return SkinTextureHelper.getPartRect(
            _selectedPart.value,
            _selectedFace.value,
            _selectedLayer.value,
            _isSlim.value
        )
    }

    fun onPixelClicked(localX: Int, localY: Int) {
        val rect = getCurrentFaceRect()
        if (localX !in 0 until rect.width || localY !in 0 until rect.height) return
        val globalX = rect.x + localX
        val globalY = rect.y + localY
        if (globalX !in 0 until 64 || globalY !in 0 until 64) return

        when (_selectedTool.value) {
            DrawingTool.PENCIL -> {
                saveUndoState()
                val current = _pixels.value.clone()
                current[globalY * 64 + globalX] = _selectedColor.value
                _pixels.value = current
            }
            DrawingTool.ERASER -> {
                saveUndoState()
                val current = _pixels.value.clone()
                current[globalY * 64 + globalX] = Color.TRANSPARENT
                _pixels.value = current
            }
            DrawingTool.EYEDROPPER -> {
                val color = _pixels.value[globalY * 64 + globalX]
                if (Color.alpha(color) > 0) {
                    _selectedColor.value = color
                }
                _selectedTool.value = DrawingTool.PENCIL
            }
            DrawingTool.BUCKET -> {
                saveUndoState()
                val current = _pixels.value.clone()
                // Fill entire face with selected color
                for (dy in 0 until rect.height) {
                    for (dx in 0 until rect.width) {
                        val gx = rect.x + dx
                        val gy = rect.y + dy
                        if (gx in 0 until 64 && gy in 0 until 64) {
                            current[gy * 64 + gx] = _selectedColor.value
                        }
                    }
                }
                _pixels.value = current
            }
        }
    }

    fun onPixelDragged(localX: Int, localY: Int) {
        if (_selectedTool.value != DrawingTool.PENCIL && _selectedTool.value != DrawingTool.ERASER) return
        val rect = getCurrentFaceRect()
        if (localX !in 0 until rect.width || localY !in 0 until rect.height) return
        val globalX = rect.x + localX
        val globalY = rect.y + localY
        if (globalX !in 0 until 64 || globalY !in 0 until 64) return

        val colorToApply = if (_selectedTool.value == DrawingTool.PENCIL) _selectedColor.value else Color.TRANSPARENT
        val current = _pixels.value
        if (current[globalY * 64 + globalX] != colorToApply) {
            val copy = current.clone()
            copy[globalY * 64 + globalX] = colorToApply
            _pixels.value = copy
        }
    }

    fun startDragGesture() {
        saveUndoState()
    }

    fun saveSkin() {
        val packId = _selectedPackId.value ?: return
        viewModelScope.launch {
            val skinId = _editingSkinId.value
            if (skinId == null) {
                repository.addSkinToPack(
                    packId = packId,
                    name = _skinName.value,
                    isSlim = _isSlim.value,
                    pixels = _pixels.value
                )
            } else {
                repository.updateSkin(
                    skinId = skinId,
                    packId = packId,
                    name = _skinName.value,
                    isSlim = _isSlim.value,
                    pixels = _pixels.value
                )
            }
            _currentScreen.value = AppScreen.PACK_DETAIL
        }
    }

    fun deleteSkin(skinId: Long) {
        viewModelScope.launch {
            repository.deleteSkin(skinId)
        }
    }

    // Import from Gallery / Bitmap
    fun importSkinFromBitmap(bitmap: Bitmap, name: String, isSlimModel: Boolean) {
        val packId = _selectedPackId.value ?: return
        val pixels = SkinTextureHelper.bitmapToPixels(bitmap)
        viewModelScope.launch {
            repository.addSkinToPack(
                packId = packId,
                name = name.ifEmpty { "İçe Aktarılan Skin" },
                isSlim = isSlimModel,
                pixels = pixels
            )
        }
    }

    // Import from Preset
    fun importPresetSkin(preset: PresetSkin, customName: String = preset.name) {
        val packId = _selectedPackId.value ?: return
        val pixels = preset.generator()
        viewModelScope.launch {
            repository.addSkinToPack(
                packId = packId,
                name = customName,
                isSlim = preset.isSlim,
                pixels = pixels
            )
            _currentScreen.value = AppScreen.PACK_DETAIL
        }
    }

    fun editPresetSkinInEditor(preset: PresetSkin) {
        _editingSkinId.value = null
        _skinName.value = "${preset.name} (Özelleştirilmiş)"
        _isSlim.value = preset.isSlim
        _pixels.value = preset.generator()
        _selectedPart.value = CharacterPart.HEAD
        _selectedFace.value = PartFace.FRONT
        _selectedLayer.value = LayerType.BASE
        _previewIsBack.value = false
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoStates()
        _currentScreen.value = AppScreen.SKIN_EDITOR
    }

    // Export Pack to .mcpack
    fun exportPack(context: Context, pack: SkinPackEntity, skins: List<SkinEntity>) {
        // Ensure fresh UUIDs are generated for the pack and saved, so Minecraft Bedrock never detects a duplicate pack
        val freshManifestUuid = UUID.randomUUID().toString()
        val freshModuleUuid = UUID.randomUUID().toString()
        val updatedPack = pack.copy(manifestUuid = freshManifestUuid, moduleUuid = freshModuleUuid)
        viewModelScope.launch {
            repository.updatePack(updatedPack)
        }

        val result = McPackExporter.createMcPack(context, updatedPack, skins)
        _exportResult.value = result
        _exportedPack.value = updatedPack
        _showExportDialog.value = true
    }

    fun dismissExportDialog() {
        _showExportDialog.value = false
        _exportResult.value = null
        _exportedPack.value = null
    }

    fun openExportedInMinecraft(context: Context) {
        val uri = _exportResult.value?.uri ?: return
        McPackExporter.openInMinecraft(context, uri)
    }

    fun shareExportedPack(context: Context) {
        val uri = _exportResult.value?.uri ?: return
        val packName = _exportedPack.value?.name ?: "SkinPaketi"
        McPackExporter.shareMcPack(context, uri, packName)
    }

    // Import .mcpack to create and edit a pack
    fun importMcPack(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = McPackImporter.importFromUri(context, uri)
            if (result.success && result.pack != null) {
                val newPackId = repository.importPack(result.pack)
                _importSuccessMessage.value = "\"${result.pack.name}\" paketi (${result.pack.skins.size} karakter) içe aktarıldı!"
                _selectedPackId.value = newPackId
                _currentScreen.value = AppScreen.PACK_DETAIL
            } else {
                _importError.value = result.errorMessage ?: "Paket içe aktarılamadı."
            }
        }
    }

    // Import skins from an .mcpack into the currently opened pack
    fun importSkinsFromMcPackIntoCurrentPack(context: Context, uri: Uri) {
        val packId = _selectedPackId.value ?: return
        viewModelScope.launch {
            val result = McPackImporter.importFromUri(context, uri)
            if (result.success && result.pack != null) {
                for (skinData in result.pack.skins) {
                    repository.addSkinToPack(
                        packId = packId,
                        name = skinData.name,
                        isSlim = skinData.isSlim,
                        pixels = skinData.pixels
                    )
                }
                _importSuccessMessage.value = "${result.pack.skins.size} karakter pakete eklendi!"
            } else {
                _importError.value = result.errorMessage ?: "Karakterler içe aktarılamadı."
            }
        }
    }
}
