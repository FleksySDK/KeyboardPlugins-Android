package co.thingthing.fleksyapps.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject


abstract class BaseAdapter<T : Any> : RecyclerView.Adapter<BaseViewHolder<T>>() {
    val items = mutableListOf<T>()

    val clickSubject: PublishSubject<T> = PublishSubject.create()

    abstract fun create(parent: ViewGroup, viewType: Int): BaseViewHolder<T>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        create(parent, viewType).apply {
            setClickSubject(clickSubject)
        }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun addAll(newItems: List<T>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, items.size)
    }
}
