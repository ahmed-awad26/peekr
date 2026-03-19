package com.peekr.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.peekr.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("الإعدادات") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.AccountCircle,
                title = "ربط الحسابات",
                subtitle = "تليجرام، يوتيوب، واتساب، فيسبوك، RSS",
                onClick = { navController.navigate(Screen.Accounts.route) }
            )
            SettingsItem(
                icon = Icons.Default.Key,
                title = "مفاتيح API",
                subtitle = "إدارة مفاتيح المنصات المختلفة",
                onClick = { navController.navigate(Screen.ApiKeys.route) }
            )
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "الأمان والخصوصية",
                subtitle = "قفل التطبيق، PIN، بصمة الإصبع",
                onClick = { navController.navigate(Screen.SecuritySettings.route) }
            )
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "النسخ الاحتياطي",
                subtitle = "تصدير، استيراد، جوجل درايف",
                onClick = { navController.navigate(Screen.Backup.route) }
            )
            SettingsItem(
                icon = Icons.Default.Article,
                title = "سجل الأحداث",
                subtitle = "عرض الأخطاء والتحذيرات",
                onClick = { navController.navigate(Screen.Logs.route) }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium)
                Text(subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp))
        }
    }
}
