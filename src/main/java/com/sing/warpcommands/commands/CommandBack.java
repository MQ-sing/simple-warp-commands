package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CommandBack {

    static class CommandBackTeleport extends AbstractCommand {
        @Override
        public @NotNull String getName() {
            return "back";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            EntityPlayerMP player = playerOperand(sender, args);
            getPlayerCapabilities(player).backPosition.teleport(player);
        }
    }

    static class CommandSetBack extends AbstractCommand {

        @Override
        public @NotNull String getName() {
            return "setback";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            EntityPlayerMP player = playerOperand(sender, args);
            getPlayerCapabilities(player).backPosition.relocate(player);
            sendSuccessSet(sender);
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
