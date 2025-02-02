package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommandSpawn {
    static class CommandSpawnTeleport extends AbstractCommand {

        @Override
        public String getName() {
            return "spawn";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            noArguments(args);
            EntityPlayerMP player = asPlayer(sender);
            EntityPos.teleport(Utils.getPlayerBedLocation(player, server).orElseGet(() -> (DimensionManager.getWorld(0).getSpawnPoint())), player);
            sendSuccess(TextFormatting.AQUA, player);
        }
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandSpawnTeleport());
    }
}
