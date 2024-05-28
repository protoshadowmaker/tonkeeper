package com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.holder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.SuggestedTokenItem
import com.tonapps.uikit.list.BaseListHolder

abstract class Holder<I : SuggestedTokenItem>(
    parent: ViewGroup,
    @LayoutRes resId: Int,
) : BaseListHolder<I>(parent, resId)