package com.peekr.ui.feed.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.peekr.data.local.entities.PostEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostCard(
    post: PostEntity,
    onSaveClick: (PostEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    var isSaved by remember { mutableStateOf(false) }

    val platformColor = when (post.platformId) {
        "youtube" -> Color(0xFFFF0000)
        "telegram" -> Color(0xFF0088CC)
        "whatsapp" -> Color(0xFF25D366)
        "facebook" -> Color(0xFF1877F2)
        "rss" -> Color(0xFFFF6600)
        else -> MaterialTheme.colorScheme.primary
    }

    val platformName = when (post.platformId) {
        "youtube" -> "يوتيوب"
        "telegram" -> "تليجرام"
        "whatsapp" -> "واتساب"
        "facebook" -> "فيسبوك"
        "rss" -> "RSS"
        else -> post.platformId
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                post.postUrl?.let { url ->
                    try { uriHandler.openUri(url) } catch (e: Exception) { }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // صورة المحتوى
            post.mediaUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {

                // هيدر: اسم المصدر + المنصة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = platformColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = platformName,
                                style = MaterialTheme.typography.labelSmall,
                                color = platformColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = post.sourceName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatTime(post.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // محتوى البوست
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (!post.isRead) FontWeight.Medium else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                // فوتر: زراير
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // زرار فتح الرابط
                    post.postUrl?.let {
                        IconButton(
                            onClick = {
                                try { uriHandler.openUri(it) } catch (e: Exception) { }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "فتح الرابط",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // زرار الحفظ
                    IconButton(
                        onClick = {
                            isSaved = !isSaved
                            onSaveClick(post)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "حفظ",
                            modifier = Modifier.size(18.dp),
                            tint = if (isSaved) platformColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "الآن"
        diff < 3_600_000 -> "${diff / 60_000} د"
        diff < 86_400_000 -> "${diff / 3_600_000} س"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}
