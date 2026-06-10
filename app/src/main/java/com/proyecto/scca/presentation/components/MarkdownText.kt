package com.proyecto.scca.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    val annotatedString =
        buildAnnotatedString {
            // A very simple Markdown parser for bold (**text**) and italic (*text*)
            var currentIndex = 0
            while (currentIndex < markdown.length) {
                val boldIndex = markdown.indexOf("**", currentIndex)
                val italicIndex = markdown.indexOf("*", currentIndex)

                if (boldIndex != -1 && (italicIndex == -1 || boldIndex <= italicIndex)) {
                    // Found bold
                    val boldEndIndex = markdown.indexOf("**", boldIndex + 2)
                    if (boldEndIndex != -1) {
                        append(markdown.substring(currentIndex, boldIndex))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(markdown.substring(boldIndex + 2, boldEndIndex))
                        }
                        currentIndex = boldEndIndex + 2
                    } else {
                        append(markdown.substring(currentIndex))
                        break
                    }
                } else if (italicIndex != -1) {
                    // Found italic
                    val italicEndIndex = markdown.indexOf("*", italicIndex + 1)
                    if (italicEndIndex != -1 && italicIndex + 1 != italicEndIndex) {
                        append(markdown.substring(currentIndex, italicIndex))
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(markdown.substring(italicIndex + 1, italicEndIndex))
                        }
                        currentIndex = italicEndIndex + 1
                    } else {
                        append(markdown.substring(currentIndex))
                        break
                    }
                } else {
                    append(markdown.substring(currentIndex))
                    break
                }
            }
        }

    Text(
        text = annotatedString,
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
    )
}
