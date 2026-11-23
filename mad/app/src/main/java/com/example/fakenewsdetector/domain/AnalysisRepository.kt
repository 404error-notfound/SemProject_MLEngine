package com.example.fakenewsdetector.domain

import com.example.fakenewsdetector.ml.Classification
import com.example.fakenewsdetector.ml.Classifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException

/** The check could not be completed, in terms the UI can explain. */
sealed class AnalysisFailure(message: String) : Exception(message) {
    /** No usable connection. */
    data object Offline : AnalysisFailure("offline")

    /** The check exceeded the request timeout. */
    data object Timeout : AnalysisFailure("timeout")

    /** Anything else the server/model did. */
    data object Failed : AnalysisFailure("failed")
}

/**
 * The UI talks to this, never to [Classifier] directly, so it can be tested
 * with fakes and so a remote implementation can replace the on-device one
 * without touching any screen.
 */
interface AnalysisRepository {
    /** @throws AnalysisFailure when the check cannot be completed. */
    suspend fun analyze(text: String): Classification
}

/**
 * Current implementation: the on-device classifier, bounded by a request
 * timeout so a stuck model can never leave the UI on the "Checking" step.
 */
class ClassifierAnalysisRepository(
    private val classifier: Classifier,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
) : AnalysisRepository {

    override suspend fun analyze(text: String): Classification =
        try {
            withTimeout(timeoutMillis) { classifier.classify(text) }
        } catch (e: TimeoutCancellationException) {
            throw AnalysisFailure.Timeout
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            // Relevant once the HTTP client replaces the on-device model.
            throw AnalysisFailure.Offline
        } catch (e: Exception) {
            throw AnalysisFailure.Failed
        }

    companion object {
        /** Sensible request timeout from the brief. */
        const val DEFAULT_TIMEOUT_MILLIS = 20_000L
    }
}
