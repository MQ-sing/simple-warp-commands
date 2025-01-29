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

public class CommandPos {
    static class CommandPosTeleport extends AbstractCommand {
        @Override
        public @NotNull String getName() {
            return "pos";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            EntityPlayerMP player = playerOperand(sender, args);
            getPlayerCapabilities(player).recordedPosition.teleport(player);
        }
    }

    static class CommandSetPos extends AbstractCommand {

        @Override
        public @NotNull String getName() {
            return "setpos";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            EntityPlayerMP player = playerOperand(sender, args);
            getPlayerCapabilities(player).recordedPosition.relocate(player);
            sendSuccessSet(sender);
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("pos!");
        }
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandPosTeleport());
        e.registerServerCommand(new CommandSetPos());
    }
}
