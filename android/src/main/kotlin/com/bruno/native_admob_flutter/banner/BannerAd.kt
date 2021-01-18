package com.bruno.native_admob_flutter.banner

import android.content.Context
import android.view.View
import com.google.android.gms.ads.*
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class BannerAdViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        return BannerAdView(context, creationParams)
    }
}

class BannerAdView(context: Context, data: Map<String?, Any?>?) : PlatformView {

    private lateinit var adView: AdView
    private var controller: BannerAdController = BannerAdControllerManager.getController(data!!["controllerId"] as String)!!
    private var adSize: AdSize

    private fun getAdSize(context: Context, width: Float): AdSize {
//        val density = Resources.getSystem().displayMetrics.density
//        val adWidth = (width / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width.toInt())
    }

    init {
        adSize = getAdSize(controller.context, (data!!["size_width"] as Double).toFloat())
        generateAdView(context, data)
        controller.loadRequested = { load() }
        load()
    }

    private fun load() {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdImpression() {
                super.onAdImpression()
                controller.channel.invokeMethod("onAdImpression", null)
            }

            override fun onAdClicked() {
                super.onAdClicked()
                controller.channel.invokeMethod("onAdClicked", null)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                controller.channel.invokeMethod("onAdFailedToLoad", hashMapOf("errorCode" to error.code))
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                controller.channel.invokeMethod("onAdLoaded", adView.adSize.height)
            }
        }
    }

    private fun generateAdView(context: Context, data: Map<String?, Any?>?) {
        adView = AdView(context)
        val width: Int = (data!!["size_width"] as Double).toInt()
        val height: Int = (data["size_height"] as Double).toInt()
        if (height != -1) adView.adSize = AdSize(width, height)
        else adView.adSize = adSize
        adView.adUnitId = data["unitId"] as String
    }

    override fun getView(): View {
        return adView
    }

    override fun dispose() {
        adView.destroy()
    }
}