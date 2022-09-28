package com.yikyaktranslate.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for sending a request to translate a string.
 * @property textToTranslate string to translate
 * @property sourceLanguage language we are translating from
 * @property targetLanguage language we are translating to
 */
@JsonClass(generateAdapter = true)
data class TranslationRequest(
    @Json(name = "q") val textToTranslate: String,
    @Json(name = "source") val sourceLanguage: String,
    @Json(name = "target") val targetLanguage: String,
)
