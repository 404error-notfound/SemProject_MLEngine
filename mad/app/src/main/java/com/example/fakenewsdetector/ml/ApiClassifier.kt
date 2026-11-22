package com.example.fakenewsdetector.ml

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class ClassifyRequest(val text: String)
data class ClassifyResponse(val label: String, val confidence: Float)

interface ClassifyApi {
    @POST("classify")
    suspend fun classify(@Body request: ClassifyRequest): ClassifyResponse
}

/** Calls the model API and maps its fact/fake/neutral labels into the app's verdicts. */
class ApiClassifier(
    private val api: ClassifyApi,
    private val languageDetector: LanguageDetector = LanguageDetector()
) : Classifier {
    override suspend fun classify(text: String): Classification {
        val response = api.classify(ClassifyRequest(text))
        val label = response.label.trim().lowercase()
        val canonicalLabel = when (label) {
            "fact", "label_0", "0" -> "fact"
            "fake", "label_1", "1" -> "fake"
            "neutral", "label_2", "2" -> "neutral"
            else -> throw IllegalStateException("The model API returned an unknown label.")
        }
        val confidence = response.confidence
        if (!confidence.isFinite() || confidence !in 0f..1f) {
            throw IllegalStateException("The model API returned an invalid confidence score.")
        }
        val verdict = when (canonicalLabel) {
            "fake" -> Verdict.LIKELY_FAKE
            "fact" -> Verdict.LIKELY_GENUINE
            else -> Verdict.UNVERIFIED
        }
        return Classification(
            verdict = verdict,
            confidence = confidence,
            reason = "Model prediction: $canonicalLabel. This is an automated estimate.",
            language = languageDetector.detect(text)
        )
    }
}

object ClassifierApiFactory {
    // Local USB debugging. Run adb reverse tcp:8000 tcp:8000 while the phone is connected.
    private const val BASE_URL = "http://127.0.0.1:8000/"

    fun create(): ClassifyApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ClassifyApi::class.java)
}