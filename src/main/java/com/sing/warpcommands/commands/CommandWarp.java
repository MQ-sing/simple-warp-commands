package com.sing.warpcommands.commands;

import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.data.WorldDataWaypoints;
import com.sing.warpcommands.utils.WayPoint;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandWarp {
    static class CommandWarpTeleport extends CommandBase {
        @Override
        public @NotNull String getName() {
            return "warp";
        }

        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "warp.usage";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            if (!(sender instanceof EntityPlayerMP)) {
                return;
            }
            if (args.length != 1) throw new WrongUsageException(this.getUsage(sender));
            WayPoint point = WorldDataWaypoints.get(sender.getEntityWorld()).get(args[0]);
            if (point == null) throw new CommandException("warp.not_found", args[0]);
            EntityPlayerMP player = (EntityPlayerMP) sender;
            CapabilityPlayer.PlayerLocations loc = player.getCapability(CapabilityPlayer.cap, null);
            point.setTo(player, loc);
            player.sendMessage(new TextComponentTranslation("warp.on", point.name));

        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            WorldDataWaypoints data = WorldDataWaypoints.get(sender.getEntityWorld());
            return data.wayPoints.values().stream().map(i -> i.name).collect(Collectors.toList());
        }
    }

    static class CommandWarpList extends CommandBase {

        @Override
        public @NotNull String getName() {
            return "warps";
        }

        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "warps.usage";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));

            WorldDataWaypoints data = WorldDataWaypoints.get(sender.getEntityWorld());
            if (data.wayPoints.isEmpty()) {
                throw new CommandException(I18n.format("warps.no_warp"));
            }
            sender.sendMessage(
                    new TextComponentString(
                            data.wayPoints.values().stream()
                                    .map(way -> I18n.format("warps.listItem", way.name,
                                            Math.round(way.x),
                                            Math.round(way.y),
                                            Math.round(way.z)))
                                    .collect(Collectors.joining("\n")))
            );
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp?");
        }
    }

    static class CommandWarpSet extends CommandBase {

        @Override
        public @NotNull String getName() {
            return "setwarp";
        }

        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "setwarp.usage";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            if (!(sender instanceof EntityPlayerMP)) return;
            if (args.length > 2 || args.length < 1) throw new WrongUsageException(this.getUsage(sender));
            WorldDataWaypoints data = WorldDataWaypoints.get(sender.getEntityWorld());
            String name = args[0];
            String arg = args.length == 2 ? args[1] : "";
            if (data.has(name) && !arg.contains("!")) {
                throw new CommandException(I18n.format("setwarp.replace", name));
            }
            data.set(new WayPoint(name, (EntityPlayerMP) sender
            ));
            sender.sendMessage(new TextComponentTranslation("setwarp.set"));

        }

        @Override
        public @NotNull List<String> getAliases() {
            return Arrays.asList("warp!", "warp+");
        }
    }

    static class CommandWarpDel extends CommandBase {

        @Override
        public @NotNull String getName() {
            return "delwarp";
        }

        @Override
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "delwarp.usage";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 1) throw new WrongUsageException(this.getUsage(sender));
            WorldDataWaypoints data = WorldDataWaypoints.get(sender.getEntityWorld());
            if (!data.has(args[0])) throw new CommandException("warp.not_found", args[0]);
            data.remove(args[0]);
            sender.sendMessage(new TextComponentTranslation("delwarp.on"));
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp-");
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            WorldDataWaypoints data = WorldDataWaypoints.get(sender.getEntityWorld());
            return data.wayPoints.values().stream().map(i -> i.name).collect(Collectors.toList());
        }
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandWarpTeleport());
        e.registerServerCommand(new CommandWarpList());
        e.registerServerCommand(new CommandWarpDel());
        e.registerServerCommand(new CommandWarpSet());
    }
}
