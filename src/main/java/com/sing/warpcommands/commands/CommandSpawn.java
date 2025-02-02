package com.sing.warpcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CommandSpawn {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Utils.command("spawn").executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity)) return 0;
            ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntity();
            final MinecraftServer server = ctx.getSource().getServer();
            Utils.solvePlayerRespawnLocation(player).orElseGet(() -> new EntityPos(server.overworld().getSharedSpawnPos(), World.OVERWORLD)).teleport(player);
            Utils.sendSuccess("spawn", TextFormatting.AQUA, ctx.getSource());
            return 1;
        }));
    }
}
