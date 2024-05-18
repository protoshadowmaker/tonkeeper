package com.tonapps.tonkeeper.ui.screen.token.list.holder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.tonapps.tonkeeper.ui.screen.token.list.Item
import com.tonapps.uikit.list.BaseListHolder

abstract class Holder<I: Item>(
    parent: ViewGroup,
    @LayoutRes resId: Int,
): BaseListHolder<I>(parent, resId)