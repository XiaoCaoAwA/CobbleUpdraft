# CobbleUpdraft

> Turn Pokémon into lift and build a sky factory powered by living flight systems.

[中文 README](README.md)

## Overview

CobbleUpdraft is a Cobblemon-focused addon for Pokémon-powered aircraft. It adds a **Pokémon Grabber** block that lets a player select a Pokémon from their Cobblemon party and station it on the grabber. Eligible Flying-type, flying-capable, or Levitate Pokémon can provide lift. On NeoForge, installing the Sable physics engine enables that lift to become real upward force for physics-based aircraft.

The Pokémon is not removed from the player's party. Instead, it remains in its original party slot while the grabber locks it and keeps a visible Pokémon entity attached to the block. While locked, the Pokémon cannot be sent out, recalled, released, traded, or used in battle. The owner can right-click the grabber to retrieve it safely. The mod also handles chunk reloads, persistent grabbed entities, a custom rope visual, and moving-aircraft tracking so a stationed Pokémon does not disappear or drift away during flight.

CobbleUpdraft provides Fabric and NeoForge builds. The basic grabber and Pokémon-locking features work on both loaders. The NeoForge build can optionally integrate with Sable, the physics engine used by Create Aeronautics, and can provide Create goggle information when Create is installed as well.

This mod was developed as one of the addons for the **Sky Pokémon Factory** modpack. Everyone is welcome to try it. Thanks to the modpack author for open-sourcing and publishing the mod, and special thanks to **Horrrs** for the support.

## Features

- **Pokémon Grabber block**: a cushion-shaped block that stations one Pokémon from the player's party.
- **Party selection screen**: empty-handed right-clicking opens Cobblemon's party selector and filters out fainted or already locked Pokémon.
- **Pokémon stays in the party**: the grabber stores the Pokémon UUID and relinks to the party instance so experience, damage, and status remain synchronized.
- **Safety lock**: a stationed Pokémon cannot be sent out, recalled, released, traded, or used in battle.
- **Owner retrieval**: the original owner can right-click an occupied grabber to release the Pokémon; breaking the block also clears the assignment and recalls it.
- **Flight capability detection**: lift eligibility can come from Levitate, Cobblemon riding flight modes, the Flying type, the `fly` move, or `canFly` behavior data.
- **Two lift styles**: balloon-style lift fills and empties smoothly, while winged-style lift responds faster and can add configurable turbulence.
- **Redstone throttle**: the grabber reads the strongest neighboring redstone signal; signal 0-15 maps to a 0%-100% throttle, with a configurable minimum throttle.
- **Speed and stats matter**: lift capacity is based on the Pokémon's Speed and the sum of its six battle stats, with a per-grabber cap.
- **Rope visual**: a dedicated invisible anchor entity renders the rope between the grabber and the Pokémon without using Cobblemon's normal leash data.
- **Moving-aircraft support**: with Sable, the grabber and Pokémon follow sublevel/aircraft coordinates and account for ship velocity.
- **Create goggles**: with Create and Sable on NeoForge, goggles show the Pokémon, Speed, stat total, throttle, fill state, and live lift.
- **Collision optimization**: by default, grabbed Pokémon are not pickable or pushable, preventing them from blocking interactions or disturbing the aircraft.

## How to use

### Obtain and place the grabber

The item ID is `cobbleupdraft:pokemon_grabber`. It is added to the mod's **CobbleUpdraft** creative tab. The current source does not define a custom crafting recipe; survival acquisition is controlled by the modpack or server setup.

### Station a Pokémon

1. Place a Pokémon Grabber.
2. Keep the target Pokémon in the player's active party and hold an empty hand.
3. Empty-handed right-click the grabber to open Cobblemon's party selection screen.
4. Choose a non-fainted Pokémon that is not already locked by another grabber.

The selected Pokémon is sent out and positioned above the grabber. It remains in the original player's party but is locked until it is retrieved or the block is broken. One grabber can station one Pokémon.

### Retrieve a Pokémon

The original owner can empty-handed right-click an occupied grabber to retrieve its Pokémon. The Pokémon is unlocked, its normal AI, gravity, and collision are restored, and Cobblemon's recall flow returns it to the party.

Other players cannot retrieve an occupied grabber by right-clicking it. Breaking the block runs the release flow and returns the Pokémon to its original owner's party.

### Control lift with redstone

The grabber reads the strongest neighboring redstone signal:

- With signal 0, the default throttle is 0% and the grabber produces no lift.
- With signal 15, the throttle is 100% and the grabber reaches the Pokémon's current capacity.
- `minThrottle` sets a minimum throttle when no redstone signal is present; the default `0.0` fully disables lift without a signal.
- Balloon and winged styles use separate fill, empty, and damping parameters, so lift changes are gradual rather than instantaneous.

### Use it on an aircraft

On NeoForge with Sable installed, assemble the grabber into a Sable-supported aircraft or sublevel using the normal workflow of the aircraft mod. Sable converts the grabber's lift into upward force using the world's gravity and air pressure. Multiple grabbers can contribute lift together, and each one can be throttled independently with redstone.

The Fabric build has no Sable physics integration, so it does not apply physical upward force to aircraft. The grabber still supports Pokémon selection, display, locking, and lift calculation as a base feature.

## Lift calculation

### Lift capacity

Each eligible Pokémon has a full-throttle capacity calculated as follows:

```text
capacity = min(
    Speed × liftPerSpeedPoint
    + six-stat total × liftPerStatTotalPoint,
    maxLift
)
```

The six-stat total is max HP, Attack, Defence, Special Attack, Special Defence, and Speed. The target lift is:

```text
targetLift = capacity × throttle
```

The tooltip and goggles display lift in `kpg`, the mod's internal lift unit for approximate block-weight capacity. `maxLift` limits one grabber; it is not a total limit for the entire aircraft.

### Pokémon that can provide lift

Lift mode is checked in this order:

1. If `levitateAbilityLifts` is enabled and the Pokémon has **Levitate**, it uses balloon-style lift.
2. If the Pokémon has Cobblemon air-riding data, modes in `smoothFlightModes` (by default `hover`, `helicopter`, `jet`, and `rocket`) use balloon-style lift. Other modes such as `bird` and `glider` use winged-style lift.
3. If `anyPokemonCanLift` is enabled, every Pokémon uses winged-style lift.
4. If `flyingTypeLifts` is enabled and the Pokémon has the Flying type, it uses winged-style lift.
5. If `flyMoveLifts` is enabled and the move list contains `fly`, it uses winged-style lift.
6. If `canFlyBehaviourLifts` is enabled and the Cobblemon behavior data marks the form as `canFly`, it uses winged-style lift.

A Pokémon that matches none of these conditions can still be stationed and displayed, but it produces no lift. The final result depends on both the Pokémon's data and the configuration.

## Locking and data safety

Because the Pokémon remains in the original party, it does not lose its party data when the grabber's chunk unloads. The mod marks the displayed entity as persistent and reconnects it to the party instance after chunk reloads.

While locked, the following actions are blocked:

- Sending the Pokémon out;
- Recalling the grabbed Pokémon;
- Releasing the Pokémon;
- Trading it with another player;
- Entering battle or switching it into battle.

If the displayed entity disappears unexpectedly, the grabber attempts to reconnect to an existing entity or send the Pokémon out again from the owner's party. If the owner is offline, the lock remains active until the owner returns. If the Pokémon is fainted, removed from the owner's party, or cannot be reconnected, the grabber clears the assignment and unlocks it.

## Sable and Create compatibility

### Sable physics lift

The NeoForge build checks for the optional `sable` mod at startup:

- Without Sable, the grabber uses the base block entity and applies no aircraft physics force.
- With Sable, the grabber becomes a Sable sublevel block entity and participates in physics updates.
- Lift is converted using world gravity and air pressure, with support for sublevel coordinate projection and ship-velocity prediction.
- When lift reaches zero, the physical body is woken so the aircraft can fall or resume simulation instead of remaining asleep in midair.

Sable is optional, and the NeoForge metadata accepts Sable `2.0` or newer. Follow the installation instructions for the Sable/Create Aeronautics version used by the modpack.

### Create goggles

When NeoForge detects both Create and Sable, the Pokémon Grabber provides Create goggle information for:

- The stationed Pokémon;
- Speed and six-stat total;
- Current redstone signal and throttle percentage;
- Lift fill progress;
- Current live lift in `kpg`.

Sable lift still works without Create, but Create-specific goggle information is unavailable.

## Configuration

The configuration file is generated after the first launch at:

```text
config/cobbleupdraft.json
```

Configuration is read when the game or server starts, so restart after editing. Numeric values should remain JSON numbers rather than strings.

### Lift and throttle

| Option | Default | Description |
| --- | ---: | --- |
| `liftPerSpeedPoint` | `0.3` | Lift capacity added per point of Speed. |
| `liftPerStatTotalPoint` | `0.05` | Lift capacity added per point of six-stat total. |
| `maxLift` | `100.0` | Maximum lift capacity of one grabber before throttle is applied. |
| `minThrottle` | `0.0` | Minimum throttle without redstone; recommended range is `0.0` to `1.0`. |

### Balloon and winged response

| Option | Default | Description |
| --- | ---: | --- |
| `smoothFlightModes` | `["hover", "helicopter", "jet", "rocket"]` | Cobblemon air-riding mode names treated as smooth balloon-style lift. |
| `liftFillingTimeTicks` | `180.0` | Balloon-style fill response time; 180 ticks is about 9 seconds. |
| `liftEmptyingTimeTicks` | `180.0` | Balloon-style empty response time. |
| `wingedFillingTimeTicks` | `40.0` | Winged-style lift rise response time. |
| `wingedEmptyingTimeTicks` | `60.0` | Winged-style lift fall response time. |
| `wingedTurbulence` | `0.15` | Periodic winged turbulence as a fraction of current lift; set to `0` to disable. |
| `wingedTurbulencePeriodTicks` | `40.0` | Winged turbulence period in ticks. |
| `wingedDamping` | `0.05` | Winged vertical damping; higher values are steadier. |
| `responsivenessFactor` | `5.0` | Extra convergence acceleration near the target; set to `0` to disable. |
| `responsivenessRange` | `0.05` | Fraction of capacity in which convergence acceleration applies. |
| `liftDamping` | `0.2` | Balloon-style vertical damping coefficient. |

### Lift eligibility and display

| Option | Default | Description |
| --- | :---: | --- |
| `anyPokemonCanLift` | `false` | When `true`, every Pokémon provides winged-style lift and later eligibility checks are skipped. |
| `flyingTypeLifts` | `true` | Allows Flying-type Pokémon to provide winged-style lift. |
| `levitateAbilityLifts` | `true` | Allows Pokémon with Levitate to provide balloon-style lift. |
| `flyMoveLifts` | `true` | Allows Pokémon that know `fly` to provide winged-style lift. |
| `canFlyBehaviourLifts` | `true` | Allows forms whose Cobblemon behavior data has `canFly` enabled. |
| `hoverHeight` | `0.3` | Height in blocks above the grabber at which a lifter hovers. |
| `grabbedPokemonNoCollision` | `true` | Disables collision, pushing, and ray-picking for grabbed Pokémon to keep interactions clear. |

Example configuration with a small minimum throttle:

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

## Block and entity IDs

| Content | ID | Notes |
| --- | --- | --- |
| Pokémon Grabber block/item | `cobbleupdraft:pokemon_grabber` | Main gameplay block. |
| Grabber Anchor entity | `cobbleupdraft:grabber_anchor` | Invisible internal entity used to synchronize and render the rope. |
| Creative tab | `cobbleupdraft:main` | The mod's CobbleUpdraft creative tab. |

## Requirements

- Minecraft **1.21.1**
- Java **21** or newer
- Cobblemon **1.7.1** or newer
- Architectury API **13.0.8** or newer
- Fabric: Fabric Loader **0.18.0** or newer and Fabric API **0.116.10+1.21.1**
- NeoForge: the **21.1** series
- Optional: Sable **2.0** or newer on NeoForge for aircraft physics lift
- Optional: Create on NeoForge; with Sable installed, it enables goggle information

CobbleUpdraft does not declare Create or Sable as base required dependencies. To apply physical lift to aircraft, install a Sable/Create Aeronautics setup compatible with the Minecraft, NeoForge, and Cobblemon versions in use.

## Installation

1. Install a Fabric or NeoForge instance for Minecraft 1.21.1.
2. Put Cobblemon, Architectury API, the loader-specific dependencies, and CobbleUpdraft into the `mods` folder.
3. For aircraft physics, install Sable or a Create Aeronautics version that provides Sable on NeoForge.
4. For goggle information, install a matching Create build alongside Sable.
5. Start the game and verify the Pokémon Grabber in the CobbleUpdraft creative tab.

## Building from source

CobbleUpdraft uses Architectury and provides Fabric and NeoForge build targets. On Windows, run:

```powershell
.\gradlew.bat :fabric:remapJar :neoforge:remapJar
```

On Linux, macOS, or Git Bash, run:

```bash
./gradlew :fabric:remapJar :neoforge:remapJar
```

Output files:

```text
fabric/build/libs/cobbleupdraft-fabric-1.0.jar
neoforge/build/libs/cobbleupdraft-neoforge-1.0.jar
```

The NeoForge build uses compile-only Sable, Sable Companion, Veil, and Create files from `common/libs/`. These files do not replace the runtime dependencies required by the game instance.

## Known limitations

- Pokémon can only be selected from the player's active party; PC storage selection is not implemented.
- One grabber can hold one Pokémon, and a stationed Pokémon is locked against sending out, recalling, trading, releasing, and battle use.
- The Fabric build has no Sable physics integration and therefore does not apply physical lift to aircraft.
- Sable/Create compatibility depends on the correct NeoForge versions being loaded. Without Sable, the grabber still displays and locks Pokémon but applies no aircraft force.
- A Pokémon with no enabled lift eligibility condition can be stationed and displayed but produces no lift.
- Create heat levels such as `seething` are unrelated to this mod; lift is determined by Pokémon data, redstone throttle, and configuration.
- The current source does not define a custom crafting recipe.

## Licensing

Use, modification, and redistribution permissions are defined by the repository's [LICENSE.txt](LICENSE.txt). The current license file states **All rights reserved**; confirm the permitted scope before redistributing or building derivative works.

## Credits

- Thanks to the developers and maintainers of Cobblemon, Architectury, Sable, Create Aeronautics, Create, and their related dependencies.
- Thanks to everyone who tested the aircraft physics behavior and Pokémon compatibility.
