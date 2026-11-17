package com.tokidiscover.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SchemePolicyTest {
    @Test
    fun webUrlsLoadInTheWebView() {
        assertEquals(SchemeAction.LoadInWebView, SchemePolicy.classify("https://example.com"))
        assertEquals(SchemeAction.LoadInWebView, SchemePolicy.classify("http://example.com/a?b=1"))
        assertEquals(SchemeAction.LoadInWebView, SchemePolicy.classify("HTTPS://EXAMPLE.COM"))
        assertEquals(SchemeAction.LoadInWebView, SchemePolicy.classify("about:blank"))
        assertEquals(SchemeAction.LoadInWebView, SchemePolicy.classify("/relative/path"))
    }

    @Test
    fun dangerousSchemesStayBlocked() {
        assertEquals(SchemeAction.Blocked, SchemePolicy.classify("javascript:alert(1)"))
        assertEquals(SchemeAction.Blocked, SchemePolicy.classify("data:text/html,<h1>x</h1>"))
        assertEquals(SchemeAction.Blocked, SchemePolicy.classify("vbscript:msgbox(1)"))
        assertEquals(SchemeAction.Blocked, SchemePolicy.classify("file:///etc/passwd"))
        assertEquals(SchemeAction.Blocked, SchemePolicy.classify("blob:https://example.com/uuid"))
        assertEquals(SchemeAction.Blocked, SchemePolicy.classify("  JAVASCRIPT:alert(1)"))
    }

    @Test
    fun appSchemesGoToAnExternalApp() {
        assertEquals(SchemeAction.OpenExternally, SchemePolicy.classify("tel:+8613800000000"))
        assertEquals(SchemeAction.OpenExternally, SchemePolicy.classify("mailto:a@example.com"))
        assertEquals(SchemeAction.OpenExternally, SchemePolicy.classify("sms:+8613800000000"))
        assertEquals(SchemeAction.OpenExternally, SchemePolicy.classify("intent://scan/#Intent;scheme=zxing;end"))
        assertEquals(SchemeAction.OpenExternally, SchemePolicy.classify("weixin://dl/scan"))
        assertEquals(SchemeAction.OpenExternally, SchemePolicy.classify("alipays://platformapi/startapp"))
    }
}
