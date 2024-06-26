package uikit.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.tonapps.uikit.color.backgroundContentTintColor
import uikit.R
import uikit.extensions.dp

class WordHintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val wordLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f)

    var doOnClickText: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        visibility = GONE
        setBackgroundColor(context.backgroundContentTintColor)
    }

    fun hide() {
        visibility = GONE
    }

    fun setWords(words: List<String>) {
        visibility = VISIBLE
        removeAllViews()

        words.forEach { word ->
            val wordView = createWordView()
            wordView.text = word
            wordView.setOnClickListener {
                doOnClickText?.invoke(word)
            }
            addView(wordView, wordLayoutParams)
        }
    }

    private fun createWordView(): AppCompatTextView {
        return inflate(context, R.layout.view_word_suggestion, null) as AppCompatTextView
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(52.dp, MeasureSpec.EXACTLY))
    }

}