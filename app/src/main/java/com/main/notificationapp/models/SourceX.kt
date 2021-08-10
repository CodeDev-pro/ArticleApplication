package com.main.notificationapp.models

import java.io.Serializable


data class SourceX(
    val category: String,
    val country: String,
    val description: String,
    val id: String,
    val language: String,
    val name: String,
    val url: String
) : Serializable