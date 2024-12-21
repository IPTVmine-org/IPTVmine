package com.samyak2403.iptvmine.Ads

import android.app.Activity
import android.media.AudioManager
import com.unity3d.ads.*

class MyInterstitialAds(private val activity: Activity) {


//    init {
//        // Mute all Unity Ads globally
//        UnityAds.setMute(true)
//    }

    fun loadInterstitialAds(placementId: String) {
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                // Ad loaded successfully
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                // Handle ad load failure
            }
        })
    }

    fun showInterstitialAds(placementId: String, afterSomeCode: () -> Unit) {

        UnityAds.show(activity, placementId, UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementIds: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    afterSomeCode()
                    loadInterstitialAds(placementId) // Reload the ad
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    // Called when ad starts showing
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    // Called when the user clicks the ad
                }

                override fun onUnityAdsShowComplete(
                    placementIds: String?,
                    state: UnityAds.UnityAdsShowCompletionState?
                ) {
                    afterSomeCode()
                    loadInterstitialAds(placementId) // Reload the ad
                }
            })
    }
}
