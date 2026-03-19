package com.peekr.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.peekr.data.local.entities.PostEntity
import com.peekr.ui.archive.ArchiveViewModel
import com.peekr.ui.archive.SavePostDialog
import com.peekr.ui.feed.components.PostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = hiltViewModel(),
    archiveViewModel: ArchiveViewModel = hiltViewModel()
) {
    val uiState by feedViewModel.uiState.collectAsState()
    val archiveState by archiveViewModel.uiState.collectAsState()
    var postToSave by remember { mutableStateOf<PostEntity?>(null) }

    val platforms = listOf(
        "all" to "الكل",
        "youtube" to "يوتيوب",
        "telegram" to "تليجرام",
        "whatsapp" to "واتساب",
        "facebook" to "فيسبوك",
        "rss" to "RSS"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Peekr",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Badge { Text("${uiState.unreadCount}") }
                        }
                    }
                },
                actions = {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { feedViewModel.syncAll() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // تبويبات المنصات
            ScrollableTabRow(
                selectedTabIndex = platforms.indexOfFirst { it.first == uiState.selectedPlatform }.coerceAtLeast(0),
                edgePadding = 16.dp
            ) {
                platforms.forEach { (id, name) ->
                    Tab(
                        selected = uiState.selectedPlatform == id,
                        onClick = { feedViewModel.selectPlatform(id) },
                        text = { Text(name) }
                    )
                }
            }

            // خطأ
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        TextButton(onClick = { feedViewModel.clearError() }) { Text("إغلاق") }
                    }
                }
            }

            // المحتوى
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "لا يوجد محتوى بعد",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "اربط حساباتك أو اضغط تحديث",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { feedViewModel.syncAll() }) { Text("تحديث الآن") }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onSaveClick = { postToSave = it }
                        )
                    }
                }
            }
        }
    }

    // ديالوج الحفظ
    postToSave?.let { post ->
        SavePostDialog(
            post = post,
            categories = archiveState.categories,
            onSave = { categoryId, note ->
                archiveViewModel.savePost(post, categoryId, note)
                postToSave = null
            },
            onDismiss = { postToSave = null }
        )
    }
}
