package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.EventEntity
import com.example.data.model.PhotoMemoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhotoGalleryScreen(
    currentEvent: EventEntity?,
    photos: List<PhotoMemoryEntity>,
    onUploadPhoto: (String, String, String) -> Unit,
    onToggleLike: (PhotoMemoryEntity) -> Unit
) {
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCaptionDialog by remember { mutableStateOf(false) }
    var selectedPhotoDetail by remember { mutableStateOf<PhotoMemoryEntity?>(null) }

    // Modern zero-permission Android Photo Picker contract (Google Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedImageUri = uri
            showCaptionDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Gallery Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Guest Memories Gallery",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${photos.size} memories shared by attendees",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("upload_memory_btn")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No memories shared yet", fontWeight = FontWeight.Bold)
                        Text("Be the first to upload an event photo!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("photo_gallery_grid"),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        PhotoMemoryCard(
                            photo = photo,
                            onClick = { selectedPhotoDetail = photo },
                            onToggleLike = { onToggleLike(photo) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_upload_photo"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Memory")
        }
    }

    // Caption & Author Dialog upon picking a photo
    if (showCaptionDialog && pickedImageUri != null) {
        var caption by remember { mutableStateOf("") }
        var uploaderName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showCaptionDialog = false
                pickedImageUri = null
            },
            title = { Text("Share Event Memory") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = pickedImageUri,
                            contentDescription = "Selected memory",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Caption *") },
                        placeholder = { Text("What made this moment special?") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("photo_caption_input")
                    )

                    OutlinedTextField(
                        value = uploaderName,
                        onValueChange = { uploaderName = it },
                        label = { Text("Your Name (Optional)") },
                        placeholder = { Text("e.g. Alex Johnson") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (caption.isNotBlank()) {
                            onUploadPhoto(pickedImageUri.toString(), caption.trim(), uploaderName.trim())
                            showCaptionDialog = false
                            pickedImageUri = null
                        }
                    },
                    modifier = Modifier.testTag("submit_photo_btn")
                ) {
                    Text("Post Memory")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showCaptionDialog = false
                    pickedImageUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full-screen Photo Detail Dialog
    selectedPhotoDetail?.let { photo ->
        Dialog(onDismissRequest = { selectedPhotoDetail = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(photo.uploaderName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.US).format(Date(photo.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { selectedPhotoDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        if (photo.imageUri.isNotBlank()) {
                            AsyncImage(
                                model = photo.imageUri,
                                contentDescription = photo.caption,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            FallbackArtwork(preset = photo.presetDrawableKey)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(photo.caption, style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onToggleLike(photo) }
                        ) {
                            Icon(
                                if (photo.isLikedByUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (photo.isLikedByUser) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${photo.likesCount} likes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoMemoryCard(
    photo: PhotoMemoryEntity,
    onClick: () -> Unit,
    onToggleLike: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("memory_card_${photo.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (photo.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = photo.imageUri,
                        contentDescription = photo.caption,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    FallbackArtwork(preset = photo.presetDrawableKey)
                }

                // Like badge top right
                Surface(
                    color = Color(0x66000000),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clickable { onToggleLike() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (photo.isLikedByUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (photo.isLikedByUser) Color(0xFFEF4444) else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${photo.likesCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = photo.caption,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "by ${photo.uploaderName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FallbackArtwork(preset: String) {
    val gradientColors = when (preset) {
        "decor" -> listOf(Color(0xFF311042), Color(0xFF1E1B4B))
        "cocktail" -> listOf(Color(0xFF065F46), Color(0xFF047857))
        "table" -> listOf(Color(0xFF78350F), Color(0xFF92400E))
        else -> listOf(Color(0xFF1E1B4B), Color(0xFF4F46E5))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = Color(0x66FFFFFF),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Event Memory",
                color = Color(0xAAFFFFFF),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
