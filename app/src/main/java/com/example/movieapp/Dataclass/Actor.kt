package com.example.movieapp.Dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Actor(
    val id :Int,
    val name: String,
    val character: String,
    val profilePath: String?
):Parcelable
