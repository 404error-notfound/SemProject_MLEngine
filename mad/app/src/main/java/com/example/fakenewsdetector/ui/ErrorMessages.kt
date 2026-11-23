package com.example.fakenewsdetector.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.ClassifierAnalysisRepository
import com.example.fakenewsdetector.viewmodel.CheckError

/** Plain-language title for every error the app can show. */
@StringRes
fun CheckError.titleRes(): Int = when (this) {
    CheckError.Offline -> R.string.error_offline_title
    CheckError.Timeout -> R.string.error_timeout_title
    CheckError.Server -> R.string.error_server_title
    CheckError.EmptyText -> R.string.error_empty_text
    is CheckError.TooLong -> R.string.error_too_long_title
}

/** Plain-language explanation; never a stack trace or exception text. */
@Composable
fun CheckError.bodyText(): String = when (this) {
    CheckError.Offline -> stringResource(R.string.error_offline_body)
    CheckError.Timeout -> stringResource(
        R.string.error_timeout_body,
        (ClassifierAnalysisRepository.DEFAULT_TIMEOUT_MILLIS / 1000).toInt()
    )
    CheckError.Server -> stringResource(R.string.error_server_body)
    CheckError.EmptyText -> stringResource(R.string.error_empty_text)
    is CheckError.TooLong -> stringResource(R.string.error_too_long_body, maxChars)
}
