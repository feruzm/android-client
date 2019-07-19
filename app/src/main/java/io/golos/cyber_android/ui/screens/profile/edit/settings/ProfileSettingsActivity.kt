package io.golos.cyber_android.ui.screens.profile.edit.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.golos.cyber_android.R
import io.golos.cyber_android.ui.base.ActivityBase

class ProfileSettingsActivity : ActivityBase() {

    companion object {
        fun getIntent(context: Context) = Intent(context, ProfileSettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_settings_activity)
    }

}
