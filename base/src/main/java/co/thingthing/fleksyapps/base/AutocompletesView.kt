package co.thingthing.fleksyapps.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import co.thingthing.fleksyapps.core.AppTheme
import kotlinx.android.synthetic.main.layout_autocomplete.view.*

class AutocompletesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var listener: AutocompleteListener? = null

    fun onReceiveAutocompletes(theme: AppTheme, autocompletes: List<BaseAutocomplete>) {
        removeAutocompletes()
        autocompletes.forEachIndexed { i, autocomplete ->
            if (i < MAX_AUTOCOMPLETE) {
                addView(LayoutInflater.from(context).inflate(R.layout.layout_autocomplete, this, false).apply {
                    autocompleteBackground.setBackgroundColor(theme.background)
                    autocompleteIcon.setColorFilter(theme.foreground)
                    autocompleteValue.apply {
                        setTextColor(theme.foreground)
                        text = autocomplete.value
                    }
                    setOnClickListener { listener?.onClickAutocomplete(autocomplete) }
                })
            }
        }
    }

    fun removeAutocompletes() {
        removeAllViews()
    }

    fun setListener(listener: AutocompleteListener) {
        this.listener = listener
    }

    interface AutocompleteListener {
        fun onClickAutocomplete(autocomplete: BaseAutocomplete)
    }

    companion object {
        private const val MAX_AUTOCOMPLETE = 4
    }
}
