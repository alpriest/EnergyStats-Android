package com.alpriest.energystats.shared.models

data class OpenApiVariableArray(
    val array: List<OpenApiVariable>
)

data class OpenApiVariable(
    val name: String,
    val variable: String,
    val unit: String?
)
