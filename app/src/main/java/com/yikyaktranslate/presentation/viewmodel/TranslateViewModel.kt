package com.yikyaktranslate.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.*
import com.yikyaktranslate.R
import com.yikyaktranslate.model.Language
import com.yikyaktranslate.model.TranslationRequest
import com.yikyaktranslate.model.TranslationResponse
import com.yikyaktranslate.service.face.TranslationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslateViewModel(application: Application) : AndroidViewModel(application) {

    // Connection to the back end translation service
    private val translationService = TranslationService.create()

    // Code for the source language that we are translating from; currently hardcoded to English
    private val sourceLanguageCode: String = application.getString(R.string.source_language_code)

    // List of Languages that we get from the back end
    private val languages: MutableStateFlow<List<Language>> by lazy {
        MutableStateFlow<List<Language>>(listOf()).also {
            loadLanguages()
        }
    }

    // List of names of languages to display to user
    val languagesToDisplay = languages.map { it.map { language ->  language.name } }.asLiveData()

    // Index within languages/languagesToDisplay that the user has selected
    val targetLanguageIndex = mutableStateOf(0)

    // Text that the user has input to be translated
    private val _textToTranslate = MutableStateFlow(TextFieldValue(""))
    val textToTranslate = _textToTranslate.asLiveData()

    // Translated text
    val translatedText = mutableStateOf("")

    /**
     * Loads the languages from our service
     */
    private fun loadLanguages() {
        val call = translationService.getLanguages()
        call.enqueue(object : Callback<List<Language>> {
            override fun onResponse(
                call: Call<List<Language>>,
                response: Response<List<Language>>
            ) {
                response.body()?.let { responseBody ->
                    languages.value = responseBody
                }
            }

            override fun onFailure(call: Call<List<Language>>, t: Throwable) {
                t.message?.let { Log.e(javaClass.name, it) }
                languages.value = listOf()
            }
        })
    }

    /**
     * Translate the text from the source language to the target language using our service
     */
    fun translateText() {
        val targetLanguage = languages.value[targetLanguageIndex.value].code
        val request = TranslationRequest(
            textToTranslate = _textToTranslate.value.text,
            sourceLanguage = sourceLanguageCode,
            targetLanguage = targetLanguage
        )
        val call = translationService.translate(request)

        call.enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(
                call: Call<TranslationResponse>,
                response: Response<TranslationResponse>
            ) {
                response.body()?.let { responseBody ->
                    translatedText.value = responseBody.translatedText
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                t.message?.let { Log.e(javaClass.name, it) }
                translatedText.value = ""
            }
        })
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