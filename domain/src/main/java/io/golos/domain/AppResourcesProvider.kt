package io.golos.domain

import androidx.annotation.*
import java.io.InputStream

@Deprecated("Need use extensions or from android resorces")
interface AppResourcesProvider {
    fun getRaw(@RawRes resId: Int): InputStream

    fun getCountries(): InputStream

    fun getCommunities(): InputStream

    fun getString(@StringRes resId: Int): String

    /**
     * @Deprecated need use [io.golos.cyber_android.utils.Resources.getFormattedString]
     */
    fun getFormattedString(@StringRes resId: Int, vararg args: Any): String

    /**
     * @Deprecated need use [io.golos.cyber_android.utils.Resources.getFormattedString]
     */
    fun getFormattedString(string: String, vararg args: Any): String

    fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String

    fun getDimens(@DimenRes resId: Int): Float

    fun getInteger(@IntegerRes resId: Int): Int

    @ColorInt
    fun getColor(@ColorRes resId: Int): Int

    fun getLocale(): String
}