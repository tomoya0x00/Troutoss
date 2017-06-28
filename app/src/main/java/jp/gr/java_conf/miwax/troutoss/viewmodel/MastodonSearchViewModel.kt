package jp.gr.java_conf.miwax.troutoss.viewmodel

import com.sys1yagi.mastodon4j.api.entity.Account
import com.sys1yagi.mastodon4j.api.entity.Results
import com.sys1yagi.mastodon4j.rx.RxPublic
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import jp.gr.java_conf.miwax.troutoss.messenger.Messenger
import jp.gr.java_conf.miwax.troutoss.messenger.OpenUrlMessage
import jp.gr.java_conf.miwax.troutoss.messenger.ShowMastodonTimelineActivityMessage
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonAccountAdapter
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonHashTagAdapter
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonTimelineAdapter
import java.util.concurrent.TimeUnit

/**
 * Created by Tomoya Miwa on 2017/06/25.
 * Mastodon検索画面のViewModel
 */

class MastodonSearchViewModel(accountUuid: String) {

    val messenger = Messenger()

    val queryProcessor = PublishProcessor.create<String>()!!
    val results: Flowable<Results>?
    val hashtagAdapter: MastodonHashTagAdapter
    val accountAdapter: MastodonAccountAdapter

    private val client = MastodonHelper().createAuthedClientOf(accountUuid)
    private val rxPublic = client?.let { RxPublic(it) }

    init {
        results = rxPublic?.let { public ->
            queryProcessor
                    .observeOn(Schedulers.computation())
                    .throttleLast(300, TimeUnit.MILLISECONDS)
                    .flatMap { public.getSearch(it, true).toFlowable() }
                    .share()
        }

        hashtagAdapter = object : MastodonHashTagAdapter(
                results?.let { it.map { it.hashtags } } ?: Flowable.just(arrayListOf())
        ) {
            override fun onClickHashTag(hashtag: String) {
                messenger.send(ShowMastodonTimelineActivityMessage(
                        MastodonTimelineAdapter.Timeline.FEDERATED_TAG,
                        accountUuid, hashtag
                ))
            }
        }

        accountAdapter = object : MastodonAccountAdapter(
                results?.let { it.map { it.accounts } } ?: Flowable.just(arrayListOf())
        ) {
            override fun onClickAccount(account: Account) {
                messenger.send(OpenUrlMessage(account.url))
            }
        }
    }

    fun onQueryTextSubmit(query: String) {
        queryProcessor.onNext(query)
    }

    fun onQueryTextChange(query: String) {
        queryProcessor.onNext(query)
    }
}
