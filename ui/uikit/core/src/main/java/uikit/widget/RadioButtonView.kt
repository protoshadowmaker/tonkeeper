package uikit.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonTertiaryBackgroundColor
import com.tonapps.uikit.icon.UIKitIcon
import uikit.extensions.dp
import uikit.extensions.getDrawable

class RadioButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private companion object {
        private val size = 28.dp
    }

    var doOnCheckedChanged: ((Boolean) -> Unit)? = null

    private val defaultDrawable: Drawable by lazy {
        getDrawable(UIKitIcon.ic_radio_default).apply {
            colorFilter = PorterDuffColorFilter(
                context.buttonTertiaryBackgroundColor, PorterDuff.Mode.SRC_IN
            )
            setBounds(0, 0, size, size)
        }
    }

    private val selectedDrawable: Drawable by lazy {
        getDrawable(UIKitIcon.ic_radio_selected).apply {
            colorFilter = PorterDuffColorFilter(
                context.buttonPrimaryBackgroundColor, PorterDuff.Mode.SRC_IN
            )
            setBounds(0, 0, size, size)
        }
    }

    var checked: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                doOnCheckedChanged?.invoke(value)
                invalidate()
            }
        }

    init {
        setOnClickListener {
            toggle()
        }
    }

    fun toggle() {
        checked = !checked
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (checked) {
            selectedDrawable.draw(canvas)
        } else {
            defaultDrawable.draw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(size, size)
    }

    override fun hasOverlappingRendering() = false
}