package jp.gr.java_conf.miwax.troutoss.view.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sys1yagi.mastodon4j.api.entity.Account
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.RowAccountBinding
import jp.gr.java_conf.miwax.troutoss.extension.actualAvatarUrl
import jp.gr.java_conf.miwax.troutoss.extension.getNonEmptyName
import jp.gr.java_conf.miwax.troutoss.extension.imageUrl
import jp.gr.java_conf.miwax.troutoss.extension.setHtml

/**
 * Created by Tomoya Miwa on 2017/06/26.
 * Mastodonのハッシュ用アダプター
 */

class MastodonAccountAdapter(private val accountsFlow: Flowable<List<Account>>) :
        RecyclerView.Adapter<MastodonAccountAdapter.ViewHolder>() {

    var accounts: List<Account> = arrayListOf()
    val disposable = CompositeDisposable()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        disposable.add(
                accountsFlow
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            accounts = it
                            notifyDataSetChanged()
                        }
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposable.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_account, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]
        holder.binding.apply {
            avatar.imageUrl(account.actualAvatarUrl())
            displayName.text = account.getNonEmptyName()
            userName.text = "@${account.acct}"
            userNote.setHtml(account.note)
        }
    }

    override fun getItemCount(): Int = accounts.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowAccountBinding = DataBindingUtil.bind(itemView)
    }
}
