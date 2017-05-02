# ourtown
But it's our town, love it anyway.

## MultiSpawn plugin

ourtown is a spigot plugin that manages multiple spawn point. Features:

- Random select / player select spawn point on new player join and teleport
- Remember the spawn point of each player
- Does not affect Essentials Spawn

### Commands & Permissions

| Command  | Permission | Description |
| --- | --- | --- |
| `/town spawn set default`  | `town.admin`  | Set default spawn points (for existing players without records) |
| `/town spawn add [name]` | `town.admin` | Add a spawn point at player location |
| `/town spawn del [name]` | `town.admin` | Delete a spawnpoint |
| `/town spawn list` | `town.admin` | List all spawnpoints and position |
| `/town tp [name]` | `town.admin` | Teleport to the specific spawnpoint |
| `/town select [name] [player]` | `town.admin` | Set a player's spawnpoint to [name] |
| `/town select [name]` | `town.player.select` | Select spawnpoint |
| `/town tp` | `town.player.tp` | Teleport to player's spawn |

### Configuration

`config.yml`

* `mode`
  * Option `RANDOM` randomly select a spawnpoint on join
  * Option `SELECT` prompt player to select a spawnpoint, and teleport to there after selection
* `force_spawn` `true` to force teleport to player's spawn point at join everytime. `false` to disable
* `lock_spawn` `true` to lock player spawnpoint after selection or random teleport (only admin may change player's spawnpoint)
* `override_command` `true` to override `/spawn` command with `/town tp`

`spawn.yml`

```
default:
  world: start
  x: 10
  y: 65
  z: 12
'spawn-name':
  world: world
  x: 123
  y: 65
  z: 456
'another-spawn':
  world: world2
  x: 789
  y: 67
  z: 234
```

`player.yml`

```
'player-uuid': 'spawn-name'
```
