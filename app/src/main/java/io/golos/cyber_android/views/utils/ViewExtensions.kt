package io.golos.cyber_android.views.utils

import android.text.Editable
import android.text.util.Linkify
import java.util.regex.Pattern

fun Editable.colorizeLinks() {
    colorizeByPattern(this, Patterns.WEB_URL)
}

fun Editable.colorizeHashTags() {
    colorizeByPattern(this, Patterns.HASHTAG)
}

private fun colorizeByPattern(e: Editable, pattern: Pattern) {
    Linkify.addLinks(e, pattern, null)
}