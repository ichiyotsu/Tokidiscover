package com.tokidiscover.app.data

import android.Manifest
import android.webkit.PermissionRequest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebPermissionPolicyTest {

    @Test
    fun videoMapsToCamera() {
        assertEquals(
            setOf(Manifest.permission.CAMERA),
            WebPermissionPolicy.systemPermissionsFor(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE)),
        )
    }

    @Test
    fun audioMapsToRecordAudio() {
        assertEquals(
            setOf(Manifest.permission.RECORD_AUDIO),
            WebPermissionPolicy.systemPermissionsFor(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE)),
        )
    }

    @Test
    fun unknownResourcesNeedNothing() {
        assertTrue(
            WebPermissionPolicy.systemPermissionsFor(
                arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID, "made-up"),
            ).isEmpty(),
        )
    }

    @Test
    fun grantableKeepsOnlySystemGranted() {
        val granted = WebPermissionPolicy.grantableResources(
            arrayOf(
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID,
            ),
            setOf(Manifest.permission.CAMERA),
        )
        assertArrayEquals(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE), granted)
    }

    @Test
    fun grantableIsEmptyWhenNothingGranted() {
        assertTrue(
            WebPermissionPolicy.grantableResources(
                arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE),
                emptySet(),
            ).isEmpty(),
        )
    }

    @Test
    fun labelsAreChineseAndDistinct() {
        assertEquals(
            listOf("摄像头", "麦克风"),
            WebPermissionPolicy.resourceLabels(
                arrayOf(
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                    PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID,
                ),
            ),
        )
    }

    @Test
    fun originHostStripsSchemePortAndPath() {
        assertEquals("example.com", WebPermissionPolicy.originHost("https://example.com:8443/path?q=1"))
        assertEquals("example.com", WebPermissionPolicy.originHost("http://example.com"))
        assertEquals("example.com", WebPermissionPolicy.originHost("example.com/page"))
        assertEquals("[2001:db8::1]", WebPermissionPolicy.originHost("https://[2001:db8::1]:443/"))
    }

    @Test
    fun originHostFallsBackToOrigin() {
        assertEquals("weird", WebPermissionPolicy.originHost("weird"))
        assertEquals("", WebPermissionPolicy.originHost(""))
        assertFalse(WebPermissionPolicy.originHost("https://").isBlank())
    }
}
