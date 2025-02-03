package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.utils.EntityPos;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import javax.annotation.ParametersAreNonnullByDefault;

public class CommandSpawn {
    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    static class CommandSpawnTeleport extends AbstractCommand {

        @Override
        public String getName() {
            return "spawn";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            noArguments(args);
            EntityPlayerMP player = asPlayer(sender);
            final WorldServer world = server.getWorld(player.getSpawnDimension());
            Utils.getPlayerBedLocation(player, server).orElseGet(() -> new EntityPos(world.provider.getDimension(), world.provider.getRandomizedSpawnPoint())).teleport(player);
            sendSuccess(TextFormatting.AQUA, player);
        }
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandSpawnTeleport());
    }
}
