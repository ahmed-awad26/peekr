package com.peekr.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val description: String
)

val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.DynamicFeed,
        iconColor = Color(0xFF5B4FCF),
        title = "كل محتواك في مكان واحد",
        description = "تليجرام، يوتيوب، واتساب، فيسبوك، وRSS — كلهم في فيد واحد مرتب"
    ),
    OnboardingPage(
        icon = Icons.Default.Send,
        iconColor = Color(0xFF0088CC),
        title = "تليجرام بدون مجهود",
        description = "استقبل رسائل قنواتك وجروباتك مباشرة بدون ما تفتح تليجرام"
    ),
    OnboardingPage(
        icon = Icons.Default.SmartDisplay,
        iconColor = Color(0xFFFF0000),
        title = "يوتيوب وRSS",
        description = "تابع قنواتك المفضلة وأي موقع أخبار عن طريق RSS feed"
    ),
    OnboardingPage(
        icon = Icons.Default.Extension,
        iconColor = Color(0xFF4285F4),
        title = "أدوات قابلة للتوسيع",
        description = "أضف أي أداة HTML بسيطة عن طريق ملف ZIP وشغّلها من داخل التطبيق"
    ),
    OnboardingPage(
        icon = Icons.Default.Widgets,
        iconColor = Color(0xFF25D366),
        title = "ويدجيز على الشاشة الرئيسية",
        description = "اعرض آخر المحتوى من أي منصة أو مصدر مباشرة على شاشة الموبايل"
    ),
    OnboardingPage(
        icon = Icons.Default.Lock,
        iconColor = Color(0xFF9C27B0),
        title = "آمن وخاص",
        description = "قاعدة البيانات مشفرة، وقفل التطبيق بالبصمة أو PIN"
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(page = onboardingPages[page])
        }

        // النقاط
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(onboardingPages.size) { index ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        .size(
                            width = if (index == pagerState.currentPage) 24.dp else 8.dp,
                            height = 8.dp
                        )
                        .animateContentSize()
                )
            }
        }

        // الأزرار
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // تخطي
            if (pagerState.currentPage < onboardingPages.size - 1) {
                TextButton(
                    onClick = {
                        viewModel.markDone()
                        onFinish()
                    }
                ) {
                    Text(
                        "تخطي",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(Modifier.width(80.dp))
            }

            // التالي / ابدأ
            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.markDone()
                        onFinish()
                    }
                },
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = onboardingPages[pagerState.currentPage].iconColor
                )
            ) {
                Text(
                    if (pagerState.currentPage < onboardingPages.size - 1) "التالي ←"
                    else "ابدأ الآن! 🚀",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // أيقونة كبيرة
        Surface(
            shape = CircleShape,
            color = page.iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(140.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    page.icon,
                    contentDescription = null,
                    tint = page.iconColor,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 26.sp
        )
    }
}
