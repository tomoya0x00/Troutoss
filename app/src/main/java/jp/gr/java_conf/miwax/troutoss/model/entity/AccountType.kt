package jp.gr.java_conf.miwax.troutoss.model.entity

/**
 * Created by Tomoya Miwa on 2017/05/15.
 * アカウントタイプ
 */

enum class AccountType {
    MASTODON {
        override val maxStatusLen: Int
            get() = 500
    },
    UNKNOWN {
        override val maxStatusLen: Int
            get() = 0
    };

    abstract val maxStatusLen: Int
}

