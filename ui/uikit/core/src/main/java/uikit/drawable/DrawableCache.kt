package uikit.drawable

import android.graphics.drawable.Drawable

class DrawableCache {
    private val cache: MutableMap<String, Drawable> = mutableMapOf()

    fun getOrCreate(key: String, builder: () -> Drawable): Drawable {
        return cache.getOrPut(key, builder)
    }
}