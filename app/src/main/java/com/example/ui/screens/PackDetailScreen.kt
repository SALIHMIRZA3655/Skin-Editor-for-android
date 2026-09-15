package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SkinEntity
import com.example.data.SkinPackWithSkins
import com.example.ui.components.SkinPreviewCard
import com.example.ui.theme.CraftGreen
import com.example.ui.theme.DiamondCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackDetailScreen(
    packWithSkins: SkinPackWithSkins?,
    onBack: () -> Unit,
    onStartNewSkin: (isSlim: Boolean) -> Unit,
    onEditSkin: (SkinEntity) -> Unit,
    onDeleteSkin: (Long) -> Unit,
    onOpenPresets: () -> Unit,
    onImportBitmap: (android.graphics.Bitmap, name: String, isSlim: Boolean) -> Unit,
    onImportFromMcPack: (Uri) -> Unit,
    onExportMcPack: () -> Unit,
    onUpdatePack: (name: String, description: String, version: String) -> Unit = { _, _, _ -> },
    onRenameSkin: (skinId: Long, newName: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showNewSkinChoiceDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var importSkinName by remember { mutableStateOf("İçe Aktarılan Karakter") }
    var importIsSlim by remember { mutableStateOf(false) }

    // Pack Rename / Edit Dialog State
    var showEditPackDialog by remember { mutableStateOf(false) }
    var editPackName by remember(packWithSkins?.pack?.name) { mutableStateOf(packWithSkins?.pack?.name ?: "") }
    var editPackDesc by remember(packWithSkins?.pack?.description) { mutableStateOf(packWithSkins?.pack?.description ?: "") }
    var editPackVersion by remember(packWithSkins?.pack?.version) { mutableStateOf(packWithSkins?.pack?.version ?: "1.0.0") }

    // Skin Rename Dialog State
    var skinToRename by remember { mutableStateOf<SkinEntity?>(null) }
    var renameSkinInput by remember { mutableStateOf("") }

    // .mcpack picker to import characters into this pack
    val mcPackPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportFromMcPack(uri)
        }
    }

    // Zero-permission modern Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    pendingImportBitmap = bitmap
                    importSkinName = "Özel Karakter"
                    importIsSlim = false
                    showImportConfirmDialog = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Görsel yüklenemedi!")
                    }
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Hata: ${e.localizedMessage}")
                }
            }
        }
    }

    if (packWithSkins == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Paket bulunamadı")
        }
        return
    }

    val pack = packWithSkins.pack
    val skins = packWithSkins.skins

    // Model selection dialog for New Skin
    if (showNewSkinChoiceDialog) {
        var selectedSlimChoice by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showNewSkinChoiceDialog = false },
            title = {
                Text("Karakter Modelini Seçin", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Minecraft Bedrock için karakterinizin kol genişliğini belirleyin:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            onClick = { selectedSlimChoice = false },
                            colors = CardDefaults.cardColors(
                                containerColor = if (!selectedSlimChoice) CraftGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (!selectedSlimChoice) 2.dp else 1.dp,
                                if (!selectedSlimChoice) CraftGreen else Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Klasik (Steve)", fontWeight = FontWeight.Bold, color = CraftGreen)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("4 Piksel Kol", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Card(
                            onClick = { selectedSlimChoice = true },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedSlimChoice) DiamondCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (selectedSlimChoice) 2.dp else 1.dp,
                                if (selectedSlimChoice) DiamondCyan else Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Slim (Alex)", fontWeight = FontWeight.Bold, color = DiamondCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("3 Piksel Kol", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewSkinChoiceDialog = false
                        onStartNewSkin(selectedSlimChoice)
                    },
                    modifier = Modifier.testTag("confirm_model_choice_btn")
                ) {
                    Text("Tasarımı Başlat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSkinChoiceDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Import confirmation dialog (Skin Name + Classic/Slim model)
    if (showImportConfirmDialog && pendingImportBitmap != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportBitmap = null
            },
            title = {
                Text("İçe Aktarılan Karakter", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Karakter adını ve model tipini seçin:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = importSkinName,
                        onValueChange = { importSkinName = it },
                        label = { Text("Karakter Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Karakter Modeli:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !importIsSlim,
                            onClick = { importIsSlim = false },
                            label = { Text("Klasik (4px)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CraftGreen.copy(alpha = 0.2f),
                                selectedLabelColor = CraftGreen
                            )
                        )
                        FilterChip(
                            selected = importIsSlim,
                            onClick = { importIsSlim = true },
                            label = { Text("Slim (3px)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiamondCyan.copy(alpha = 0.2f),
                                selectedLabelColor = DiamondCyan
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bmp = pendingImportBitmap
                        if (bmp != null) {
                            onImportBitmap(bmp, importSkinName, importIsSlim)
                        }
                        showImportConfirmDialog = false
                        pendingImportBitmap = null
                    },
                    modifier = Modifier.testTag("confirm_import_btn")
                ) {
                    Text("Pakete Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportBitmap = null
                }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showEditPackDialog && pack != null) {
        AlertDialog(
            onDismissRequest = { showEditPackDialog = false },
            title = {
                Text("Paket Bilgilerini Düzenle", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editPackName,
                        onValueChange = { editPackName = it },
                        label = { Text("Paket Adı") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_pack_name_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editPackDesc,
                        onValueChange = { editPackDesc = it },
                        label = { Text("Açıklama") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_pack_desc_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editPackVersion,
                        onValueChange = { editPackVersion = it },
                        label = { Text("Versiyon") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_pack_version_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePack(editPackName, editPackDesc, editPackVersion)
                        showEditPackDialog = false
                    },
                    modifier = Modifier.testTag("save_edit_pack_btn")
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPackDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (skinToRename != null) {
        AlertDialog(
            onDismissRequest = { skinToRename = null },
            title = {
                Text("Karakter Adını Değiştir", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = renameSkinInput,
                        onValueChange = { renameSkinInput = it },
                        label = { Text("Karakter Adı") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rename_skin_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentSkin = skinToRename
                        if (currentSkin != null && renameSkinInput.isNotBlank()) {
                            onRenameSkin(currentSkin.id, renameSkinInput)
                        }
                        skinToRename = null
                    },
                    modifier = Modifier.testTag("save_rename_skin_btn")
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { skinToRename = null }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pack.name, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            "v${pack.version} • ${skins.size} Karakter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("pack_detail_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            editPackName = pack.name
                            editPackDesc = pack.description
                            editPackVersion = pack.version
                            showEditPackDialog = true
                        },
                        modifier = Modifier.testTag("pack_detail_edit_pack_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Paket Adını Değiştir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onExportMcPack,
                        modifier = Modifier.testTag("pack_detail_export_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Dışa Aktar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("skin_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High Impact Direct Export to Minecraft Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CraftGreen
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Minecraft'a Aktar (.mcpack)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tek dokunuşla oyuna entegre edin",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Button(
                            onClick = onExportMcPack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = CraftGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("direct_export_mc_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aktar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Actions: Tasarla, Galeriden Ekle, Hazır Seç, .mcpack'ten Ekle
            item {
                Text(
                    text = "Karakter Ekle & Düzenle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showNewSkinChoiceDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_new_skin_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tasarla", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_gallery_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galeriden", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenPresets,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_presets_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hazır Kütüphane", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { mcPackPickerLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_from_mcpack_btn")
                        ) {
                            Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(".mcpack'ten Ekle", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Skins Header & Counter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paketteki Karakterler (${skins.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val classicCount = skins.count { !it.isSlim }
                    val slimCount = skins.count { it.isSlim }

                    Text(
                        text = "$classicCount Klasik • $slimCount Slim",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (skins.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Bu pakette henüz karakter yok",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Yukarıdaki butonları kullanarak yeni bir karakter tasarlayabilir, hazır karakter seçebilir veya galerinizden skin yükleyebilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(skins) { skin ->
                    SkinPreviewCard(
                        skin = skin,
                        onEdit = { onEditSkin(skin) },
                        onDelete = { onDeleteSkin(skin.id) },
                        onRename = {
                            skinToRename = skin
                            renameSkinInput = skin.name
                        }
                    )
                }
            }
        }
    }
}
