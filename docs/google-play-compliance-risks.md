# Google Play 上架剩余合规风险

更新时间：2026-06-26

本文只保留**当前代码里仍然存在**、在 Google Play 上架前需要继续处理的问题。已经修正或已经移除的历史问题不再记录。

## 风险总览

| 优先级 | 风险项 | 当前状态 | 建议处理 |
|---|---|---|---|
| 高 | 隐私政策 URL 仍是占位符 | App 内已提供隐私入口，但实际打开的是 `https://example.com/velaris/privacy-policy` | 替换为真实公网 URL，并确保内容与 Play Console 申报一致 |
| 高 | Data safety 仍需按 Ads SDK 实际情况申报 | 项目集成 Google Ads / UMP / Firebase，不能因为部分权限已移除就省略申报 | 按 SDK 实际数据处理情况填写 Data safety 和隐私政策 |
| 中 | 冷启动开屏广告体验仍需验证 | 冷启动会尝试展示 App Open Ad | 用 release 包重点验证是否阻塞启动、是否在错误时机打断用户 |

## 详细问题

### 1. 隐私政策 URL 仍未落地

当前 settings 页面已经提供“隐私政策和广告隐私选项”入口，但实际打开的仍是占位地址：

- `feature/settings/impl/src/main/java/com/wujia/feature/settings/impl/ui/SettingsScreen.kt`
- `PRIVACY_POLICY_URL = "https://example.com/velaris/privacy-policy"`

这意味着：

1. App 内虽然有入口，但不满足真实可访问的隐私政策要求。
2. Play Console 的隐私政策链接与应用内文案容易不一致。

建议：

1. 替换为正式公网地址。
2. 隐私政策正文至少覆盖广告 SDK、同意管理、用户主动选择的本地媒体素材用途。
3. 保证 App 内入口、Play Console 链接、Data safety 描述三者一致。

### 2. Data safety 不能只看 Manifest 权限

当前项目已集成以下能力：

- Google Mobile Ads
- Google UMP（用户同意管理）
- Firebase / google-services 集成

虽然主 Manifest 已显式移除 `AD_ID`、`ACCESS_ADSERVICES_AD_ID` 等权限，也关闭了部分 analytics/AD_ID collection 开关，但这**不等于**可以跳过 Data safety 申报。Google Play 看的是应用和 SDK 的实际数据处理行为，不只是你最终保留了哪些权限声明。

建议：

1. 按 Ads SDK、UMP、Firebase 在 release 包中的实际行为填写 Data safety。
2. 不要沿用“Manifest 已移除相关权限，所以这项没风险”的判断。
3. 提审前用 release 变体重新核对一次 merged manifest、运行时同意流程、隐私入口文案。

### 3. 冷启动开屏广告仍有体验风险

当前 `MainActivity` 在冷启动时会：

1. 标记 cold start app open pending
2. preload App Open Ad
3. 在 `onPostCreate` 后尝试 `showIfAvailable`

这类广告本身不一定违规，但属于 Google Play 审核比较敏感的场景。当前代码虽然有 timeout / unavailable 分支，但是否真正“不阻塞启动、不在错误时机打断用户”，还需要以 release 包实测为准。

重点验证：

1. 首次冷启动时 splash 是否被广告流程拖住过久。
2. 广告不可用、超时、失败时，是否能快速进入主界面。
3. 是否只在真正冷启动时展示，而不是返回前台、切后台再回来时误触发。
4. 全屏广告关闭路径是否清晰。

## 上架前最小检查清单

1. 替换真实隐私政策 URL。
2. 用 release / prod 包验证 UMP 同意流程。
3. 用 release / prod 包验证冷启动 App Open Ad 的真实体验。
4. 按 Ads SDK / UMP / Firebase 的实际行为填写 Data safety。
