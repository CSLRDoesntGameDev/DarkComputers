# DarkComputers

### What is DarkComputers?
DarkComputers is a modern Neoforge Mod for Minecraft 1.21.1 that adds programmable computers to the game. <br>
The built-in computer implementation uses a custom RISC ISA emulator known as the DC-S88 purpose-built for this mod <br>
<sub><b/> Note: The previous DC-16 Architecture is still present in the codebase, but unused. </b></sub><br>

While reload persistence is an ongoing goal, several bugs and issues (bad programming) are preventing this from happening.<br>

### Why did you develop DarkComputers?
Minecraft Computing mods have come and gone throughout the years, each one has their own unique thing but tended to not see updates to modern versions.
OpenComputers was the main inspiration for the mod, and early on into development DarkComputers was intended as a Spiritual Successor of OpenComputers before I turned it into what it is now.

### Why a whole custom CPU architecture and not LUA?
<b/> Short Version </b><br>
  LUA-JIT is annoying to integrate with Java and highly limiting, an emulated CPU architecture was the way out of that and meant that languages other than LUA could be used.<br>

<b/> Long Version </b><br>
  I was Intending to use the Risc-V architecture early on but I decided against that due to it's machine language being inconsistent. Ben Eater's 6502 Computer series originally inspired me to use a 6502 CPU Emulator, but that idea did not go far.
  I went through a few different CPUs early on - The 65C02, The Motorola 68000, Zilog Z80, and even a few PowerPC CPUs. I didnt like many of these architectures and went through a few custom ones, I made the DC-16 when prototyping the mod and later designed the DC-S88 to be simplified, easier to use, and have a smaller more useful instruction set.

# Architecture Documentation
 [Main Architecture Documentation (Incomplete and May be Missing Features when they're developed)](https://docs.google.com/document/d/1bmEafuYdsv7kRBn8K8bvwYvGE2lr7YS4L-uJ5lKPWbk/edit?gid=0#gid=0) <br>
 [DC-S88 CPU Instruction Set (Prone to Changes)](https://docs.google.com/spreadsheets/d/14X9gcP__5zqXYBg-X2ciSdWCh2e-8TFFVkLAnioGjEc/edit?gid=0#gid=0)

# Known Issues
- Key Actions tend to repeat even when key is tapped
  - S88 Bus does not appear to remove most recent key when reading from buffer

### Issues with planned Fixes


 ### Inconveniences:  
  - Storage Devices internally point to a directory, Does not clean up that directory when the item is deleted.
    - Potential Refactor: Point storage devices to a binary file, emulate a file system where needed.

# Future Feature Goals
  - Future Mod Goals
    - Programmable Robots
    - Wireless Communication Items
  - Future Software Goals
    - Easy to Use OS (MS-Basic?)
    - Player-Made Example S88 Programs
    - C/C++ or Rust Compiler for DC-S88 Architecture. Bonus Points if it can compile itself and function in the S88 emulator.
  
