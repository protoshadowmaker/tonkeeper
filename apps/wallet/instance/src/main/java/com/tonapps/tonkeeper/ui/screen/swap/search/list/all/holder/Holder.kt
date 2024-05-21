package com.tonapps.tonkeeper.ui.screen.swap.search.list.all.holder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.tonapps.tonkeeper.ui.screen.swap.search.list.all.TokenItem
import com.tonapps.uikit.list.BaseListHolder

abstract class Holder<I : TokenItem>(
    parent: ViewGroup,
    @LayoutRes resId: Int,
) : BaseListHolder<I>(parent, resId)