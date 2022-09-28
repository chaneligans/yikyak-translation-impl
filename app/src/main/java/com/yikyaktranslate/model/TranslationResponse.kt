package com.yikyaktranslate.model

import com.squareup.moshi.Json

/**
 * Data class for holding the response after sending a [TranslationRequest].
 * @property translatedText text that has been translated successfully
 */
data class TranslationResponse(
    @Json(name="translatedText") val translatedText: String,
)
