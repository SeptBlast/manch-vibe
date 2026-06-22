package com.solace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SolaceApplication : Application()
// Firebase is auto-initialised via google-services.json — no manual init needed.
// Remote Config defaults are loaded in RemoteConfigRepository on first fetch.
