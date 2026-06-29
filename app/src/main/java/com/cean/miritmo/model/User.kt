package com.cean.miritmo.model

data class User(
    val id: String = "",
    val name: String = "",
    val apodo: String? = null,
    val email: String = "",
    val photoUrl: String? = null,
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList()
)
