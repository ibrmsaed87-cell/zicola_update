package com.spinel.zicola.zicola.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9118481973136364/9246564585"
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-9118481973136364/4649893659"

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null

    

    // User gets 2 hours free of ads if they watch the rewarded video
    private const val PREFS_NAME = "ad_reward_prefs"
    private const val AD_FREE_UNTIL_KEY = "ad_free_until"
    private lateinit var appContext: Context
    private var adFreeUntil = 0L

    fun init(context: Context) {
        appContext = context.applicationContext
        adFreeUntil = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong(AD_FREE_UNTIL_KEY, 0L)
        loadInterstitial(context)
        loadRewarded(context)
    }

    fun isAdsDisabled(): Boolean {
        return System.currentTimeMillis() < adFreeUntil
    }

    fun disableAdsForHours(hours: Int) {
        adFreeUntil = System.currentTimeMillis() + (hours * 3600000L)
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putLong(AD_FREE_UNTIL_KEY, adFreeUntil).apply()
    }
    
    fun getAdFreeRemainingTime(): Long {
        if (!isAdsDisabled()) return 0L
        return adFreeUntil - System.currentTimeMillis()
    }

    private fun loadInterstitial(context: Context) {
        if (!com.spinel.zicola.zicola.ads.ConsentManager.canRequestAds.value) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun loadRewarded(context: Context) {
        if (!com.spinel.zicola.zicola.ads.ConsentManager.canRequestAds.value) return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mRewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                mRewardedAd = ad
            }
        })
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!com.spinel.zicola.zicola.ads.ConsentManager.canRequestAds.value) {
            onAdDismissed()
            return
        }
        if (isAdsDisabled()) {
            onAdDismissed()
            return
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    loadInterstitial(activity)
                    onAdDismissed()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            if (mInterstitialAd == null) {
                loadInterstitial(activity)
            }
            onAdDismissed()
        }
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdFailedOrDismissed: () -> Unit) {
        if (!com.spinel.zicola.zicola.ads.ConsentManager.canRequestAds.value) {
            onAdFailedOrDismissed()
            return
        }
        if (mRewardedAd != null) {
            var rewardEarned = false
            mRewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mRewardedAd = null
                    loadRewarded(activity)
                    if (rewardEarned) {
                        onRewardEarned()
                    } else {
                        onAdFailedOrDismissed()
                    }
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mRewardedAd = null
                    loadRewarded(activity)
                    onAdFailedOrDismissed()
                }
            }
            mRewardedAd?.show(activity) { _ ->
                rewardEarned = true
                disableAdsForHours(2)
            }
        } else {
            loadRewarded(activity)
            onAdFailedOrDismissed()
        }
    }
}
