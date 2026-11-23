package com.example.fakenewsdetector.domain

import android.content.Context
import android.content.res.Configuration
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.data.AppLanguage
import java.util.Locale

/**
 * Loads the correction templates in the language the correction should be sent
 * in, independent of the current app language. Keeping them as resources means
 * the copy can be edited without code changes.
 */
class CorrectionFactory(private val context: Context) {

    fun build(result: CheckResult): String {
        val swahili = CorrectionBuilder.useSwahili(result.language)
        return CorrectionBuilder.build(result, strings(if (swahili) AppLanguage.SWAHILI else AppLanguage.ENGLISH))
    }

    fun strings(language: AppLanguage): CorrectionStrings = load(language.tag)

    private fun load(tag: String): CorrectionStrings {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(tag))
        val res = context.createConfigurationContext(config).resources
        return CorrectionStrings(
            verdictFalse = res.getString(R.string.correction_verdict_false),
            verdictUnverified = res.getString(R.string.correction_verdict_unverified),
            verdictTrue = res.getString(R.string.correction_verdict_true),
            template = res.getString(R.string.correction_message)
        )
    }
}
