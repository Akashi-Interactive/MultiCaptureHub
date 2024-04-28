package com.akashi.multicapturehub.elements

import android.net.Uri
import java.io.Serializable

data class Video(
    val name: String,
    val location: String,
    val videoUri: Uri
) : Serializable
