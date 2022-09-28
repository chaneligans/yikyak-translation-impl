package com.yikyaktranslate.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.*
import com.yikyaktranslate.R
import com.yikyaktranslate.model.Language
import com.yikyaktranslate.model.TranslationRequest
import com.yikyaktranslate.service.face.TranslationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TranslateViewModel(application: Application) : AndroidViewModel(application) {

    // Connection to the back end translation service
    private val translationService = TranslationService.create()

    // Code for the source language that we are translating from; currently hardcoded to English
    private val sourceLanguageCode: String = application.getString(R.string.source_language_code)

    // List of Languages that we get from the back end
    private val _languages = MutableStateFlow<List<Language>>(listOf())
    private val languages = _languages.asStateFlow()

    // List of names of languages to display to user
    val languagesToDisplay = languages.map { it.map { language ->  language.name } }.asLiveData()

    // Index within languages/languagesToDisplay that the user has selected
    val targetLanguageIndex = mutableStateOf(0)

    // Text that the user has input to be translated
    private val _textToTranslate = MutableStateFlow(TextFieldValue(""))
    val textToTranslate = _textToTranslate.asLiveData()

    // Translated text
    private val _translatedText = MutableStateFlow("")
    val translatedText = _translatedText.asLiveData()

    init {
        // Loads the languages using a coroutine on a background thread
        viewModelScope.launch {
            loadLanguages()
        }
    }

    /**
     * Loads the languages from our service
     */
    private suspend fun loadLanguages() {
        try {
            _languages.value = translationService.getLanguages()
        } catch (ex: Exception) {
            Log.e(javaClass.name, "Failed to load languages", ex)
            _languages.value = listOf()
        }
    }

    /**
     * Translate the text from the source language to the target language using our service
     */
    private suspend fun loadTranslation() {
        val targetLanguage = languages.value[targetLanguageIndex.value]
        val targetLanguageCode = targetLanguage.code
        val request = TranslationRequest(
            textToTranslate = _textToTranslate.value.text,
            sourceLanguage = sourceLanguageCode,
            targetLanguage = targetLanguageCode
        )

        try {
            val translationResponse = translationService.translate(request)
            _translatedText.value = translationResponse.translatedText
        } catch (ex: Exception) {
            Log.e(javaClass.name, "Failed to translate text", ex)
            _translatedText.value = ""
        }
    }

    /**
     * Translates the text on the IO thread
     */
    fun translateText() {
        CoroutineScope(Dispatchers.IO).launch {
            loadTranslation()
        }
    }

    /**
     * Updates the data when there's new text from the user
     *
     * @param newText TextFieldValue that contains user input we want to keep track of
     */
    fun onInputTextChange(newText: TextFieldValue) {
        _textToTranslate.value = newText
    }

    /**
     * Updates the selected target language when the user selects a new language
     *
     * @param newLanguageIndex Represents the index for the chosen language in the list of languages
     */
    fun onTargetLanguageChange(newLanguageIndex: Int) {
        targetLanguageIndex.value = newLanguageIndex
    }

}