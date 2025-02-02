## Simple Warp Commands

A Minecraft mod that adds the following commands...

### Waypoints

- /warp <name>  
  Teleport to a waypoint
- /setwarp(/warp+)  
  Set a waypoint at current location
- /delwarp(/warp-)  
  Remove a waypoint by name
- /warps  
  Manage waypoints:
  - rename <old> <new>  
    Rename a way point
  - move <name>  
    Update a waypoint's location to your current position.
  - get: get waypoint info  
    Display detailed info about a waypoint.
  - list
    Show all registered waypoints.

### home

- /home  
  Teleport to your home location
- /sethome(/home!)  
  Set the home location.
  - reset  
    Remove your home location.(Then you will be teleported to your bed location)

### tpp

- /tpp [player]  
  Teleport to another player.  
  arguments can be omitted when only 2 players online

### back

- /back  
  Return to your last death location or
  previous position before teleportation (e.g., after using /warp).
- /back!  
  Manually set a backtrack location.

### pos

- /pos  
  Teleport to the location saved via /pos!
- /pos!  
  Save a location for /pos teleportation

### Misc

- /bye(/kil)  
  Just kill yourself

### Tips

With [JustEnoughCharacters](https://www.curseforge.com/minecraft/mc-mods/just-enough-characters) installed,
warp commands(e.g. /warp) will support PinYin autocompletion for waypoint names, allowing Chinese users to type them
using phonetic input.