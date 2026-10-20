package com.spinel.zicola.zicola.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9118481973136364/9246564585"
    private var interstitialAd: InterstitialAd? = null
    private var loading = false

    fun init(context: Context) {
        loadInterstitial(context)
    }

    private fun loadInterstitial(context: Context) {
        if (!ConsentManager.canRequestAds.value || loading || interstitialAd != null) return
        loading = true
        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitialAd = null
                }
            }
        )
    }

    // Called only when the user selects a chapter from the chapter list.
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!ConsentManager.canRequestAds.value) {
            onAdDismissed()
            return
        }
        val ad = interstitialAd
        if (ad == null) {
            loadInterstitial(activity)
            onAdDismissed()
            return
        }
        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitial(activity)
                onAdDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                loadInterstitial(activity)
                onAdDismissed()
            }
        }
        ad.show(activity)
    }
}
