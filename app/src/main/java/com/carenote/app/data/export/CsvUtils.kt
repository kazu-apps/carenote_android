package com.carenote.app.data.export

fun String.escapeCsv(): String {
    val needsQuoting = contains(',') ||
        contains('"') ||
        contains('\n') ||
        contains('\r')
    return if (needsQuoting) {
        "\"${replace("\"", "\"\"")}\""
    } else {
        this
    }
}
