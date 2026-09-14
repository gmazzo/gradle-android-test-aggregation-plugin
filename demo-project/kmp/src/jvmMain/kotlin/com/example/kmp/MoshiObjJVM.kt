package com.example.kmp

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoshiObjJVM(
    val a: String,
    val b: String,
)
