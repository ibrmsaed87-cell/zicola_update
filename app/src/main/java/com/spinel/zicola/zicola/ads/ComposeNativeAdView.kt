package com.spinel.zicola.zicola.ads

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.spinel.zicola.zicola.R

@Composable
fun ComposeNativeAdView(modifier: Modifier = Modifier) {
    val canRequestAds = com.spinel.zicola.zicola.ads.ConsentManager.canRequestAds.collectAsState().value
    if (!canRequestAds) return
    if (AdManager.isAdsDisabled()) return

    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isFailed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, "ca-app-pub-9118481973136364/8195344971")
            .forNativeAd { ad: NativeAd ->
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    android.util.Log.e("NativeAd", "Failed to load: ${error.message}")
                    isFailed = true
                }
                override fun onAdLoaded() {
                    android.util.Log.d("NativeAd", "Loaded successfully")
                }
            })
            .build()
            
        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    if (nativeAd != null) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val adView = inflater.inflate(R.layout.native_ad_layout, null) as NativeAdView
                adView
            },
            update = { adView ->
                val currentAd = nativeAd ?: return@AndroidView
                adView.headlineView = adView.findViewById(R.id.ad_headline)
                adView.bodyView = adView.findViewById(R.id.ad_body)
                adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                adView.iconView = adView.findViewById(R.id.ad_app_icon)

                (adView.headlineView as TextView).text = currentAd.headline

                if (currentAd.body == null) {
                    adView.bodyView?.visibility = android.view.View.INVISIBLE
                } else {
                    adView.bodyView?.visibility = android.view.View.VISIBLE
                    (adView.bodyView as TextView).text = currentAd.body
                }

                if (currentAd.callToAction == null) {
                    adView.callToActionView?.visibility = android.view.View.INVISIBLE
                } else {
                    adView.callToActionView?.visibility = android.view.View.VISIBLE
                    (adView.callToActionView as Button).text = currentAd.callToAction
                }

                if (currentAd.icon == null) {
                    adView.iconView?.visibility = android.view.View.GONE
                } else {
                    (adView.iconView as ImageView).setImageDrawable(currentAd.icon?.drawable)
                    adView.iconView?.visibility = android.view.View.VISIBLE
                }

                adView.setNativeAd(currentAd)
            }
        )
    }
}
