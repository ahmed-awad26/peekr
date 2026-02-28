package com.peekr.ui.settings.apikeys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class ApiKeyField(
    val platformId: String,
    val platformName: String,
    val keyName: String,
    val placeholder: String,
    val helpText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeysScreen(navController: NavController) {

    val apiFields = listOf(
        ApiKeyField("telegram_id", "تليجرام", "API ID", "12345678", "من my.telegram.org"),
        ApiKeyField("telegram_hash", "تليجرام", "API Hash", "abcdef1234567890...", "من my.telegram.org"),
        ApiKeyField("youtube", "يوتيوب", "YouTube API Key", "AIzaSy...", "من Google Cloud Console"),
        ApiKeyField("facebook", "فيسبوك", "Access Token", "EAABsbCS...", "من developers.facebook.com"),
    )

    val keyValues = remember { mutableStateMapOf<String, String>() }
    val keyVisibility = remember { mutableStateMapOf<String, Boolean>() }
    var saveSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مفاتيح API") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "المفاتيح دي بتتحفظ مشفرة على جهازك. تقدر تغيرها في أي وقت من غير ما تأثر على باقي التطبيق.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // تجميع الـ fields حسب المنصة
            val grouped = apiFields.groupBy { it.platformName }
            grouped.forEach { (platformName, fields) ->
                item {
                    Text(
                        text = platformName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(fields.size) { index ->
                    val field = fields[index]
                    val isVisible = keyVisibility[field.platformId] ?: false

                    OutlinedTextField(
                        value = keyValues[field.platformId] ?: "",
                        onValueChange = { keyValues[field.platformId] = it },
                        label = { Text(field.keyName) },
                        placeholder = { Text(field.placeholder) },
                        supportingText = { Text(field.helpText) },
                        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                keyVisibility[field.platformId] = !isVisible
                            }) {
                                Icon(
                                    if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        // هنضيف الحفظ الفعلي لاحقاً
                        saveSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ المفاتيح")
                }

                if (saveSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ تم الحفظ بنجاح",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
