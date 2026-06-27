# Google Play 上架剩余合规风险

更新时间：2026-06-27

本文只保留**按当前代码检查后仍然存在**、在 Google Play 上架前需要继续确认的问题。已经修正或已经移除的历史问题不再记录。

## 风险总览

| 优先级 | 风险项 | 当前状态 | 建议处理 |
|---|---|---|---|
| 高 | Data safety 仍需按 SDK 实际行为申报 | 项目仍集成 Google Ads、Google UMP、Firebase / google-services；仅看 Manifest 权限是否移除并不足以完成申报 | 以 `release/prod` 包为准，按广告、同意、崩溃/配置相关 SDK 的实际数据处理行为填写 Data safety |
| 中 | 冷启动 App Open Ad 的展示时机仍需 release 验证 | 冷启动时仍会 preload 并尝试展示 App Open Ad，但 splash 已不再依赖广告状态结束 | 用 `release/prod` 包验证是否只在真正冷启动触发、是否在错误时机打断用户、失败/超时时是否快速回到主界面 |
| 中 | 隐私政策正文与最终申报内容仍需做一次一致性核对 | 应用内隐私入口、多语言隐私页面、UMP 隐私选项入口都已落地，但 Play Console、Data safety、对外隐私文案仍需要最终一致 | 提审前逐项核对隐私政策页面、应用内入口文案、Play Console 隐私政策链接和 Data safety 描述 |

## 详细问题

### 1. Data safety 仍然是主要合规项

当前代码里可以确认的事实：

- 主 Manifest 已移除 `AD_ID`、`ACCESS_ADSERVICES_AD_ID` 等权限声明
- 应用仍集成 Google Mobile Ads
- 应用仍集成 Google UMP（用户同意管理）
- 工程仍接入 Firebase / `google-services`

这意味着：

1. 不能因为某些广告权限已从 Manifest 移除，就默认认为 Play Console 的 Data safety 可以不填或少填。
2. Google Play 关注的是**应用及其集成 SDK 的实际数据处理行为**，而不只是最终 manifest 里保留了哪些权限。
3. 隐私政策和 Data safety 的描述必须能解释当前代码中广告、同意管理、设备标识符、诊断/崩溃相关能力的实际用途。

建议：

1. 以 `prodRelease` 或最终提审包为准，重新核对 Google Ads、UMP、Firebase 的实际启用状态。
2. 按最终集成结果填写 Data safety，不要沿用旧结论。
3. 如果某项 SDK 仅保留接入但未启用，也要在提审前明确证据链，而不是仅靠主观判断。

### 2. 冷启动 App Open Ad 需要看 release 真实体验

当前 `MainActivity` 的冷启动流程中，仍会：

1. 在真正冷启动时标记待展示状态
2. preload App Open Ad
3. 在后续时机尝试 `showIfAvailable`

当前风险不在于“splash 必须等广告结束”这一旧问题，而在于：

1. 广告是否只在真正冷启动时展示
2. 广告不可用、超时、失败时，是否快速回到主界面
3. 从后台返回、旋转、重建 Activity 等场景下，是否误触发展示
4. 全屏广告关闭路径是否清晰，是否造成用户误解或打断核心体验

建议：

1. 用 `release/prod` 包在真实设备上验证冷启动、热启动、回前台三类场景。
2. 至少覆盖广告可用、不可用、超时、展示失败四种结果。
3. 记录一轮验证结果，作为提审前自检依据。

### 3. 隐私政策、应用内入口、Play Console 申报需要最终一致

当前代码里，应用内已经具备：

- 设置页“隐私与广告”入口
- 多语言隐私政策页面跳转
- UMP 隐私选项管理入口

这部分已经不是“缺入口”问题，剩余风险在于**一致性**：

1. GitHub Pages 上的公开隐私政策内容，是否与当前应用功能一致
2. Play Console 中填写的隐私政策链接，是否与应用内跳转地址一致
3. Data safety 中关于广告、同意管理、本地媒体访问的描述，是否与隐私政策正文一致

建议：

1. 提审前逐语言检查一次隐私政策页面是否可访问、内容是否最新。
2. 确认 Play Console 使用的隐私政策 URL 与应用内入口一致。
3. 以当前隐私政策正文为准，反向核对 Data safety 文案，避免两边口径不一致。

## 上架前最小检查清单

1. 用 `release/prod` 包重新核对 Google Ads、UMP、Firebase 的实际启用与数据处理行为。
2. 完成 Google Play Data safety 填写，并与隐私政策正文保持一致。
3. 用 `release/prod` 包验证冷启动 App Open Ad 的展示时机和失败回退路径。
4. 检查多语言隐私政策页面、应用内入口、Play Console 链接是否一致且可访问。
