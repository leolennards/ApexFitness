package com.example.apexfitness.ui.body

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.apexfitness.ui.authentication.AuthService
import com.example.apexfitness.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Progress photos. They are saved only on this phone, never uploaded.
@Composable
fun ProgressPhotosScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authService = remember { AuthService(context.applicationContext) }
    val uid = authService.getCurrentUser()?.uid
    val coroutineScope = rememberCoroutineScope()
    val glassState = rememberGlassState()

    var photos by remember { mutableStateOf<List<File>>(emptyList()) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<File?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid, refreshKey) {
        photos = if (uid != null) ProgressPhotoStore.list(context, uid) else emptyList()
    }

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && uid != null) {
            coroutineScope.launch {
                val saved = ProgressPhotoStore.saveFromUri(context, uid, uri)
                if (saved == null) errorText = "I could not read that photo." else errorText = null
                refreshKey++
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val temp = ProgressPhotoStore.cameraTempFile(context)
        if (success && uid != null) {
            coroutineScope.launch {
                val saved = ProgressPhotoStore.saveFromUri(context, uid, ProgressPhotoStore.uriFor(context, temp))
                if (saved == null) errorText = "I could not save that photo." else errorText = null
                temp.delete()
                refreshKey++
            }
        } else {
            temp.delete()
        }
    }

    fun takePhoto() {
        try {
            val temp = ProgressPhotoStore.cameraTempFile(context)
            cameraLauncher.launch(ProgressPhotoStore.uriFor(context, temp))
        } catch (e: Exception) {
            errorText = "No camera app was found on this phone."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassScreenBackground(glassState)
    ) {
        ApexScreenHeader(
            title = "Progress Photos",
            label = "PRIVATE, ON THIS PHONE",
            onBack = { navController.popBackStack() }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (photos.isEmpty()) {
                EmptyPhotosState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenEdge,
                        end = Dimens.ScreenEdge,
                        bottom = Dimens.Space2
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Space1),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space1)
                ) {
                    if (photos.size >= 2) {
                        item(key = "compare", span = { GridItemSpan(maxLineSpan) }) {
                            ThenAndNowCard(
                                oldest = photos.last(),
                                newest = photos.first(),
                                glassState = glassState,
                                onOpen = { selected = it }
                            )
                        }
                    }
                    item(key = "label", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "ALL PHOTOS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.apex.mutedText,
                            modifier = Modifier.padding(start = 4.dp, top = Dimens.Space1)
                        )
                    }
                    items(photos, key = { it.name }) { file ->
                        PhotoTile(file = file, onClick = { selected = file })
                    }
                }
            }
        }

        errorText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenEdge)
            )
        }

        ApexPrimaryButton(
            text = "Take Photo",
            icon = Icons.Outlined.CameraAlt,
            onClick = { takePhoto() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenEdge, vertical = Dimens.Space1)
        )
        TextButton(
            onClick = {
                pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenEdge)
                .heightIn(min = Dimens.MinTouchTarget)
        ) {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "  Add from gallery",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.apex.accentText
            )
        }
    }

    selected?.let { file ->
        PhotoViewerDialog(
            file = file,
            onDismiss = { selected = null },
            onDelete = {
                ProgressPhotoStore.delete(file)
                selected = null
                refreshKey++
            }
        )
    }
}

// Loads a photo off the main thread. Shows a soft placeholder until it is ready.
@Composable
private fun rememberPhoto(file: File, maxSize: Int): Bitmap? {
    val bitmap by produceState<Bitmap?>(initialValue = null, file, maxSize) {
        value = ProgressPhotoStore.loadBitmap(file, maxSize)
    }
    return bitmap
}

@Composable
private fun PhotoTile(file: File, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bitmap = rememberPhoto(file, 600)
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .apexClickable(onClick = onClick)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Progress photo from ${dateFormat.format(Date(ProgressPhotoStore.dateMillis(file)))}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = dateFormat.format(Date(ProgressPhotoStore.dateMillis(file))),
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
                .fillMaxWidth()
                .padding(horizontal = Dimens.Space1, vertical = 4.dp)
        )
    }
}

// Oldest and newest photo side by side
@Composable
private fun ThenAndNowCard(
    oldest: File,
    newest: File,
    glassState: GlassState,
    onOpen: (File) -> Unit
) {
    val days = remember(oldest, newest) {
        TimeUnit.MILLISECONDS.toDays(ProgressPhotoStore.dateMillis(newest) - ProgressPhotoStore.dateMillis(oldest))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(glassState, shape = CardShape)
            .padding(Dimens.Space2)
    ) {
        Text(
            text = "THEN AND NOW",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.apex.mutedText
        )
        Text(
            text = if (days <= 0L) "Same day" else "$days days apart",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.apex.accentText
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space1)) {
            PhotoTile(file = oldest, onClick = { onOpen(oldest) }, modifier = Modifier.weight(1f))
            PhotoTile(file = newest, onClick = { onOpen(newest) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PhotoViewerDialog(file: File, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val bitmap = rememberPhoto(file, 1400)
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d yyyy", Locale.getDefault()) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = ApexShapes.large,
        title = {
            Text(
                text = dateFormat.format(Date(ProgressPhotoStore.dateMillis(file))),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            if (confirmDelete) {
                Text(
                    text = "Delete this photo from your phone? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.apex.mutedText
                )
            } else if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Progress photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .clip(ApexShapes.small)
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        },
        confirmButton = {
            if (confirmDelete) {
                TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)) {
                    Text("Delete", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)) {
                    Text("Close", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.apex.accentText)
                }
            }
        },
        dismissButton = {
            if (confirmDelete) {
                TextButton(onClick = { confirmDelete = false }, modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = Dimens.MinTouchTarget)) {
                    Text("Delete", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun EmptyPhotosState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.Space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.apex.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.apex.accentText,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Space2))
        Text(
            text = "No photos yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(Dimens.Space1))
        Text(
            text = "Take a photo now and another in a few weeks to see your change. Photos stay on this phone and are never uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.apex.mutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Photos light")
@Composable
private fun ProgressPhotosScreenPreview() {
    ApexFitnessTheme(darkTheme = false) {
        ProgressPhotosScreen(navController = rememberNavController())
    }
}
