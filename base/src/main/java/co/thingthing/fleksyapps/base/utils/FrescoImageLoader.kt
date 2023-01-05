package co.thingthing.fleksyapps.base.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import co.thingthing.fleksyapps.base.R
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder

class FrescoImageLoader {
    private companion object {
        private const val FADE_DURATION = 150
        private const val ALPHA = 80
    }

    fun load(
        view: SimpleDraweeView,
        @ColorInt background: Int,
        @ColorInt foreground: Int,
        width: Float,
        height: Float,
        url: String,
        lowResUrl: String?
    ) {
        with(view) {
            hierarchy.fadeDuration = FADE_DURATION
            hierarchy.roundingParams = RoundingParams
                .fromCornersRadius(resources.getDimension(R.dimen.base_card_radius))
                .setOverlayColor(background)
            hierarchy.setPlaceholderImage(
                ColorDrawable(
                    Color.argb(ALPHA, foreground.red, foreground.green, foreground.blue)
                )
            )
            aspectRatio = width / height
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setLowResImageRequest(imageRequest(lowResUrl))
                .setImageRequest(imageRequest(url))
                .setOldController(controller)
                .setAutoPlayAnimations(true)
                .build()
        }
    }

    private fun imageRequest(url: String?) =
        url?.let {
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(it))
                .setProgressiveRenderingEnabled(true)
                .build()
        }
}
