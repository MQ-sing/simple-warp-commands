package com.sing.warpcommands.commands.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class Utils {
    public static Optional<BlockPos> getPlayerBedLocation(EntityPlayer player, MinecraftServer server) {
        return Optional.ofNullable(player.getBedLocation(0)).map(pos ->
                EntityPlayer.getBedSpawnLocation(server.getWorld(0), pos, false)
        ).map(x -> x.add(0.5, 0.1, 0.5));
    }
}
