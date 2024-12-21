/*
 * Created by Samyak Kamble on 12/21/24, 10:54 PM
 *  Copyright (c) 2024 . All rights reserved.
 *  Last modified 12/21/24, 10:54 PM
 */

package com.samyak2403.iptvmine.Ads


import android.app.Application
import com.samyak2403.iptvmine.R
import com.unity3d.ads.*

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // gameId change in  Monetization console
        // testMode if app release change to false
        UnityAds.initialize(this, getString(R.string.game_id),false,
            object : IUnityAdsInitializationListener{
                override fun onInitializationComplete() {}

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?,
                ) {
                }

            })

    }
}