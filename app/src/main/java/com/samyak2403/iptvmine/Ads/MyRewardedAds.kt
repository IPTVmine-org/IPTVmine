/*
 * Created by Samyak Kamble on 12/22/24, 7:11 PM
 *  Copyright (c) 2024 . All rights reserved.
 *  Last modified 12/22/24, 7:11 PM
 */

package com.samyak2403.iptvmine.Ads


import android.app.Activity
import com.unity3d.ads.*

class MyRewardedAds(private val activity: Activity) {

    fun loadRewardedAds(placementId : String){
        UnityAds.load(placementId, object : IUnityAdsLoadListener{
            override fun onUnityAdsAdLoaded(placementId: String?) {
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?,
            ) {
            }
        })
    }
    fun showRewardedAds(placementId : String,afterCodeRewardCoin: () ->Unit ){
        UnityAds.show(activity,placementId, UnityAdsShowOptions(),
            object : IUnityAdsShowListener{
                override fun onUnityAdsShowFailure(
                    placementIds: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?,
                ) {
                    activity.longToastShow("error = $message")
                    loadRewardedAds(placementId)
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    activity.longToastShow("onUnityAdsShowStart")

                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    activity.longToastShow("onUnityAdsShowClick")

                }

                override fun onUnityAdsShowComplete(
                    placementIds: String?,
                    state: UnityAds.UnityAdsShowCompletionState?,
                ) {
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED){
                        // Reward the user for watching the ad to completion
                        afterCodeRewardCoin()
                    }else{
                        //Do not reward the user for skipping the ad
                        activity.longToastShow("Do not reward the user for skipping the ad")

                    }
                    loadRewardedAds(placementId)
                }

            })
    }
}