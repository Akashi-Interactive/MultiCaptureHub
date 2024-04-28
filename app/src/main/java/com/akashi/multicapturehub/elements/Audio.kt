package com.akashi.multicapturehub.elements

import android.net.Uri
import java.io.Serializable

data class Song (
    val title: String,
    val location: String,
    val songUri: Uri
) : Serializable