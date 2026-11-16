# TokiUI 快照

这个目录是 [TokiUI](https://github.com/ichiyotsu/TokiUI) `main` 的只读快照，给公开仓库的 CI 用。TokiDiscover 不能在 CI 里用 `../../TokiUI` 这种兄弟路径，因为 TokiUI 是私有仓库。

- 来源提交：`e2bec809da4678b8fcfc45109607fd5e7e341134`（TokiStudio 0.1.2 当时的 `main`；语义 token 版本仍是 `0.2.0-b1`）
- 包含：`src/main/kotlin/com/tokiui/semantic/`（官方解析器，不改逻辑）和 `tokens/*.json`（官方语义 token 与场景清单）
- 不包含：TokiStudio 的界面代码。Discover 自己的界面只调用 `SemanticTokens.parse`，不把色值抄进 Kotlin。

刷新：从 TokiUI `main` 原样覆盖上面两类文件，更新本说明里的提交号。不要手改解析器或 JSON，也不要在应用源码里另写一套色板。
