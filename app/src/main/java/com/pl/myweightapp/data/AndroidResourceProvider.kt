package com.pl.myweightapp.data

import android.content.Context
import com.pl.myweightapp.domain.ResourceProvider

class AndroidResourceProvider(
    private val context: Context
) : ResourceProvider {
    override fun getString(resId: Int, vararg args: Any): String {
        return context.getString(resId, args)
    }
}