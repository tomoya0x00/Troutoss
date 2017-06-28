package jp.gr.java_conf.miwax.troutoss.viewmodel

import com.sys1yagi.mastodon4j.api.entity.Results
import com.sys1yagi.mastodon4j.rx.RxPublic
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonAccountAdapter
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonHashTagAdapter
import java.util.concurrent.TimeUnit

/**
 * Created by Tomoya Miwa on 2017/06/25.
 * Mastodon検索画面のViewModel
 */

class MastodonSearchViewModel(accountUuid: String) {

    val queryProcessor = PublishProcessor.create<String>()!!
    val client = MastodonHelper().createAuthedClientOf(accountUuid)
    val rxPublic = client?.let { RxPublic(it) }
    val results: Flowable<Results>?
    val hashtagAdapter: MastodonHashTagAdapter
    val accountAdapter: MastodonAccountAdapter

    init {
        results = rxPublic?.let { public ->
            queryProcessor
                    .observeOn(Schedulers.computation())
                    .throttleLast(300, TimeUnit.MILLISECONDS)
                    .flatMap { public.getSearch(it, true).toFlowable() }
                    .share()
        }

        hashtagAdapter = MastodonHashTagAdapter(
                results?.let { it.map { it.hashtags } } ?: Flowable.just(arrayListOf()))

        accountAdapter = MastodonAccountAdapter(
                results?.let { it.map { it.accounts } } ?: Flowable.just(arrayListOf())
        )
    }

    fun onQueryTextSubmit(query: String) {

    }

    fun onQueryTextChange(query: String) {
        queryProcessor.onNext(query)
    }
}
