# 黑洞手雷 · Black Hole Grenade（Fabric 1.21.1）

扔出去，落点生成一个持续 **12 秒** 的黑洞：**50 格** 内所有实体被吸向中心（带切向旋转，像吸积盘），每 0.5 秒结算一次伤害，中心最高 **8 点/次**（约 16 DPS），边缘按距离平方衰减。

![吸积盘纹理](src/main/resources/assets/blackhole/textures/entity/black_hole_disk.png)

## 版本

| 项 | 值 |
| --- | --- |
| 游戏 | Minecraft **1.21.1** |
| 加载器 | **Fabric**（Loader 0.16.5、Fabric API 0.102.1+1.21.1） |
| Java | **21**（⚠️ 见下） |
| 依赖 | Fabric API |

> ⚠️ **你说 Java 17，但 1.21.1 官方要求 Java 21**（1.20.5 起就升到 21 了，Java 17 起不来游戏）。
> 所以这个工程按 **Java 21** 写：`java 21`、`options.release = 21`、`fabric.mod.json` 里 `"java": ">=21"`。

## 编译（本机没装 JDK，我没法替你编）

```powershell
# 1. 装 JDK 21（任选其一）
winget install EclipseAdoptium.Temurin.21.JDK
# 2. 装 Gradle 8.8+（或用 IDEA 自带的）
winget install Gradle.Gradle
# 3. 进目录编译
cd E:\dsh1\blackhole-mod
gradle build
# 产物：build\libs\blackhole-grenade-1.0.0.jar
```

把 jar 丢进 `.minecraft/mods/`，同时需要有 **Fabric Loader 0.16.5+** 和 **Fabric API**。

## 玩法

- 合成：`末影之眼 ×4 + 下界之星 ×1`（十字形）
- 右键扔出，撞到方块或实体就在落点展开黑洞
- 黑洞对**自己人也会吸**（包括投掷者），扔完记得跑

## 参数在哪改

`src/main/java/com/dsh/blackhole/entity/BlackHoleEntity.java` 顶部：

```java
public static final double RADIUS = 50.0;   // 吸附半径（格）
public static final int LIFETIME = 240;     // 存活 tick（240 = 12 秒）
private static final double PULL = 0.16;    // 吸力系数
private static final float MAX_DAMAGE = 8.0f; // 中心每次伤害（每 10 tick 结算）
```

## 视觉怎么做的

没有用自定义着色器，三层面向镜头的四边形叠加，贴图都是脚本生成的（`tools/gen-textures.mjs`，纯 Node、零依赖）：

| 层 | 贴图 | 作用 |
| --- | --- | --- |
| 1 | `black_hole_disk.png` | 橙色三旋臂吸积盘，加法混合，缓慢旋转 |
| 2 | `black_hole_core.png` | 纯黑事件视界，挡住后面的星空 |
| 3 | `black_hole_ring.png` | 青白引力透镜光环，反向旋转 |

外加上服务端粒子：`REVERSE_PORTAL`（向内吸的紫色点）、`SOUL_FIRE_FLAME`（旋臂火星）、`END_ROD`（外圈亮点）、`PORTAL`。

## 文件结构

```
src/main/java/com/dsh/blackhole/
  BlackHoleMod.java           注册物品/实体/物品栏
  item/BlackHoleGrenadeItem.java  扔出去
  entity/ThrownBlackHole.java     飞行体，落地生成黑洞
  entity/BlackHoleEntity.java     吸附 + 伤害 + 粒子（核心逻辑）
  client/BlackHoleClient.java     注册渲染器
  client/BlackHoleRenderer.java   三层叠加渲染
src/main/resources/
  fabric.mod.json
  assets/blackhole/{icon,textures/item,textures/entity,lang,models}
  data/blackhole/recipe/black_hole_grenade.json
tools/gen-textures.mjs       贴图生成脚本（改完重跑即可重生成）
```

## 已知风险（未编译验证）

这台机器**没有 JDK**，所以代码是照 1.21.1 的 Yarn 映射写的，**没有真正编译过**。最可能报错的几处：

- `EntityType.Builder.build()` —— 1.21.2+ 才要求传 RegistryKey，1.21.1 应该无参
- `EntityRenderer#getTexture` —— 1.21.2 之后被重构掉，1.21.1 还留着
- `entity.velocityModified` 字段赋值
- 渲染层 `VertexConsumer.vertex(MatrixStack.Entry, …)` 的链式调用

把编译报错贴给我，我按报错改。或者你说一声，我在这台机器上装 JDK 21 + Gradle（约 300MB 下载）直接编出 jar 来验证。
