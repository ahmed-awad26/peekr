package com.peekr.data.remote.telegram

import android.content.Context
import com.peekr.core.logger.AppLogger
import com.peekr.data.local.dao.AccountDao
import com.peekr.data.local.dao.ApiKeyDao
import com.peekr.data.local.dao.PostDao
import com.peekr.data.local.entities.AccountEntity
import com.peekr.data.local.entities.PostEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import javax.inject.Inject
import javax.inject.Singleton

sealed class TelegramAuthState {
    object Idle : TelegramAuthState()
    object WaitingPhone : TelegramAuthState()
    object WaitingCode : TelegramAuthState()
    object WaitingPassword : TelegramAuthState()
    object Authorized : TelegramAuthState()
    data class Error(val message: String) : TelegramAuthState()
}

@Singleton
class TelegramClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKeyDao: ApiKeyDao,
    private val accountDao: AccountDao,
    private val postDao: PostDao,
    private val logger: AppLogger
) {
    private var client: Client? = null

    private val _authState = MutableStateFlow<TelegramAuthState>(TelegramAuthState.Idle)
    val authState: StateFlow<TelegramAuthState> = _authState

    private val resultChannel = Channel<TdApi.Object>(Channel.UNLIMITED)

    // ==============================
    // تهيئة الـ TDLib Client
    // ==============================
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val apiId = apiKeyDao.getApiKeyByPlatform("telegram_id")?.keyValue?.toIntOrNull()
            val apiHash = apiKeyDao.getApiKeyByPlatform("telegram_hash")?.keyValue

            if (apiId == null || apiHash.isNullOrEmpty()) {
                logger.warning("Telegram API ID أو Hash غير موجود", "telegram")
                _authState.value = TelegramAuthState.Error("أضف API ID و API Hash في الإعدادات أولاً")
                return@withContext false
            }

            Client.setLogVerbosityLevel(0)

            client = Client.create(
                { update -> handleUpdate(update) },
                null,
                null
            )

            logger.info("TDLib Client تم تهيئته", "telegram")
            true
        } catch (e: Exception) {
            logger.error("فشل تهيئة TDLib", "telegram", e)
            _authState.value = TelegramAuthState.Error(e.message ?: "خطأ غير معروف")
            false
        }
    }

    // ==============================
    // معالجة التحديثات
    // ==============================
    private fun handleUpdate(update: TdApi.Object) {
        when (update) {
            is TdApi.UpdateAuthorizationState -> handleAuthState(update.authorizationState)
            is TdApi.UpdateNewMessage -> handleNewMessage(update.message)
        }
    }

    private fun handleAuthState(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                sendTdlibParams()
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _authState.value = TelegramAuthState.WaitingPhone
            }
            is TdApi.AuthorizationStateWaitCode -> {
                _authState.value = TelegramAuthState.WaitingCode
            }
            is TdApi.AuthorizationStateWaitPassword -> {
                _authState.value = TelegramAuthState.WaitingPassword
            }
            is TdApi.AuthorizationStateReady -> {
                _authState.value = TelegramAuthState.Authorized
                logger.info("تليجرام: تم تسجيل الدخول بنجاح", "telegram")
            }
            is TdApi.AuthorizationStateClosed -> {
                _authState.value = TelegramAuthState.Idle
                client = null
            }
        }
    }

    private fun sendTdlibParams() {
        val filesDir = context.filesDir.absolutePath
        client?.send(
            TdApi.SetTdlibParameters().apply {
                databaseDirectory = "$filesDir/tdlib"
                filesDirectory = "$filesDir/tdlib/files"
                useMessageDatabase = true
                useSecretChats = false
                apiId = 0 // هيتملى من الـ DAo
                apiHash = ""
                systemLanguageCode = "ar"
                deviceModel = "Android"
                applicationVersion = "1.0"
            }
        ) { }
    }

    // ==============================
    // تسجيل الدخول
    // ==============================
    suspend fun sendPhoneNumber(phoneNumber: String) = withContext(Dispatchers.IO) {
        client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { result ->
            if (result is TdApi.Error) {
                _authState.value = TelegramAuthState.Error(result.message)
                logger.error("خطأ في إرسال رقم التليفون: ${result.message}", "telegram")
            }
        }
    }

    suspend fun sendCode(code: String) = withContext(Dispatchers.IO) {
        client?.send(TdApi.CheckAuthenticationCode(code)) { result ->
            if (result is TdApi.Error) {
                _authState.value = TelegramAuthState.Error(result.message)
                logger.error("خطأ في كود التحقق: ${result.message}", "telegram")
            }
        }
    }

    suspend fun sendPassword(password: String) = withContext(Dispatchers.IO) {
        client?.send(TdApi.CheckAuthenticationPassword(password)) { result ->
            if (result is TdApi.Error) {
                _authState.value = TelegramAuthState.Error(result.message)
                logger.error("خطأ في كلمة المرور: ${result.message}", "telegram")
            }
        }
    }

    // ==============================
    // جلب الرسائل الجديدة
    // ==============================
    private fun handleNewMessage(message: TdApi.Message) {
        val content = message.content
        val text = when (content) {
            is TdApi.MessageText -> content.text.text
            is TdApi.MessagePhoto -> content.caption.text.ifEmpty { "[صورة]" }
            is TdApi.MessageVideo -> content.caption.text.ifEmpty { "[فيديو]" }
            is TdApi.MessageDocument -> content.caption.text.ifEmpty { "[ملف]" }
            is TdApi.MessageVoiceNote -> "[رسالة صوتية]"
            else -> return
        }

        if (text.isEmpty()) return

        val chatId = message.chatId
        val messageId = message.id

        // تحقق من نوع المحادثة
        client?.send(TdApi.GetChat(chatId)) { chatResult ->
            if (chatResult is TdApi.Chat) {
                val chatTitle = chatResult.title
                val chatType = chatResult.type
                val isChannel = chatType is TdApi.ChatTypeSupergroup && (chatType as TdApi.ChatTypeSupergroup).isChannel

                val post = PostEntity(
                    platformId = "telegram",
                    sourceId = chatId.toString(),
                    sourceName = chatTitle,
                    content = text,
                    postUrl = "tg://openmessage?chat_id=$chatId&message_id=$messageId",
                    timestamp = (message.date.toLong()) * 1000
                )

                kotlinx.coroutines.GlobalScope.kotlinx.coroutines.launch(Dispatchers.IO) {
                    postDao.insertPost(post)
                    logger.info("تليجرام: رسالة جديدة من $chatTitle", "telegram")
                }
            }
        }
    }

    suspend fun syncChats(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (_authState.value != TelegramAuthState.Authorized) {
                return@withContext Result.failure(Exception("تليجرام مش متصل"))
            }
            logger.info("تليجرام: جاري مزامنة المحادثات", "telegram")
            Result.success(0)
        } catch (e: Exception) {
            logger.error("خطأ في مزامنة تليجرام", "telegram", e)
            Result.failure(e)
        }
    }

    // ==============================
    // قطع الاتصال
    // ==============================
    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            client?.send(TdApi.LogOut()) { }
            accountDao.deleteAccountByPlatform("telegram")
            logger.info("تليجرام: تم قطع الاتصال", "telegram")
        } catch (e: Exception) {
            logger.error("خطأ في قطع الاتصال", "telegram", e)
        }
    }

    fun isAuthorized() = _authState.value == TelegramAuthState.Authorized
}
