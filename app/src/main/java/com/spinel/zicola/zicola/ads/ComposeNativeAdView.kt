package com.spinel.zicola.zicola.ads

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.spinel.zicola.zicola.R

@Composable
fun ComposeNativeAdView(modifier: Modifier = Modifier) {
    val canRequestAds = com.spinel.zicola.zicola.ads.ConsentManager.canRequestAds.collectAsState().value
    if (!canRequestAds) return
    if (AdManager.isAdsDisabled()) return

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val inflater = LayoutInflater.from(context)
            val adView = inflater.inflate(R.layout.native_ad_layout, null) as NativeAdView

            val adLoader = AdLoader.Builder(context, "ca-app-pub-9118481973136364/8195344971")
                .forNativeAd { nativeAd: NativeAd ->
                    adView.headlineView = adView.findViewById(R.id.ad_headline)
                    adView.bodyView = adView.findViewById(R.id.ad_body)
                    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                    adView.iconView = adView.findViewById(R.id.ad_app_icon)

                    (adView.headlineView as TextView).text = nativeAd.headline
                    if (nativeAd.body == null) {
                        adView.bodyView?.visibility = android.view.View.INVISIBLE
                    } else {
                        adView.bodyView?.visibility = android.view.View.VISIBLE
                        (adView.bodyView as TextView).text = nativeAd.body
                    }

                    if (nativeAd.callToAction == null) {
                        adView.callToActionView?.visibility = android.view.View.INVISIBLE
                    } else {
                        adView.callToActionView?.visibility = android.view.View.VISIBLE
                        (adView.callToActionView as Button).text = nativeAd.callToAction
                    }

                    if (nativeAd.icon == null) {
                        adView.iconView?.visibility = android.view.View.GONE
                    } else {
                        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                        adView.iconView?.visibility = android.view.View.VISIBLE
                    }

                    adView.setNativeAd(nativeAd)
                }
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
            adView
        }
    )
}
