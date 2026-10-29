package com.example.fakenewsdetector.ml

import android.content.Context

/**
 * The real on-device classifier. Left as a stub until the AI side exports a model.
 *
 * What to ask the AI teammates for:
 *  1. A quantized model file (e.g. fake_news.tflite) small enough for a phone.
 *  2. The tokenizer vocabulary file (e.g. vocab.txt) and max sequence length.
 *  3. The label order of the output (e.g. [fake, unverified, genuine]).
 * Put the files in app/src/main/assets/.
 *
 * Then:
 *  - add the TensorFlow Lite dependency in app/build.gradle.kts,
 *  - load the model once in init (Interpreter(loadModelFile(...))),
 *  - tokenize [text], run inference, map the output to a [Classification],
 *  - switch the classifier in CheckViewModel from MockClassifier to this class.
 */
class TfliteClassifier(
    @Suppress("unused") private val context: Context,
    private val languageDetector: LanguageDetector = LanguageDetector()
) : Classifier {

    override suspend fun classify(text: String): Classification {
        // TODO: replace with real inference.
        return Classification(
            verdict = Verdict.UNVERIFIED,
            confidence = 0f,
            reason = "Model not connected yet.",
            language = languageDetector.detect(text)
        )
    }
}
