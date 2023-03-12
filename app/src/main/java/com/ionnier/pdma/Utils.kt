package com.ionnier.pdma

import android.annotation.SuppressLint
import android.content.Context

object Utils {
    @SuppressLint("DiscouragedApi")
    fun getResource(
        name: String,
        context: Context,
        type: String = "drawable",
    ): Int? {
        val nameResourceID = context.resources.getIdentifier(name, type, context.applicationInfo.packageName)
        return if (nameResourceID == 0) {
            null
        } else {
            nameResourceID
        }
    }

}
