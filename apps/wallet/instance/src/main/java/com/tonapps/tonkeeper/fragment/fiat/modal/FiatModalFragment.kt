package com.tonapps.tonkeeper.fragment.fiat.modal

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.dialog.fiat.list.MethodAdapter
import com.tonapps.tonkeeper.koin.settingsRepository
import kotlinx.coroutines.launch
import uikit.base.BaseFragment
import uikit.widget.HeaderView

class FiatModalFragment: BaseFragment(R.layout.dialog_fiat), BaseFragment.Modal {

    companion object {
        fun newInstance() = FiatModalFragment()
    }

    private val adapter = MethodAdapter {

    }

    private lateinit var headerView: HeaderView
    private lateinit var listView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)!!
        headerView.setBackgroundColor(Color.TRANSPARENT)
        // headerView.doOnCloseClick = { pickCountry() }
        // headerView.doOnActionClick = { dismiss() }

        listView = view.findViewById(R.id.list)!!
        listView.adapter = adapter
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            requestItems()
        }
    }


    private suspend fun requestItems() {
        val country = context?.settingsRepository?.country!!
        val items = com.tonapps.tonkeeper.App.fiat.getMethods(country)

        adapter.submit(items)
    }
}