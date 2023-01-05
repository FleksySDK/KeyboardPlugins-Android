package co.thingthing.fleksyapps.base

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import co.thingthing.fleksyapps.core.AppTheme
import io.reactivex.subjects.Subject

abstract class BaseViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    constructor(parent: ViewGroup, @LayoutRes layout: Int) : this(inflate(parent, layout))

    private lateinit var viewModel: T

    open fun bind(viewModel: T) {
        this.viewModel = viewModel
    }

    fun setClickSubject(subject: Subject<T>) {
        itemView.setOnClickListener { subject.onNext(viewModel) }
    }

    companion object {
        private fun inflate(parent: ViewGroup, @LayoutRes layout: Int) =
            LayoutInflater
                .from(ContextThemeWrapper(parent.context, androidx.appcompat.R.style.Theme_AppCompat))
                .inflate(layout, parent, false)
    }
}
