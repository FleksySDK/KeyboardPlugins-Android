package co.thingthing.fleksyapps.mediashare

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.children
import co.thingthing.fleksyapps.mediashare.databinding.ItemAdViewBinding
import co.thingthing.fleksyapps.mediashare.models.MediaShareResponse

@SuppressLint("ViewConstructor")
class AdViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    adContent: MediaShareResponse.Advertisement,
    val maxWidth: Int,
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ItemAdViewBinding =
        ItemAdViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.adView.setContent(adContent)
    }

    // Calculate the aspect ratio from the Advertisement object
    private val aspectRatio: Float = adContent.width.toFloat() / adContent.height.toFloat()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val calculatedHeight = MeasureSpec.getSize(heightMeasureSpec)
        val calculatedWidth = (calculatedHeight * aspectRatio).toInt()

        val height: Int
        val width: Int
        if (calculatedWidth <= maxWidth) {
            height = calculatedHeight
            width = calculatedWidth
        } else {
            /**
             * If the ad’s width is greater than the screen width,
             * then we resize the ad (preserving the aspect ratio),
             * so that its width matches the screen
             */
            height = (maxWidth / aspectRatio).toInt()
            width = maxWidth
        }

        val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)

        children.first().measure(widthSpec, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val child = children.first()
        child.layout(0, 0, child.measuredWidth, child.measuredHeight)
    }
}

