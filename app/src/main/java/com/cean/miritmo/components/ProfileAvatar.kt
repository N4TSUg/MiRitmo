package com.cean.miritmo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ProfileAvatar(
    photoUrl: String?,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null
) {
    val predefinedAvatars = listOf(
        "avatar_1" to Triple(Color(0xFF10B981), Icons.Filled.Person, "Verde"),
        "avatar_2" to Triple(Color(0xFF3B82F6), Icons.Filled.Face, "Azul"),
        "avatar_3" to Triple(Color(0xFFF472B6), Icons.Filled.Favorite, "Rosa"),
        "avatar_4" to Triple(Color(0xFFF59E0B), Icons.Filled.Star, "Naranja")
    )

    var modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant)

    if (onClick != null) {
        modifier = modifier.clickable { onClick() }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            if (photoUrl.startsWith("avatar_")) {
                val avatarData = predefinedAvatars.find { it.first == photoUrl }?.second ?: predefinedAvatars[0].second
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(avatarData.first),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        avatarData.second,
                        contentDescription = "Foto de perfil",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.5f)
                    )
                }
            } else {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Perfil",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
