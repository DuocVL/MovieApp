package com.example.movieapp.Dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Director(
    val id : Int,
    val name: String,
    val profilePath: String?
):Parcelable
