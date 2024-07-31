package com.sing.warpcommands.commands;

import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandHome {
    static class CommandHomeTeleport extends CommandBase {
        @Override
        public @NotNull String getName() {
            return "home";
        }

        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "home.usage";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            if (loc == null) return;
            if (loc.homePosition == null) {
                @Nullable
                BlockPos bedLocation = player.getBedLocation(0);
                if (bedLocation == null) throw new CommandException(I18n.format("home.tip"));
                loc.homePosition = new EntityPos(bedLocation);
            }
            loc.homePosition.teleport(player, loc);
            player.sendMessage(new TextComponentTranslation("home.on"));
        }
    }

    static class CommandHomeSet extends CommandBase {

        @Override
        public @NotNull String getName() {
            return "sethome";
        }

        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "sethome.usage";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            if (args.length > 1) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            if (loc == null) return;
            if (args.length == 1) {
                if (args[0].equals("reset")) {
                    loc.homePosition = null;
                    return;
                } else throw new WrongUsageException(this.getUsage(sender));
            }
            loc.homePosition = new EntityPos(player);
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
