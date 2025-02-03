package com.sing.warpcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CommandSpawn {
    private static EntityPos getWorldSpawn(MinecraftServer server) {
        return new EntityPos(server.overworld().getSharedSpawnPos(), World.OVERWORLD);
    }
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Utils.command("spawn").then(Commands.literal("world").executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            final MinecraftServer server = ctx.getSource().getServer();
            getWorldSpawn(server).teleport(player);
            Utils.sendSuccess("spawn", TextFormatting.AQUA, ctx.getSource());
            return 1;
        })).executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            final MinecraftServer server = ctx.getSource().getServer();
            Utils.solvePlayerRespawnLocation(player).orElseGet(() -> getWorldSpawn(server)).teleport(player);
            Utils.sendSuccess("spawn", TextFormatting.AQUA, ctx.getSource());
            return 1;
        }));
    }
}
