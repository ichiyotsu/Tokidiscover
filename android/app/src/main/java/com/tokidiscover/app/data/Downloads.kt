package com.tokidiscover.app.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil

data class DownloadStart(val fileName: String, val url: String, val totalBytes: Long, val status: String)

/** Hands a WebView download to the system DownloadManager so the file is actually saved. */
object Downloads {
    fun enqueue(
        context: Context,
        url: String,
        userAgent: String,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
    ): DownloadStart {
        val name = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
            .addRequestHeader("User-Agent", userAgent.ifBlank { "TokiDiscover" })
        if (!mimeType.isNullOrBlank()) request.setMimeType(mimeType)
        runCatching { CookieManager.getInstance().getCookie(url) }
            .getOrNull()
            ?.let { request.addRequestHeader("Cookie", it) }
        context.getSystemService(DownloadManager::class.java).enqueue(request)
        return DownloadStart(name, url, contentLength, "已交给系统下载")
    }
}
