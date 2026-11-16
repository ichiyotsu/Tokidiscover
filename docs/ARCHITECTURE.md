# TokiDiscover 架构

## 引擎选型（各端原生 Web 引擎，不自研内核）

| 平台 | 引擎 | 说明与限制 |
|---|---|---|
| Android | 系统 WebView（每个标签保留一个实例，关标签才销毁） | Chromium 系、随系统/Play 更新；无扩展。查找用 `findAllAsync`，下载交给系统 DownloadManager。最低 Android 16（minSdk 36） |
| iOS | WKWebView（UIViewRepresentable） | 系统 WebKit；进度 KVO `estimatedProgress`；无扩展；下载无系统级管理 UI |
| Windows | WebView2（Evergreen） | 需 Evergreen 运行时；find = `CoreWebView2.Find`；下载 = `DownloadStarting` 事件 |

## 核心可测逻辑（三端同构 + 各自单测，命名一致）

- `UrlNormalizer.normalize(input, engine, customTemplate)` → `Url / Search / NewTab / Blocked`
  - 空白或 `about:blank`（及其他 `about:`）→ 新标签页；带空格 → 搜索；
  - `http://`/`https://` 原样；无空格且 host 含点（或 localhost）→ 补 `https://`；
  - `javascript:`/`data:`/`vbscript:`/`file:`/`blob:` → 拦截（Blocked），导航层二次校验 `isSafeScheme`。
- `SearchEngine`：Google / Bing（默认，与 Windows 生态一致，设置里可改）/ DuckDuckGo / Baidu / Custom（模板含 `{query}`，查询 URL-encode）。
- 测试：Android `UrlNormalizerTest`（JUnit）、iOS `TokiDiscoverCore` 的 `UrlNormalizerTests`（XCTest / `swift test`）、
  Windows `TokiDiscover.Tests`（xUnit）— 用例一一对应，改逻辑必须三端同步改测试。

## 标签与隐私模型

- Tab 模型三端一致（id/url/title/isPrivate/canGoBack/canGoForward/progress）；每标签一个 Web 引擎实例（内存管理：关标签即释放）。
- 无痕标签：`isPrivate=true` → 不写历史、不写搜索记录（本产品无独立搜索记录存储，omnibox 历史即网页历史）。
  Android 对无痕页使用 `LOAD_NO_CACHE`，并关闭该 WebView 的第三方 Cookie；**不**调用进程级 `CookieManager.setAcceptCookie(false)`，否则普通标签也会失去 Cookie。系统 WebView 没有独立 Cookie 罐，这是平台限制。
  iOS 用 `WKWebsiteDataStore.nonPersistent()`，Windows 暂未做独立 WebView2 环境隔离（见限制）。

## 持久化

- Android：SharedPreferences + JSON（BrowserStore/SettingsStore，API 稳定，可替换为 DataStore 实现）。
- iOS：UserDefaults + JSON（AppModel）。
- Windows：内存 + LocalSettings/LocalFolder 的接线点在 BrowserService（0.1.0 运行时以内存为主，重启不恢复书签/历史 — 见限制）。

## 已知限制（0.1.0，诚实清单）

1. Android 下载：WebView 的 DownloadListener 会交给系统 DownloadManager，文件进系统「下载」目录，书库只保留记录。下载管理器被关掉或链接无法保存时，记录状态为「未能开始」。
2. iOS 下载：仅模型 + 空状态，无真实下载管理；分享为系统分享的接线点在后续补齐（当前 RootView 未挂分享按钮）。
3. Windows：桌面 UA 开关为占位（WebView2 无内置 UA 切换 API，需拦截请求改 header，后续做）；
   清除 Cookie/缓存按钮需要活动的 WebView2 Profile 跨页面接线，当前会给出提示而非假装已清除；
   分享走剪贴板兜底（DataTransferManager 需要窗口句柄 interop）；无痕标签未做独立 WebView2 Environment 隔离（历史层面已隔离）。
4. 三端均无广告拦截、扩展、同步、密码管理；长按菜单/上下文菜单为系统默认，未做自定义。
5. 检查更新读 GitHub Releases（仓库 `ichiyotsu/tokidiscover` 尚待用户手动创建）；版本解析优先安装包文件名、其次 tag（沿用 Tokilive/tokimusic 修过的误报修复）。
6. 本地未编译项见 README「构建说明」。
