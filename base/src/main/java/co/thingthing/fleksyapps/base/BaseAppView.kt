package co.thingthing.fleksyapps.base

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import kotlin.math.max

class BaseAppView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var startPosition = 0f
    private var position = 0f
    private var startHeight: Int? = null

    private val density = context.resources.displayMetrics.density
    private val dragOffset = 10 * density

    var onHideGesture: (() -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // TODO: Find better way?
        if (startHeight == null && height > 0) {
            startHeight = height
        }
    }

    private fun translateTo(distance: Float) {
        val startHeight = startHeight ?: return

        position = max(0f, distance)

        updateLayoutParams<LayoutParams> {
            height = max(0, startHeight - position.toInt())
        }
    }
}
