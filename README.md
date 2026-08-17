# CobbleUpdraft

> 让宝可梦成为飞行器的升力来源，搭建真正由宝可梦驱动的天空工厂。

[English README](README_EN.md)

## 游戏介绍

CobbleUpdraft 是一款基于 **Cobblemon** 开发的宝可梦飞行器辅助模组。它新增了一个“宝可梦抓手”，玩家可以从自己的 Cobblemon 队伍中选择宝可梦，将其固定在抓手上。符合条件的飞行系、会飞或具有漂浮能力的宝可梦会产生升力，并可在 NeoForge 安装 Sable 物理引擎后为飞行器提供真实的向上浮力。

宝可梦不会从玩家队伍中被删除，而是继续保留在原来的队伍槽位中，同时被抓手锁定并以实体形式固定在方块上。锁定期间，宝可梦不能被放出、收回、放生、交易或参加战斗；玩家可以随时右键抓手将它安全取回。模组还会处理区块重载、实体常驻、绳索显示和移动飞行器跟随，避免固定的宝可梦在飞行过程中消失或脱离抓手。

本模组提供 Fabric 和 NeoForge 版本。基础抓手功能在两个平台都可使用；NeoForge 版本可以选择性接入 Sable（Create Aeronautics 使用的物理引擎），并在同时安装 Create 时提供护目镜信息。

本模组是为 **天空宝可梦工厂** 整合包开发的附属模组之一，欢迎各位玩家游玩体验。感谢整合包作者愿意将模组开源并公开，也感谢 **Horrrs** 老大的支持。

## 核心功能

- **宝可梦抓手方块**：新增一个坐垫造型的方块，用于固定一只队伍中的宝可梦。
- **队伍选择界面**：空手右键空抓手会打开 Cobblemon 队伍选择界面，只显示未濒死且未被其他抓手锁定的宝可梦。
- **宝可梦仍在队伍中**：抓手保存宝可梦 UUID 并重新链接到队伍实例，经验、伤势和状态会继续同步。
- **安全锁定机制**：被抓手固定的宝可梦不能放出、收回、放生、交易或参加战斗，避免飞行器上的宝可梦被意外操作。
- **自动释放**：原主人右键已占用抓手即可取回宝可梦；破坏抓手也会解除锁定并收回展示实体。
- **飞行模式识别**：根据漂浮特性、Cobblemon 骑乘飞行模式、飞行属性、`飞翔` 招式和行为数据判断宝可梦是否可以提供升力。
- **两种升力表现**：气球式升力平稳充放气，扑翼式升力响应更快并带有可调节的周期性颠簸。
- **红石油门控制**：抓手读取周围最高红石信号，0~15 信号对应 0%~100% 油门；可配置无信号时的最低油门。
- **属性与速度影响升力**：宝可梦的速度和六项战斗属性总和共同决定升力容量，并受单个抓手上限限制。
- **绳索视觉连接**：抓手顶部与宝可梦之间使用独立的锚点实体绘制绳子，不依赖 Cobblemon 原版拴绳数据。
- **移动飞行器支持**：在 Sable 的 sublevel/飞行器空间中，抓手和宝可梦会跟随飞行器的位置与速度更新。
- **Create 护目镜信息**：NeoForge 同时安装 Create 与 Sable 时，佩戴护目镜可查看宝可梦、速度、属性总和、油门、填充进度和实时升力。
- **碰撞优化**：默认取消被抓住宝可梦的碰撞和可选取状态，避免推挤玩家、干扰飞行器或挡住方块交互。

## 使用方法

### 放置抓手

宝可梦抓手物品 ID 为 `cobbleupdraft:pokemon_grabber`，会加入模组自己的 **CobbleUpdraft** 创造物品栏。当前源码没有提供额外的自定义合成配方，生存模式中的获取方式由整合包或服务器配置决定。

### 固定宝可梦

1. 放置宝可梦抓手。
2. 确保目标宝可梦位于玩家当前队伍中，且玩家手持空手。
3. 空手右键抓手，打开 Cobblemon 队伍选择界面。
4. 选择一只未濒死、未被其他抓手锁定的宝可梦。

选中后，宝可梦会被放出并移动到抓手上方。它仍然属于原玩家的队伍，但会被抓手锁定，直到取回或抓手被破坏。一个抓手只能固定一只宝可梦。

### 取回宝可梦

由原主人空手右键已占用的抓手即可取回宝可梦。取回后，宝可梦会解除锁定、恢复正常 AI、重力和碰撞，并通过 Cobblemon 的收回流程回到队伍中。

其他玩家不能通过右键直接取回别人的宝可梦。破坏抓手会执行释放流程，宝可梦仍会回到它原本所属玩家的队伍中。

### 控制升力

抓手会读取相邻方块中的最高红石信号：

- 红石信号为 0 时，默认油门为 0%，抓手不产生升力。
- 红石信号为 15 时，油门为 100%，达到当前宝可梦的最大升力容量。
- `minThrottle` 可以设置无红石信号时的最低油门；默认值 `0.0` 表示完全关闭。
- 气球式和扑翼式会使用不同的充气、排气和阻尼参数，升力不会瞬间跳变。

### 在飞行器上使用

在 NeoForge 中安装 Sable 后，将抓手装配到 Sable 支持的飞行器或 sublevel 中。Sable 会根据当前世界重力和气压，将抓手的升力换算为作用于飞行器的向上浮力。多个抓手可以共同提供升力，红石信号可以分别调节每个抓手的油门。

Fabric 版本没有 Sable 物理集成，因此不会对飞行器施加物理向上力；宝可梦固定、展示、锁定和基础升力计算仍然可用。

## 升力计算

### 升力容量

每个可提供升力的宝可梦都有一个满油门容量，计算方式为：

```text
capacity = min(
    速度 × liftPerSpeedPoint
    + 六项属性总和 × liftPerStatTotalPoint,
    maxLift
)
```

六项属性总和为最大生命值、攻击、防御、特攻、特防和速度的总和。实际目标升力为：

```text
targetLift = capacity × throttle
```

界面和护目镜中的升力单位显示为 `kpg`，可理解为模组内部用于表示可吊起方块重量的升力单位。`maxLift` 是单个抓手的容量上限，并不代表整个飞行器的总升力上限。

### 可提供升力的宝可梦

模组按以下顺序判断升力来源：

1. 启用 `levitateAbilityLifts` 且宝可梦拥有 **漂浮** 特性：使用气球式升力。
2. 宝可梦存在 Cobblemon 空中骑乘数据：`smoothFlightModes` 中的模式（默认 `hover`、`helicopter`、`jet`、`rocket`）使用气球式，其他模式（例如 `bird`、`glider`）使用扑翼式。
3. 启用 `anyPokemonCanLift`：所有宝可梦都使用扑翼式升力。
4. 启用 `flyingTypeLifts` 且宝可梦拥有飞行属性：使用扑翼式升力。
5. 启用 `flyMoveLifts` 且招式列表中有 `fly`（飞翔）：使用扑翼式升力。
6. 启用 `canFlyBehaviourLifts` 且 Cobblemon 行为数据标记 `canFly`：使用扑翼式升力。

不满足任何条件的宝可梦仍然可以被固定和展示，但不能提供升力。宝可梦的实际物种数据和配置文件共同决定最终结果。

## 宝可梦锁定与数据安全

被抓手固定后，宝可梦仍在原玩家队伍中，因此不会因为抓手方块被卸载而丢失队伍数据。模组会将展示实体标记为常驻实体，并在区块重载后重新连接队伍中的宝可梦实例。

锁定期间，以下操作会被阻止：

- 放出宝可梦；
- 收回抓手上的宝可梦；
- 放生宝可梦；
- 与其他玩家交易；
- 参加战斗或在战斗中切换上场。

如果展示实体意外消失，抓手会在服务器端尝试重新连接或从主人队伍重新放出；主人离线时会保持锁定并等待主人回到服务器。宝可梦濒死、从主人队伍中移出或无法重新连接时，抓手会清除绑定并解除锁定。

## Sable 与 Create 兼容

### Sable 物理升力

NeoForge 版本会在启动时检测可选的 `sable` 模组：

- 未安装 Sable：使用基础方块实体，不施加飞行器物理力。
- 安装 Sable：抓手作为 Sable 的 sublevel 方块实体参与物理计算。
- 升力会根据世界重力和气压换算，并支持飞行器坐标投影与移动速度前瞻。
- 升力归零时会唤醒休眠的物理体，避免飞行器在关闭油门后停在空中。

Sable 是可选依赖，NeoForge 元数据要求版本为 `2.0` 或更高。具体安装方式请以使用的 Sable/Create Aeronautics 版本说明为准。

### Create 护目镜

如果 NeoForge 同时检测到 Create 和 Sable，宝可梦抓手会提供 Create 护目镜信息，包括：

- 当前固定的宝可梦；
- 速度和六项属性总和；
- 当前红石信号与油门百分比；
- 气囊/升力填充进度；
- 当前实时升力（`kpg`）。

没有 Create 时，Sable 升力仍可工作，但不会提供 Create 专属护目镜提示。

## 配置文件

首次启动后，配置文件会生成在：

```text
config/cobbleupdraft.json
```

配置在游戏或服务器启动时读取，修改后需要重启才能生效。数值参数使用小数时请保留 JSON 数字格式，不要加单位字符串。

### 升力与油门

| 配置项 | 默认值 | 说明 |
| --- | ---: | --- |
| `liftPerSpeedPoint` | `0.3` | 速度每增加 1 点，为升力容量增加的数值。 |
| `liftPerStatTotalPoint` | `0.05` | 六项属性总和每增加 1 点，为升力容量增加的数值。 |
| `maxLift` | `100.0` | 单个抓手的升力容量上限，红石油门计算前的最大值。 |
| `minThrottle` | `0.0` | 无红石信号时的最低油门，范围建议为 `0.0`~`1.0`。 |

### 气球式与扑翼式响应

| 配置项 | 默认值 | 说明 |
| --- | ---: | --- |
| `smoothFlightModes` | `["hover", "helicopter", "jet", "rocket"]` | 判定为平稳气球式升力的 Cobblemon 空中骑乘模式名称。 |
| `liftFillingTimeTicks` | `180.0` | 气球式升力充气响应时间，180 tick 约为 9 秒。 |
| `liftEmptyingTimeTicks` | `180.0` | 气球式升力排气响应时间。 |
| `wingedFillingTimeTicks` | `40.0` | 扑翼式升力上升响应时间。 |
| `wingedEmptyingTimeTicks` | `60.0` | 扑翼式升力回落响应时间。 |
| `wingedTurbulence` | `0.15` | 扑翼式周期性颠簸幅度，占当前升力比例；设为 `0` 可关闭。 |
| `wingedTurbulencePeriodTicks` | `40.0` | 扑翼式颠簸周期，单位为 tick。 |
| `wingedDamping` | `0.05` | 扑翼式垂直阻尼，数值越高越稳定。 |
| `responsivenessFactor` | `5.0` | 接近目标升力时的收敛加速系数；设为 `0` 可关闭。 |
| `responsivenessRange` | `0.05` | 收敛加速的生效区间，占容量的比例。 |
| `liftDamping` | `0.2` | 气球式垂直阻尼系数。 |

### 升力资格与显示

| 配置项 | 默认值 | 说明 |
| --- | :---: | --- |
| `anyPokemonCanLift` | `false` | 为 `true` 时任何宝可梦都能提供扑翼式升力，并跳过后续资格判定。 |
| `flyingTypeLifts` | `true` | 飞行属性宝可梦可以提供扑翼式升力。 |
| `levitateAbilityLifts` | `true` | 拥有漂浮特性的宝可梦可以提供气球式升力。 |
| `flyMoveLifts` | `true` | 学会 `fly`（飞翔）招式的宝可梦可以提供扑翼式升力。 |
| `canFlyBehaviourLifts` | `true` | Cobblemon 行为数据中 `canFly` 为真的宝可梦可以提供扑翼式升力。 |
| `hoverHeight` | `0.3` | 可提供升力时，宝可梦悬浮在抓手上方的高度，单位为方块。 |
| `grabbedPokemonNoCollision` | `true` | 关闭被固定宝可梦的碰撞、推动和射线选取，避免影响玩家与飞行器交互。 |

一个用于限制宝可梦资格的配置示例：

```json
{
  "liftPerSpeedPoint": 0.3,
  "liftPerStatTotalPoint": 0.05,
  "maxLift": 100.0,
  "minThrottle": 0.1,
  "smoothFlightModes": ["hover", "helicopter", "jet", "rocket"],
  "liftFillingTimeTicks": 180.0,
  "liftEmptyingTimeTicks": 180.0,
  "wingedFillingTimeTicks": 40.0,
  "wingedEmptyingTimeTicks": 60.0,
  "wingedTurbulence": 0.15,
  "wingedTurbulencePeriodTicks": 40.0,
  "wingedDamping": 0.05,
  "responsivenessFactor": 5.0,
  "responsivenessRange": 0.05,
  "liftDamping": 0.2,
  "anyPokemonCanLift": false,
  "flyingTypeLifts": true,
  "levitateAbilityLifts": true,
  "flyMoveLifts": true,
  "canFlyBehaviourLifts": true,
  "hoverHeight": 0.3,
  "grabbedPokemonNoCollision": true
}
```

## 方块与实体 ID

| 内容 | ID | 说明 |
| --- | --- | --- |
| 宝可梦抓手方块/物品 | `cobbleupdraft:pokemon_grabber` | 核心功能方块。 |
| 抓手锚点实体 | `cobbleupdraft:grabber_anchor` | 不可见的内部实体，仅用于同步和绘制抓手与宝可梦之间的绳子。 |
| 创造物品栏 | `cobbleupdraft:main` | 模组自己的 CobbleUpdraft 创造标签页。 |

## 安装要求

- Minecraft **1.21.1**
- Java **21** 或更高版本
- Cobblemon **1.7.1** 或更高版本
- Architectury API **13.0.8** 或更高版本
- Fabric：Fabric Loader **0.18.0** 或更高版本、Fabric API **0.116.10+1.21.1**
- NeoForge：NeoForge **21.1** 系列
- 可选：NeoForge 下的 Sable **2.0** 或更高版本，用于飞行器物理升力
- 可选：NeoForge 下的 Create，与 Sable 一起安装时启用护目镜信息

CobbleUpdraft 本身不把 Create 或 Sable 声明为基础必需依赖；如果需要飞行器浮力，请确保使用的 Sable/Create Aeronautics 版本与 Minecraft、NeoForge 和 Cobblemon 版本匹配。

## 安装方法

1. 安装 Minecraft 1.21.1 对应的 Fabric 或 NeoForge 实例。
2. 将 Cobblemon、Architectury API、对应平台加载器依赖和 CobbleUpdraft 放入 `mods` 文件夹。
3. 需要让抓手推动物理飞行器时，在 NeoForge 中额外安装 Sable 或提供 Sable 的 Create Aeronautics 版本。
4. 需要护目镜信息时，再安装与 Sable 匹配的 Create。
5. 启动游戏，在 CobbleUpdraft 创造物品栏中确认宝可梦抓手已加载。

## 从源码构建

项目使用 Architectury，同时提供 Fabric 和 NeoForge 构建目标。Windows 可以运行：

```powershell
.\gradlew.bat :fabric:remapJar :neoforge:remapJar
```

Linux、macOS 或 Git Bash 可以运行：

```bash
./gradlew :fabric:remapJar :neoforge:remapJar
```

构建产物位于：

```text
fabric/build/libs/cobbleupdraft-fabric-1.0.jar
neoforge/build/libs/cobbleupdraft-neoforge-1.0.jar
```

NeoForge 构建使用 `common/libs/` 中的 Sable、Sable Companion、Veil 和 Create 编译期文件；这些文件不会替代游戏实例中的运行时依赖。

## 当前版本限制

- 只能从玩家当前队伍中选择宝可梦，暂不支持直接从 PC 盒子选择。
- 一个抓手只能固定一只宝可梦，且固定后宝可梦会被锁定，不能用于放出、收回、交易、放生和战斗。
- Fabric 版本没有 Sable 物理集成，因此不会直接为物理飞行器施加升力。
- Sable/Create 兼容取决于 NeoForge 侧对应版本是否正确加载；没有 Sable 时抓手仍可展示和锁定宝可梦，但不产生飞行器物理浮力。
- 没有任何升力资格的宝可梦可以被固定，但不会产生升力。
- `seething` 等 Create 热量等级与本模组无关；本模组的升力大小只由宝可梦数据、红石油门和配置决定。
- 当前源码没有额外的自定义合成配方。

## 授权说明

本项目附带的使用、修改和再发布权限以仓库中的 [LICENSE.txt](LICENSE.txt) 为准。当前许可证文件注明 **All rights reserved**，请在分发或二次开发前确认授权范围。

## 致谢

- 感谢 Cobblemon、Architectury、Sable、Create Aeronautics、Create 和相关依赖的开发者与维护者。
- 感谢所有参与测试、反馈飞行器物理表现和宝可梦兼容性的玩家。
