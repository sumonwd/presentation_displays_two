package com.phonetechbd.presentation_displays_two

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DisplayTwoJson(
    @SerializedName("displayId")
    val displayId: Int,
    @SerializedName("flags")
    val flags: Int,
    @SerializedName("rotation")
    val rotation: Int,
    @SerializedName("name")
    val name: String
)