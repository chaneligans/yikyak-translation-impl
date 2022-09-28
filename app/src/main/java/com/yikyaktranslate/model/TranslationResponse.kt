package com.yikyaktranslate.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for holding the response after sending a [TranslationRequest].
 * @property translatedText text that has been translated successfully
 */
@JsonClass(generateAdapter = true)
data class TranslationResponse(
    @Json(name="translatedText") val translatedText: String,
)
