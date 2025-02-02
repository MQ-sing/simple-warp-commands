package com.sing.warpcommands.commands;

import com.sing.warpcommands.Configure;
import com.sing.warpcommands.WarpCommandsMod;
import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.data.WorldDataWaypoints;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandWarp {

    private static ITextComponent waypointName(String s) {
        return new TextComponentString(s).setStyle(new Style().setColor(TextFormatting.YELLOW));
    }

    private static List<String> completeWaypoint(World world, String name) {
        class StoreStruct {
            public final int startIndex;
            public final String name;

            public StoreStruct(int startIndex, String name) {
                this.startIndex = startIndex;
                this.name = name;
            }
        }
        List<StoreStruct> suggestions = new ArrayList<>();
        final WorldDataWaypoints.IWaypointList waypoints = WorldDataWaypoints.get(world);
        final int target = world.provider.getDimension();
        for (Map.Entry<String, EntityPos> waypoint : waypoints.entries()) {
            final int matchIndex = WarpCommandsMod.matchSubStr(waypoint.getKey(), name);
            if (Configure.couldTeleportTo(target, waypoint.getValue().dim) && matchIndex != -1) {
                suggestions.add(new StoreStruct(matchIndex, waypoint.getKey()));
            }
        }
        return suggestions.stream().sorted(Comparator.comparingInt(i -> i.startIndex)).map(i -> i.name).collect(Collectors.toList());
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
            EntityPos p = WorldDataWaypoints.get(player.world).get(name);
            if (p == null) throw new CommandException("warp.not_found", name);
            p.teleport(player);
            sendSuccess(new Style().setColor(TextFormatting.LIGHT_PURPLE).setItalic(true), sender, waypointName(name));
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return completeWaypoint(sender.getEntityWorld(), args[0]);
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
            Entity entity = asEntity(sender);
            WorldDataWaypoints.IWaypointList data = WorldDataWaypoints.get(entity.world);
            if (data.has(name)) {
                throw new CommandException(I18n.format("setwarp.replace", name));
            }
            data.set(name, new EntityPos(entity));
            sendSuccess(TextFormatting.GREEN, sender, waypointName(name));
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
            WorldDataWaypoints.IWaypointList data = WorldDataWaypoints.get(sender.getEntityWorld());
            if (data.remove(name) == null) throw new CommandException("warp.not_found", name);
            sendSuccess(TextFormatting.DARK_AQUA, sender, waypointName(name));
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp-");
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return completeWaypoint(sender.getEntityWorld(), args[0]);
        }
    }

    static class CommandWarpOperation extends AbstractCommand {

        @Override
        @NonNls
        public @NotNull String getName() {
            return "warps";
        }
        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            EntityPlayerMP player = asPlayer(sender);
            WorldDataWaypoints.IWaypointList data = WorldDataWaypoints.get(player.world);
            final int dim = player.dimension;
            if (args.length == 0 || args[0].equals("list")) {
                if (args.length > 1) badUsage();
                Map<EntityPos, Double> distances = data.values().stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                pos -> pos.dim == dim ?
                                        pos.distanceSquared(player.posX, player.posY, player.posZ) :
                                        Double.POSITIVE_INFINITY
                        ));
                sender.sendMessage(
                        Utils.joinTextComponent(
                                data.entries().stream()
                                        .sorted(
                                                Comparator.<Map.Entry<String, EntityPos>>comparingDouble(pos -> distances.get(pos.getValue()))
                                                        .thenComparing(pos -> pos.getValue().dim != dim)
                                                        .thenComparing(pos -> !Configure.couldTeleportTo(dim, pos.getValue().dim))
                                                        .thenComparing(pos -> pos.getValue().dim)
                                                        .thenComparing(Map.Entry::getKey)
                                        )
                                        .map(i -> getWarpInfoMessage(i.getKey(), i.getValue(), Configure.couldTeleportTo(dim, i.getValue().dim)))
                                        .collect(Collectors.toList()),
                                new TextComponentString("\n"))
                );
                return;
            }
            if (args.length < 2) badUsage();
            String name = args[1];
            if (!data.has(name)) throw new CommandException("warp.not_found", name);
            switch (args[0]) {
                case "rename":
                    argumentsInLength(args, 3);
                    data.set(args[2], data.remove(name));
                    sendSuccess("warps.rename", TextFormatting.AQUA, sender, waypointName(name), waypointName(args[2]));
                    break;
                case "get":
                    argumentsInLength(args, 2);
                    sender.sendMessage(getWarpInfoMessage(name, Objects.requireNonNull(data.get(name)), Configure.couldTeleportTo(dim, data.get(name).dim)));
                    break;
                case "move":
                    argumentsInLength(args, 2);
                    data.set(name, new EntityPos(player));
                    sendSuccess("warps.move", TextFormatting.AQUA, sender, waypointName(name));
                    break;
                default:
                    badUsage();
            }
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            switch (args.length) {
                case 1:
                    return optionsStartsWith(args[0], "rename", "get", "move", "list");
                case 2:
                    if (!args[0].equals("list"))
                        return completeWaypoint(sender.getEntityWorld(), args[0]);
            }
            return Collections.emptyList();
        }
    }

    private static String dotString(int count) {
        final char[] res = new char[count];
        Arrays.fill(res, '·');
        return new String(res);
    }

    private static @NotNull ITextComponent getWarpInfoMessage(String name, EntityPos data, boolean ableToTeleport) {
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        int nameWidth = renderer.getStringWidth(name);
        int spaceWidth = renderer.getStringWidth("·");
        final String dataString = data.toString();
        final int dataWidth = renderer.getStringWidth(dataString);
        int paddedCount = Math.max(((Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatWidth() - nameWidth - dataWidth - 5) / spaceWidth), 0);
        return waypointName(name)
                .appendSibling(new TextComponentString(
                        dotString(paddedCount)).setStyle(new Style().setColor(TextFormatting.DARK_GRAY)))
                .appendSibling(
                        new TextComponentString(dataString).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE))
                ).setStyle(new Style().setItalic(!ableToTeleport));
    }
    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandWarpTeleport());
        e.registerServerCommand(new CommandWarpDel());
        e.registerServerCommand(new CommandWarpSet());
        e.registerServerCommand(new CommandWarpOperation());
    }
}
