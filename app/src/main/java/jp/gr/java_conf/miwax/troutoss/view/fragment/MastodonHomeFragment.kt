package jp.gr.java_conf.miwax.troutoss.view.fragment


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.marshalchen.ultimaterecyclerview.ui.divideritemdecoration.HorizontalDividerItemDecoration
import jp.gr.java_conf.miwax.troutoss.R
import jp.gr.java_conf.miwax.troutoss.databinding.FragmentMastodonHomeBinding
import jp.gr.java_conf.miwax.troutoss.model.MastodonHelper
import jp.gr.java_conf.miwax.troutoss.view.adapter.MastodonHomeAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [MastodonHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MastodonHomeFragment : Fragment() {

    private var accountUuid: String? = null
    private var option: String? = null

    lateinit private var binding: FragmentMastodonHomeBinding
    private var adapter: MastodonHomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountUuid = arguments?.getString(ARG_ACCOUNT_UUID)
        option = arguments?.getString(ARG_OPTION)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mastodon_home, container, false)

        val helper = MastodonHelper(context)
        val client = accountUuid?.let { helper.createAuthedClientOf(it) }
        adapter = client?.let { MastodonHomeAdapter(it, context) }

        binding.timeline.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration((HorizontalDividerItemDecoration.Builder(context).build()))
            setDefaultOnRefreshListener { onRefresh() }
            setOnLoadMoreListener { _, _ -> onLoadMoreOld() }
            setLoadMoreView(R.layout.center_progressbar)
            setAdapter(this@MastodonHomeFragment.adapter)
        }

        onRefresh()

        return binding.root
    }

    private fun onRefresh() = launch(UI) {
        binding.timeline.setRefreshing(true)
        try {
            adapter?.refresh()?.await()
        } catch (e: Exception) {
            Timber.e("refresh failed: %s", e)
            Toast.makeText(getContext(), R.string.error_comm, Snackbar.LENGTH_SHORT).show()
        } finally {
            binding.timeline.setRefreshing(false)
        }
    }

    private fun onLoadMoreOld() = launch(UI) {
        // TODO: 通信途絶時に何度も実行されてしまうのを修正
        try {
            adapter?.loadMoreOld()?.await()
        } catch (e: Exception) {
            Timber.e("loadMoreOld failed: %s", e)
            Toast.makeText(getContext(), R.string.error_comm, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val ARG_ACCOUNT_UUID = "account_uuid"
        private val ARG_OPTION = "option"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param accountUuid
         * *
         * @param option
         * *
         * @return A new instance of fragment MastodonHomeFragment.
         */
        fun newInstance(accountUuid: String, option: String): MastodonHomeFragment {
            val fragment = MastodonHomeFragment()
            val args = Bundle()
            args.putString(ARG_ACCOUNT_UUID, accountUuid)
            args.putString(ARG_OPTION, option)
            fragment.arguments = args
            return fragment
        }
    }

}
