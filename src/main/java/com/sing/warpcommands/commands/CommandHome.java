package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandHome {
    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandHomeTeleport());
        e.registerServerCommand(new CommandHomeSet());
    }

    static class CommandHomeTeleport extends AbstractCommand {
        @Override
        public @NotNull String getName() {
            return "home";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            EntityPlayerMP player = playerOperand(sender, args);
            CapabilityPlayer.PlayerLocations loc = getPlayerCapabilities(player);
            if (loc.homePosition.exist()) {
                loc.homePosition.teleport(player);
            } else {
                Utils.getPlayerBedLocation(player, server)
                        .map((pos) -> new EntityPos(0, pos))
                        .orElseThrow(() -> new CommandException(I18n.format("home.tip")))
                        .teleport(player);
            }
            sendSuccess(TextFormatting.LIGHT_PURPLE, sender);
        }
    }

    static class CommandHomeSet extends AbstractCommand {

        @Override
        public @NotNull String getName() {
            return "sethome";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            EntityPlayerMP player = asPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = getPlayerCapabilities(player);
            switch (args.length) {
                case 0:
                    loc.homePosition.relocate(player);
                    sendSuccess(TextFormatting.DARK_GREEN, sender);
                    break;
                case 1:
                    if (!args[0].equals("reset")) badUsage();
                    loc.homePosition.clear();
                    sendSuccess("sethome.reset", TextFormatting.DARK_AQUA, sender);
                    break;
                default:
                    badUsage();
            }
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return optionsStartsWith(args[0], "reset");
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("home!");
        }
    }
}
