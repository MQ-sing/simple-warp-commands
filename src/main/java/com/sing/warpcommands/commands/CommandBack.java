package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
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
            noArguments(args);
            EntityPlayerMP player = asPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            if (loc == null) return;
            if (loc.backPosition == null) throw new CommandException("back.not_found");
            loc.backPosition.teleport(player, loc);
        }
    }

    static class CommandSetBack extends AbstractCommand {

        @Override
        public @NotNull String getName() {
            return "setback";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            noArguments(args);
            EntityPlayerMP player = asPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            nonNull(loc);
            loc.backPosition = new EntityPos(player);
            sender.sendMessage(new TextComponentTranslation("setback.on"));
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
