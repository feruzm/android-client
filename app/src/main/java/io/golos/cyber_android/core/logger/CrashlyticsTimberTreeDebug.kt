package io.golos.cyber_android.core.logger

import android.util.Log
import timber.log.Timber
import javax.inject.Inject

class CrashlyticsTimberTreeDebug
@Inject
constructor() : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if(t != null) {
            t.printStackTrace()
            return
        }

        Log.println(priority, tag, message)
    }
}