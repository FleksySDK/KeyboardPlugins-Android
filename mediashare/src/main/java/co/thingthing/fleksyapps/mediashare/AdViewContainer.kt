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
    adContent: MediaShareResponse.Advertisement
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ItemAdViewBinding =
        ItemAdViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.adView.setContent(adContent)
    }

    // Calculate the aspect ratio from the Advertisement object
    private val aspectRatio: Float = adContent.width.toFloat() / adContent.height.toFloat()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val width = (height * aspectRatio).toInt()

        val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)

        children.first().measure(widthSpec, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val child = children.first()
        child.layout(0, 0, child.measuredWidth, child.measuredHeight)
    }
}

