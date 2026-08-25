package com.spinel.zicola.zicola.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ConsentManager {
    private lateinit var consentInformation: ConsentInformation
    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    fun init(activity: Activity, onConsentGathered: () -> Unit) {
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                    if (consentInformation.canRequestAds()) {
                        _canRequestAds.value = true
                        onConsentGathered()
                    }
                }
            },
            { requestConsentError ->
                if (consentInformation.canRequestAds()) {
                    _canRequestAds.value = true
                    onConsentGathered()
                }
            }
        )

        // Check if already have consent
        if (consentInformation.canRequestAds()) {
            _canRequestAds.value = true
            onConsentGathered()
        }
    }

    fun isPrivacyOptionsRequired(): Boolean {
        if (!this::consentInformation.isInitialized) return false
        return consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun showPrivacyOptionsForm(activity: Activity, onFormDismissed: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onFormDismissed()
        }
    }
}
