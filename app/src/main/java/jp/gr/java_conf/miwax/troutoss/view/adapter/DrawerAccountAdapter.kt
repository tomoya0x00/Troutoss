package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.ContentDrawerAccountBinding
import jp.gr.java_conf.miwax.troutoss.model.entity.MastodonAccount
import jp.gr.java_conf.miwax.troutoss.viewmodel.DrawerAccountViewModel

/**
 * Created by Tomoya Miwa on 2017/06/04.
 * ドロワーのアカウント一覧用アダプター
 */

open class DrawerAccountAdapter(val realm: Realm) :
        RealmRecyclerViewAdapter<MastodonAccount, DrawerAccountAdapter.ViewHolder>(
                realm.where(MastodonAccount::class.java).findAll(), true
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_drawer_account, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = getItem(position)
        account?.let {
            holder.binding.viewModel = DrawerAccountViewModel(it)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ContentDrawerAccountBinding = DataBindingUtil.bind(itemView)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }
}

