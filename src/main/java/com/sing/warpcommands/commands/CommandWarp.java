package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.data.WorldDataWaypoints;
import com.sing.warpcommands.utils.EntityPos;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandWarp {

    private static ObjectSet<String> waypointsName(MinecraftServer server) {
        WorldDataWaypoints p = WorldDataWaypoints.get(server.getEntityWorld());
        return p.wayPoints.keySet();
    }

    @NotNull
    private static EntityPos findWayPoint(String name, WorldDataWaypoints data) throws CommandException {
        EntityPos p = data.get(name);
        if (p == null) throw new CommandException("warp.not_found", name);
        return p;
    }

    private static String waypointName(String s) {
        return "§2" + s + "§r";
    }

    static class CommandWarpTeleport extends AbstractCommand {
        @Override
        public @NotNull String getName() {
            return "warp";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            String name = firstArgOnly(args);
            EntityPlayerMP player = asPlayer(sender);
            EntityPos point = findWayPoint(name, WorldDataWaypoints.get(player.world));
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            point.teleport(player, loc);
            sendSuccess(sender, waypointName(name));
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return optionsStartsWith(args[0], waypointsName(server));
        }
    }


    static class CommandWarpSet extends AbstractCommand {

        @Override
        public @NotNull String getName() {
            return "setwarp";
        }

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            String name = firstArgOnly(args);
            EntityPlayerMP player = asPlayer(sender);
            WorldDataWaypoints data = WorldDataWaypoints.get(player.world);
            if (data.has(name)) {
                throw new CommandException(I18n.format("setwarp.replace", "'" + name + "'"));
            }
            data.set(name, new EntityPos(player));
            sendSuccess(sender, waypointName(name));
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp+");
        }
    }

    static class CommandWarpDel extends AbstractCommand {

        @Override
        public @NotNull String getName() {
            return "delwarp";
        }
        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            String name = firstArgOnly(args);
            WorldDataWaypoints data = WorldDataWaypoints.get(sender.getEntityWorld());
            if (!data.remove(name)) throw new CommandException("warp.not_found", name);
            sendSuccess(sender, waypointName(name));
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp-");
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return optionsStartsWith(args[0], waypointsName(server));
        }
    }

    static class CommandWarpOperation extends AbstractCommand {

        @Override
        @NonNls
        public @NotNull String getName() {
            return "warpopt";
        }
        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            EntityPlayerMP player = asPlayer(sender);
            WorldDataWaypoints p = WorldDataWaypoints.get(player.world);
            if (args[0].equals("list")) {
                argumentsInLength(args, 1);
                if (p.wayPoints.isEmpty()) {
                    throw new CommandException(I18n.format("warps.no_warp"));
                }
                int paddingLen = Math.max(p.wayPoints.keySet().stream()
                                .max(Comparator.comparingInt(String::length))
                                .get()
                                .length() + 5,
                        13);

                sender.sendMessage(
                        new TextComponentString(
                                p.wayPoints.object2ObjectEntrySet().stream()
                                        .map(i -> getWarpInfoMessage(i.getKey(), i.getValue(), paddingLen))
                                        .collect(Collectors.joining("\n")))
                );
                return;
            }

            String name = args[1];
            if (!p.has(name)) throw new CommandException("warp.not_found", name);
            switch (args[0]) {
                case "rename":
                    argumentsInLength(args, 3);
                    p.set(args[2], p.wayPoints.remove(name));
                    sendSuccess("warpopt.rename", sender, waypointName(name), waypointName(args[2]));
                    break;
                case "get":
                    argumentsInLength(args, 2);
                    sender.sendMessage(new TextComponentString(getWarpInfoMessage(name, p.get(name), 10)));
                    break;
                case "move":
                    argumentsInLength(args, 2);
                    p.set(name, new EntityPos(player));
                    sendSuccess("warpopt.move", sender, waypointName(name));
                    break;
                case "remove":
                    argumentsInLength(args, 2);
                    p.remove(name);
                    sendSuccess("delwarp", sender, waypointName(name));
                    break;
                default:
                    badUsage();
            }
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            switch (args.length) {
                case 1:
                    return optionsStartsWith(args[0], "rename", "get", "move", "remove", "list");
                case 2:
                    if (!args[0].equals("list")) return optionsStartsWith(args[0], waypointsName(server));
            }
            return Collections.emptyList();
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp.");
        }
    }

    private static String spaceString(int count) {
        final char[] res = new char[count];
        Arrays.fill(res, ' ');
        return new String(res);
    }

    private static @NotNull String getWarpInfoMessage(String name, EntityPos data, int paddingLen) {
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        int nameWidth = renderer.getStringWidth(name);
        int spaceWidth = renderer.getCharWidth(' ');
        int paddedCount = Math.max(((spaceWidth * paddingLen - nameWidth) / spaceWidth), 0);

        return "§2" +
                name +
                spaceString(paddedCount) +
                "§8-" +
                spaceString(5) +
                "§d" +
                data;
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandWarpTeleport());
        e.registerServerCommand(new CommandWarpDel());
        e.registerServerCommand(new CommandWarpSet());
        e.registerServerCommand(new CommandWarpOperation());
    }
}
