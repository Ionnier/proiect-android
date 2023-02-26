package com.ionnier.pdma.data

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class LanguageResponse(
    @SerialName("count") var count: Int? = null,
    @SerialName("next") var next: String? = null,
    @SerialName("previous") var previous: String? = null,
    @SerialName("results") var results: ArrayList<Languages> = arrayListOf()

)

@kotlinx.serialization.Serializable
data class Languages(
    @SerialName("id") var id: Int? = null,
    @SerialName("short_name") var shortName: String? = null,
    @SerialName("full_name") var fullName: String? = null

)