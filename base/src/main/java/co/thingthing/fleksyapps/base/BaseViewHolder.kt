package co.thingthing.fleksyapps.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.Subject

abstract class BaseViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private lateinit var viewModel: T

    open fun bind(viewModel: T) {
        this.viewModel = viewModel
    }

    fun setClickSubject(subject: Subject<T>) {
        itemView.setOnClickListener { subject.onNext(viewModel) }
    }

    open fun onRecycled() {}

    companion object {
        val DEFAULT_CONTENT_TYPES = listOf("image/webp", "video/mp4", "image/gif", "image/jpeg")
    }
}
