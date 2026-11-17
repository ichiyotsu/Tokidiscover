package com.tokidiscover.app.data

import android.Manifest
import android.webkit.PermissionRequest

/**
 * Pure rules for WebView permission requests (camera, microphone, location).
 *
 * The WebView asks the app whenever a page wants a protected resource. When the
 * app already holds the Android permission, the request is granted right away;
 * otherwise the shell shows its own prompt and asks the system, and these rules
 * decide what may be granted from the outcome.
 */
object WebPermissionPolicy {

    /** Android permissions a web [resources] request needs. Empty when nothing is gated. */
    fun systemPermissionsFor(resources: Array<String>): Set<String> =
        resources.mapNotNull { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                else -> null
            }
        }.toSet()

    /** The subset of [resources] that may be granted given [grantedPermissions]. */
    fun grantableResources(
        resources: Array<String>,
        grantedPermissions: Set<String>,
    ): Array<String> =
        resources.filter { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                    Manifest.permission.CAMERA in grantedPermissions
                PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                    Manifest.permission.RECORD_AUDIO in grantedPermissions
                else -> false
            }
        }.toTypedArray()

    /** Human-readable names for the requested resources, for the prompt dialog. */
    fun resourceLabels(resources: Array<String>): List<String> =
        resources.mapNotNull { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> "摄像头"
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> "麦克风"
                else -> null
            }
        }.distinct()

    /** Host part of an origin URL, falling back to the full origin when unparseable. */
    fun originHost(origin: String): String {
        val authority = origin.substringAfter("://", origin).substringBefore("/").substringBefore("?")
        if (authority.isBlank()) return origin
        val host = if (authority.startsWith("[")) {
            val end = authority.indexOf("]")
            if (end >= 0) authority.substring(0, end + 1) else authority
        } else {
            authority.substringBefore(":")
        }
        return host.takeIf { it.isNotBlank() } ?: origin
    }
}
