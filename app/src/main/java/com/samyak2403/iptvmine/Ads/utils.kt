/*
 * Created by Samyak Kamble on 12/22/24, 4:02 PM
 *  Copyright (c) 2024 . All rights reserved.
 *  Last modified 12/22/24, 4:02 PM
 */

package com.samyak2403.iptvmine.Ads



import android.app.Activity
import android.content.Context
import android.widget.RelativeLayout
import android.widget.Toast
import com.unity3d.services.banners.*


fun showBannerAds(
    activity: Activity,
    adsBannerCon : RelativeLayout,
    placementId: String,
    adSize: UnityBannerSize
){

    val adView = BannerView(activity,placementId,adSize)
    val adListener = object : BannerView.IListener {
        override fun onBannerLoaded(bannerAdView: BannerView?) {
            activity.longToastShow("onBannerLoaded")
        }

        override fun onBannerShown(bannerAdView: BannerView?) {
            activity.longToastShow("onBannerShown")
        }

        override fun onBannerClick(bannerAdView: BannerView?) {
            activity.longToastShow("onBannerClick")

        }

        override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
            activity.longToastShow("error"+ errorInfo?.errorMessage)
        }

        override fun onBannerLeftApplication(bannerView: BannerView?) {
            activity.longToastShow("onBannerLeftApplication")
        }

    }
    adView.listener = adListener
    adView.load()
    adsBannerCon.addView(adView)
}


fun Context.longToastShow(msg:String){
    Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
}