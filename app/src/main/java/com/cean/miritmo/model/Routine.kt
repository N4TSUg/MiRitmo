package com.cean.miritmo.model

import com.google.firebase.firestore.PropertyName

data class Routine(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val habitIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,
    @get:PropertyName("isPrivate")
    @set:PropertyName("isPrivate")
    var isPrivate: Boolean = false
)
