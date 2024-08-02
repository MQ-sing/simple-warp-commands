package com.sing.warpcommands.commands;

import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CommandBack {

    static class CommandBackTeleport extends CommandBase {
        @Override
        public @NotNull String getName() {
            return "back";
        }

        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "back.usage";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            if (loc == null) return;
            if (loc.backPosition == null) throw new CommandException("back.not_found");
            loc.backPosition.teleport(player, loc);
        }
    }

    static class CommandSetBack extends CommandBase {
        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "setback.usage";
        }

        @Override
        public @NotNull String getName() {
            return "setback";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            if (loc == null) return;
            loc.backPosition = new EntityPos(player);
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("back!");
        }
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandBackTeleport());
        e.registerServerCommand(new CommandSetBack());
    }
}
