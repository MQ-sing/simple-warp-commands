package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandHome {
    static class CommandHomeTeleport extends AbstractCommand {
        @Override
        public @NotNull String getName() {
            return "home";
        }

        @Override
        @SuppressWarnings("all")
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            noArguments(args);
            EntityPlayerMP player = asPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            if (loc == null) return;
            EntityPos home = loc.homePosition;
            if (home == null) {
                @Nullable
                BlockPos bedLocation = player.getBedLocation(0);
                if (bedLocation == null) throw new CommandException(I18n.format("home.tip"));
                home = new EntityPos(bedLocation);
            }
            home.teleport(player, loc);
            sendSuccess(sender);
        }
    }

    static class CommandHomeSet extends AbstractCommand {

        @Override
        public @NotNull String getName() {
            return "sethome";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            if (args.length > 1) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = asPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            if (loc == null) return;
            if (args.length == 1) {
                if (args[0].equals("reset")) {
                    loc.homePosition = null;
                    sendSuccess("sethome.reset", sender);
                    return;
                } else throw new WrongUsageException(this.getUsage(sender));
            }
            loc.homePosition = new EntityPos(player);
            sendSuccess(sender);
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

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandHomeTeleport());
        e.registerServerCommand(new CommandHomeSet());
    }
}
