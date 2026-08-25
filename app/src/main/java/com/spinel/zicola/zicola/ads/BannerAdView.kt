package com.spinel.zicola.zicola.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val canRequestAds = com.spinel.zicola.zicola.ads.ConsentManager.canRequestAds.collectAsState().value
    if (!canRequestAds) return
    if (AdManager.isAdsDisabled()) return

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-9118481973136364/6939965443"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
