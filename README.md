# ReForged

[中文版](README_CN.md) | English

## Overview

**ReForged** is a compatibility bridge mod for Minecraft Forge 1.21.1 that enables NeoForge mods to run seamlessly on Forge without any modifications. It acts as a runtime translation layer, automatically handling API differences between Forge and NeoForge.

## Features

- ✅ **Zero Modification Required** - Drop NeoForge mods into your `mods/` folder alongside ReForged
- 🔄 **Runtime Bytecode Rewriting** - Automatically translates NeoForge API calls to Forge equivalents using ASM
- 📦 **Resource Integration** - Registers NeoForge mod resources (textures, models, sounds, data) as resource packs
- 🌉 **Event Bridge** - Forwards events between Forge and NeoForge event buses
- 🔌 **API Shims** - Provides compatibility layers for capabilities, attachments, and network packets
- 🚀 **Dynamic Loading** - Discovers and loads NeoForge mods at runtime

## How It Works

1. **Discovery** - Scans the `mods/` directory for NeoForge JARs (containing `META-INF/neoforge.mods.toml`)
2. **Loading** - Loads NeoForge mod classes via a custom ClassLoader
3. **Remapping** - Rewrites bytecode at runtime to translate NeoForge API calls to Forge
4. **Bridging** - Instantiates mod classes with bridged event buses
5. **Resource Registration** - Registers mod resources as Forge resource packs

## Installation

### Requirements

- Minecraft 1.21.1
- Minecraft Forge 52.1.10 or higher
- Java 21

### Steps

1. Install Minecraft Forge 1.21.1 (version 52.1.10+)
2. Download ReForged mod and place it in your `.minecraft/mods/` folder
3. Place any NeoForge mods (for Minecraft 1.21.1) in the same `mods/` folder
4. Launch Minecraft

That's it! ReForged will automatically detect and load the NeoForge mods.

## Building from Source

### Prerequisites

- JDK 21
- Git

### Build Steps

```bash
# Clone the repository
git clone https://github.com/Mai-xiyu/ReForged.git
cd ReForged

# Build the mod (Linux/macOS)
./gradlew build

# Build the mod (Windows)
gradlew.bat build
```

The compiled JAR will be located in `build/libs/reforged-1.0.0.jar`

## Technical Details

### Architecture

ReForged uses several advanced techniques:

- **ASM Bytecode Manipulation** - Transforms NeoForge classes at load time
- **Custom ClassLoader** - Isolates NeoForge mods while sharing Minecraft/Forge classes
- **Mixin Integration** - Hooks into Forge's mod loading system
- **Event Bus Adapter** - Translates between Forge and NeoForge event systems
- **Resource Pack Bridge** - Exposes NeoForge mod assets to Forge's resource system

### Key Components

- `NeoForgeModLoader` - Discovers and loads NeoForge mods from the mods directory
- `EventBridge` - Forwards all Forge events to NeoForge mod handlers
- `ReForgedRemapper` - ASM-based bytecode rewriter for API translation
- `NeoForgeEventBusAdapter` - Wraps Forge's IEventBus for NeoForge compatibility
- `CapabilityShims` & `AttachmentBridge` - Compatibility layers for data attachment APIs

### Supported APIs

ReForged provides compatibility for:

- Event Bus (lifecycle events, gameplay events)
- Mod Loading Context
- Configuration System
- Tags and Registries
- Data Providers
- Capabilities (Forge) ↔ Attachments (NeoForge)
- Network Packets

## Limitations

- Only supports Minecraft 1.21.1
- NeoForge mods must be compatible with Minecraft 1.21.1
- Some NeoForge-specific features may not be fully supported
- Mods with heavy NeoForge API dependencies may require additional work

## Troubleshooting

### Mods Not Loading?

1. Check that mods have `META-INF/neoforge.mods.toml` (not `mods.toml`)
2. Verify mod compatibility with Minecraft 1.21.1
3. Check the log file (`logs/latest.log`) for errors

### Crashes or Errors?

1. Ensure you have the correct Forge version (52.1.10+)
2. Verify Java 21 is installed
3. Check for conflicting mods
4. Report issues on GitHub with logs

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

All Rights Reserved

## Credits

- **Author**: Mai_xiyu
- **Project**: ReForged
- Built with Minecraft Forge and ASM

## Links

- [GitHub Repository](https://github.com/Mai-xiyu/ReForged)
- [Minecraft Forge](https://files.minecraftforge.net/)
- [NeoForge](https://neoforged.net/)

---

**Note**: ReForged is an unofficial compatibility layer and is not affiliated with or endorsed by Minecraft Forge or NeoForge teams.
