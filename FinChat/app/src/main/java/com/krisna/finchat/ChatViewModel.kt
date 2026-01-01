package com.krisna.finchat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

data class Message(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val sources: List<String> = emptyList()
)

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _uploadStatus = MutableStateFlow("Ready to analyze reports")
    val uploadStatus = _uploadStatus.asStateFlow()

    fun uploadPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uploadStatus.value = "Uploading & Analyzing..."
            try {
                val file = uriToFile(context, uri)
                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                RetrofitClient.api.uploadPdf(body)

                _uploadStatus.value = "Success! Document Memorized."

                addMessage(Message(
                    text = "I have analyzed the document. You can ask me about revenue, risks, or strategic outlook.",
                    isUser = false
                ))

            } catch (e: Exception) {
                _uploadStatus.value = "Error: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(question: String) {
        if (question.isBlank()) return

        addMessage(Message(question, isUser = true))
        addMessage(Message("", isUser = false, isLoading = true))

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.chat(ChatRequest(question))
                val safeSources = response.sources ?: emptyList()

                removeLastLoading()
                addMessage(Message(response.answer, isUser = false, sources = safeSources))
            } catch (e: Exception) {
                removeLastLoading()
                addMessage(Message("Failed to connect to AI: ${e.localizedMessage}", isUser = false))
                e.printStackTrace()
            }
        }
    }

    private fun addMessage(msg: Message) {
        _messages.value = _messages.value + msg
    }

    private fun removeLastLoading() {
        _messages.value = _messages.value.filter { !it.isLoading }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_upload.pdf")
        FileOutputStream(file).use { output ->
            inputStream?.copyTo(output)
        }
        return file
    }
}