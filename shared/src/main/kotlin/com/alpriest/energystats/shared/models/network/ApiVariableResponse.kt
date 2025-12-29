package com.alpriest.energystats.shared.models.network

data class ApiVariableArray(
    val array: List<ApiVariable>
)

data class ApiVariable(
    val name: String,
    val variable: String,
    val unit: String?
)
