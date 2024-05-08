package uikit.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import com.tonapps.uikit.color.backgroundContentColor
import com.tonapps.uikit.list.ListCell
import uikit.drawable.CellBackgroundDrawable

fun ListCell.Position.drawable(
    context: Context,
    backgroundColor: Int = context.backgroundContentColor
): Drawable {
    return CellBackgroundDrawable.create(context, this, backgroundColor)
}

fun ListCell.Position.cacheKey(
    backgroundColor: Int
): String {
    return "${backgroundColor}_${this.value}"
}