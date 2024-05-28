package com.tonapps.wallet.data.core

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.tonapps.extensions.cacheFolder
import com.tonapps.extensions.file
import com.tonapps.extensions.folder
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toListParcel
import java.io.File
import kotlin.math.min

class GenericCacheSource(
    context: Context
) {

    private val rootFolder = context.cacheFolder("generic-cache")

    inline fun <reified T : Parcelable> get(
        name: String,
        block: (parcel: Parcel) -> T
    ): List<T> {
        val bytes = getData(name)
        if (bytes.isEmpty()) {
            return emptyList()
        }
        val l = bytes.toListParcel(block) ?: emptyList()
        return l
    }

    fun getData(
        name: String,
    ): ByteArray {
        val file = getFile(name)
        if (!file.exists() || file.length() == 0L) {
            return ByteArray(0)
        }
        return file.readBytes()
    }

    fun set(
        name: String,
        list: List<Parcelable>
    ) {
        val file = getFile(name)
        if (list.isEmpty()) {
            file.delete()
        } else {
            val maxListSize = min(list.size, 15)
            val bytes = list.subList(0, maxListSize).toByteArray()
            file.writeBytes(bytes)
        }
    }

    private fun getFolder(name: String): File {
        return rootFolder.folder(name)
    }

    private fun getFile(name: String): File {
        val folder = getFolder(name)
        val filename = "cache.dat"
        return folder.file(filename)
    }
}