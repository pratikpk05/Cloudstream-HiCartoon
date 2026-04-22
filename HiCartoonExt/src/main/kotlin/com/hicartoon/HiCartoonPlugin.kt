package com.hicartoon

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class HiCartoonPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(HiCartoonProvider())
    }
}
