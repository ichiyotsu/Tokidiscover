package com.tokidiscover.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.tokidiscover.app.R

/**
 * Latin is the official Inter variable font. Han is the official Noto Sans SC
 * release of Source Han Sans. Both follow the wght axis: body 400, titles and labels 700.
 */
private fun variableFonts(latin: Int, weight: FontWeight): List<Font> {
    val variation = FontVariation.Settings(FontVariation.weight(weight.weight))
    return listOf(
        Font(latin, weight = weight, variationSettings = variation),
        Font(R.font.noto_sans_sc, weight = weight, variationSettings = variation),
    )
}

val InterFamily: FontFamily = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Bold)
        .flatMap { variableFonts(R.font.inter, it) },
)
