package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.marshalchen.ultimaterecyclerview.dragsortadapter.DragSortAdapter
import com.marshalchen.ultimaterecyclerview.dragsortadapter.NoForegroundShadowBuilder
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.RowTabBinding
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.model.SnsTabRepository
import jp.gr.java_conf.miwax.troutoss.model.entity.SnsTab


/**
 * Created by Tomoya Miwa on 2017/06/17.
 * タブカスタマイズ用のDrag対応Adapter
 */

open class TabDragAdapter(recyclerView: RecyclerView, tabs: MutableList<SnsTab>? = null) :
        DragSortAdapter<TabDragAdapter.ViewHolder>(recyclerView) {

    private val tabRepository = SnsTabRepository(MastodonHelper())
    private val context = recyclerView.context
    val tabs: MutableList<SnsTab>

    init {
        this.tabs = tabs ?: tabRepository.findAllSorted().toMutableList()
    }

    fun getReIndexedTabs(): List<SnsTab> {
        return tabs.toMutableList().apply {
            forEachIndexed { index, snsTab -> snsTab.position = index }
        }
    }

    fun add(tab: SnsTab) {
        tabs.add(tab)
        notifyItemInserted(tabs.lastIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_tab, parent, false)
        return ViewHolder(this, v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tab = tabs[position]
        holder.binding.tabName.text = tabRepository.getTitleOf(tab)
        holder.binding.tabName.run {
            val drawables = compoundDrawables
            setCompoundDrawablesWithIntrinsicBounds(
                    getTabIconOf(tab), drawables[1], drawables[2], drawables[3]
            )
        }
    }

    private fun getTabIconOf(tab: SnsTab): Drawable {
        val drawableId = when (tab.type) {
            SnsTab.TabType.MASTODON_HOME -> R.drawable.home
            SnsTab.TabType.MASTODON_FEDERATED -> R.drawable.public_earth
            SnsTab.TabType.MASTODON_NOTIFICATIONS -> R.drawable.notifications
            SnsTab.TabType.MASTODON_FAVOURITES -> R.drawable.favourite_white
            SnsTab.TabType.MASTODON_LOCAL -> R.drawable.group
            else -> R.drawable.tab
        }
        return AppCompatResources.getDrawable(context, drawableId)!!
    }

    override fun getItemId(position: Int): Long {
        return tabs[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun getPositionForId(id: Long): Int {
        return tabs.find { it.hashCode().toLong() == id }?.
                let { tabs.indexOf(it) } ?: -1
    }

    override fun move(fromPosition: Int, toPosition: Int): Boolean {
        tabs.add(toPosition, tabs.removeAt(fromPosition))
        return true
    }

    class ViewHolder(adapter: DragSortAdapter<*>, itemView: View) :
            DragSortAdapter.ViewHolder(adapter, itemView) {

        val binding: RowTabBinding = DataBindingUtil.bind(itemView)

        init {
            binding.tabName.apply {
                setOnLongClickListener {
                    startDrag()
                    true
                }
                setOnTouchListener { _, event ->
                    val DRAWABLE_RIGHT = 2
                    if (event.action == MotionEvent.ACTION_DOWN &&
                            event.rawX >= (this.right - this.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                        startDrag()
                        true
                    } else {
                        false
                    }
                }
            }
        }

        override fun getShadowBuilder(itemView: View, touchPoint: Point): View.DragShadowBuilder {
            return NoForegroundShadowBuilder(itemView, touchPoint)
        }
    }
}