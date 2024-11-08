package com.jamali.eparenting.application

import android.app.Activity
import android.app.Application
import android.os.Bundle

class App : Application() {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    // App goes to background
                    Utility.setUserStatus("offline")
                }
            }

            override fun onActivityResumed(activity: Activity) {
                if (activityReferences++ == 0) {
                    // App comes to foreground
                    Utility.setUserStatus("online")
                }
            }

            override fun onActivityStopped(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
            }

            // Other lifecycle methods can be left empty
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}