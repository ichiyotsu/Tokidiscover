package com.tokidiscover.app.data
import android.content.Context
/** Browser settings persisted in SharedPreferences. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("tokidiscover_settings", Context.MODE_PRIVATE)
    var searchEngine: String get() = prefs.getString("engine", SearchEngine.Default.name) ?: SearchEngine.Default.name
        set(v) { prefs.edit().putString("engine", v).apply() }
    var customTemplate: String get() = prefs.getString("custom_template", "") ?: ""
        set(v) { prefs.edit().putString("custom_template", v).apply() }
    var themeMode: String get() = prefs.getString("theme_mode", "System") ?: "System"
        set(v) { prefs.edit().putString("theme_mode", v).apply() }
    var desktopUa: Boolean get() = prefs.getBoolean("desktop_ua", false); set(v) { prefs.edit().putBoolean("desktop_ua", v).apply() }
    var javaScriptEnabled: Boolean get() = prefs.getBoolean("js", true); set(v) { prefs.edit().putBoolean("js", v).apply() }
    var homepage: String get() = prefs.getString("homepage", UrlNormalizer.NEW_TAB_URL) ?: UrlNormalizer.NEW_TAB_URL
        set(v) { prefs.edit().putString("homepage", v).apply() }
    var useDynamicColor: Boolean get() = prefs.getBoolean("dynamic_color", true)
        set(v) { prefs.edit().putBoolean("dynamic_color", v).apply() }
    var seedIndex: Int get() = prefs.getInt("seed_index", 0)
        set(v) { prefs.edit().putInt("seed_index", v).apply() }
    var reduceMotion: Boolean get() = prefs.getBoolean("reduce_motion", false)
        set(v) { prefs.edit().putBoolean("reduce_motion", v).apply() }
    var backdropBlur: Boolean get() = prefs.getBoolean("backdrop_blur", true)
        set(v) { prefs.edit().putBoolean("backdrop_blur", v).apply() }
    /** New-tab page: show the Speed Dial shortcuts row. */
    var showSpeedDial: Boolean get() = prefs.getBoolean("ntp_speed_dial", true)
        set(v) { prefs.edit().putBoolean("ntp_speed_dial", v).apply() }
    /** Bottom function bar layout as JSON (see FunctionBarConfig); blank = default. */
    var functionBarJson: String get() = prefs.getString("function_bar", "") ?: ""
        set(v) { prefs.edit().putString("function_bar", v).apply() }
    fun engine(): SearchEngine = runCatching { SearchEngine.valueOf(searchEngine) }.getOrDefault(SearchEngine.Default)
}
