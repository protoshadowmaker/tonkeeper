package com.tonapps.tonkeeper.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.gone
import uikit.extensions.setEndDrawable
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.useAttributes
import uikit.extensions.visible
import uikit.widget.LoaderView

class LineInfoSimpleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    var position: ListCell.Position = ListCell.Position.SINGLE
        set(value) {
            field = value
            background = value.drawable(context)
        }

    private val titleView: AppCompatTextView
    private val valueView: AppCompatTextView
    private val loaderView: LoaderView

    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    var value: CharSequence?
        get() = valueView.text
        set(value) {
            valueView.text = value
        }

    init {
        inflate(context, R.layout.view_line_info_simple, this)
        setPaddingHorizontal(context.getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        orientation = HORIZONTAL
        if (minimumHeight == 0) {
            minimumHeight = context.getDimensionPixelSize(uikit.R.dimen.itemHeight)
        }

        titleView = findViewById(R.id.title)
        valueView = findViewById(R.id.value)
        loaderView = findViewById(R.id.loader)

        context.useAttributes(attrs, R.styleable.LineInfoSimpleView) {
            titleView.text = it.getString(R.styleable.LineInfoSimpleView_android_title)
            val iconResId = it.getResourceId(
                R.styleable.LineInfoSimpleView_android_icon,
                ResourcesCompat.ID_NULL
            )
            if (iconResId != ResourcesCompat.ID_NULL) {
                titleView.setEndDrawable(iconResId)
            }
        }
    }

    fun setLoading() {
        valueView.gone()
        loaderView.visible()
    }

    fun setDefault() {
        valueView.visible()
        loaderView.gone()
    }

    fun setData(value: CharSequence) {
        setDefault()
        valueView.text = value
    }

}