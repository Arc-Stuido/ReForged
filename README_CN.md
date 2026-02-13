# ReForged

中文版 | [English](README.md)

## 概述

**ReForged** 是一个用于 Minecraft Forge 1.21.1 的兼容性桥接模组，它能够让 NeoForge 模组无需任何修改即可在 Forge 上无缝运行。它充当运行时翻译层，自动处理 Forge 和 NeoForge 之间的 API 差异。

## 特性

- ✅ **无需修改** - 只需将 NeoForge 模组和 ReForged 一起放入 `mods/` 文件夹即可
- 🔄 **运行时字节码重写** - 使用 ASM 自动将 NeoForge API 调用转换为 Forge 等效调用
- 📦 **资源集成** - 将 NeoForge 模组资源（材质、模型、声音、数据）注册为资源包
- 🌉 **事件桥接** - 在 Forge 和 NeoForge 事件总线之间转发事件
- 🔌 **API 垫片** - 为 Capabilities、Attachments 和网络数据包提供兼容层
- 🚀 **动态加载** - 在运行时发现并加载 NeoForge 模组

## 工作原理

1. **发现** - 扫描 `mods/` 目录寻找 NeoForge JAR 文件（包含 `META-INF/neoforge.mods.toml`）
2. **加载** - 通过自定义 ClassLoader 加载 NeoForge 模组类
3. **重映射** - 在运行时重写字节码，将 NeoForge API 调用转换为 Forge
4. **桥接** - 使用桥接的事件总线实例化模组类
5. **资源注册** - 将模组资源注册为 Forge 资源包

## 安装方法

### 前置要求

- Minecraft 1.21.1
- Minecraft Forge 52.1.10 或更高版本
- Java 21

### 安装步骤

1. 安装 Minecraft Forge 1.21.1（版本 52.1.10+）
2. 下载 ReForged 模组并放入 `.minecraft/mods/` 文件夹
3. 将任何 NeoForge 模组（适用于 Minecraft 1.21.1）放入同一 `mods/` 文件夹
4. 启动 Minecraft

就是这么简单！ReForged 会自动检测并加载 NeoForge 模组。

## 从源码构建

### 前置条件

- JDK 21
- Git

### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/Mai-xiyu/ReForged.git
cd ReForged

# 构建模组（Linux/macOS）
./gradlew build

# 构建模组（Windows）
gradlew.bat build
```

编译后的 JAR 文件将位于 `build/libs/reforged-1.0.0.jar`

## 技术细节

### 架构

ReForged 使用了多种先进技术：

- **ASM 字节码操作** - 在加载时转换 NeoForge 类
- **自定义 ClassLoader** - 隔离 NeoForge 模组，同时共享 Minecraft/Forge 类
- **Mixin 集成** - 挂钩到 Forge 的模组加载系统
- **事件总线适配器** - 在 Forge 和 NeoForge 事件系统之间转换
- **资源包桥接** - 将 NeoForge 模组资源暴露给 Forge 的资源系统

### 核心组件

- `NeoForgeModLoader` - 从 mods 目录发现并加载 NeoForge 模组
- `EventBridge` - 将所有 Forge 事件转发到 NeoForge 模组处理器
- `ReForgedRemapper` - 基于 ASM 的字节码重写器，用于 API 转换
- `NeoForgeEventBusAdapter` - 包装 Forge 的 IEventBus 以提供 NeoForge 兼容性
- `CapabilityShims` 和 `AttachmentBridge` - 数据附加 API 的兼容层

### 支持的 API

ReForged 提供以下兼容性：

- 事件总线（生命周期事件、游戏玩法事件）
- 模组加载上下文
- 配置系统
- 标签和注册表
- 数据提供器
- Capabilities（Forge）↔ Attachments（NeoForge）
- 网络数据包

## 限制

- 仅支持 Minecraft 1.21.1
- NeoForge 模组必须与 Minecraft 1.21.1 兼容
- 某些 NeoForge 特有功能可能不完全支持
- 严重依赖 NeoForge API 的模组可能需要额外工作

## 故障排除

### 模组无法加载？

1. 检查模组是否包含 `META-INF/neoforge.mods.toml`（不是 `mods.toml`）
2. 验证模组与 Minecraft 1.21.1 的兼容性
3. 检查日志文件（`logs/latest.log`）中的错误

### 崩溃或错误？

1. 确保使用正确的 Forge 版本（52.1.10+）
2. 验证已安装 Java 21
3. 检查是否有冲突的模组
4. 在 GitHub 上报告问题并附上日志

## 贡献

欢迎贡献！请随时提交问题或拉取请求。

1. Fork 本仓库
2. 创建特性分支（`git checkout -b feature/amazing-feature`）
3. 提交更改（`git commit -m '添加某个特性'`）
4. 推送到分支（`git push origin feature/amazing-feature`）
5. 创建拉取请求

## 许可证

保留所有权利

## 致谢

- **作者**：Mai_xiyu
- **项目**：ReForged
- 使用 Minecraft Forge 和 ASM 构建

## 链接

- [GitHub 仓库](https://github.com/Mai-xiyu/ReForged)
- [Minecraft Forge](https://files.minecraftforge.net/)
- [NeoForge](https://neoforged.net/)

---

**注意**：ReForged 是一个非官方兼容层，与 Minecraft Forge 或 NeoForge 团队无关，也未获得他们的认可。
