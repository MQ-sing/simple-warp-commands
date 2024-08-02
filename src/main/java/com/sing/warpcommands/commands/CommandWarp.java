package com.sing.warpcommands.commands;

import com.google.common.base.Strings;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.data.WorldDataWaypoints;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandWarp {

    private static List<String> getWarpCompletion(ICommandSender sender, String[] args, int at) {
        WorldDataWaypoints p = WorldDataWaypoints.get(sender.getEntityWorld());
        String arg = args[at];
        if (arg.isEmpty()) return new ArrayList<>(p.wayPoints.keySet());
        return p.wayPoints.keySet().stream().filter(w -> w.startsWith(arg)).collect(Collectors.toList());
    }
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
            if (args.length != 1) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            String name = args[0];
            EntityPos point = WorldDataWaypoints.get(sender.getEntityWorld()).get(name);
            if (point == null) throw new CommandException("warp.not_found", name);
            CapabilityPlayer.PlayerLocations loc = CapabilityPlayer.get(player);
            point.teleport(player, loc);
            player.sendMessage(new TextComponentTranslation("warp.on", name));
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return getWarpCompletion(sender, args, 0);
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
            if (args.length > 2 || args.length < 1) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            WorldDataWaypoints data = WorldDataWaypoints.get(player.world);
            String name = args[0];
            String arg = args.length == 2 ? args[1] : "";
            if (data.has(name) && !arg.contains("!")) {
                throw new CommandException(I18n.format("setwarp.replace", name));
            }
            data.set(name, new EntityPos(player));
            sender.sendMessage(new TextComponentTranslation("setwarp.set"));

        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp+");
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
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return getWarpCompletion(sender, args, 0);
        }
    }

    static class CommandWarpOperation extends CommandBase {

        @Override
        @NonNls
        public @NotNull String getName() {
            return "warpopt";
        }

        @Override
        @NonNls
        public @NotNull String getUsage(@NotNull ICommandSender sender) {
            return "warpopt.usage";
        }

        static final List<String> OPTIONS = Arrays.asList("rename", "move", "get");

        @Override
        public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
            if (args.length < 2) throw new WrongUsageException(this.getUsage(sender));
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            WorldDataWaypoints p = WorldDataWaypoints.get(player.world);
            String name = args[1];
            if (!p.has(name)) throw new CommandException("warp.not_found", name);
            switch (args[0]) {
                case "rename":
                    if (!(args.length == 3)) throw new WrongUsageException(this.getUsage(sender));
                    p.set(args[2], p.wayPoints.remove(name));
                    break;
                case "get":
                    if (!(args.length == 2)) throw new WrongUsageException(this.getUsage(sender));
                    sender.sendMessage(new TextComponentString(getWarpInfoMessage(name, p.wayPoints.get(name))));
                    break;
                case "move":
                    if (!(args.length == 2)) throw new WrongUsageException(this.getUsage(sender));
                    p.set(name, new EntityPos(player));
                    break;
                case "remove":
                    if (!(args.length == 2)) throw new WrongUsageException(this.getUsage(sender));
                    p.remove(name);
                    break;
            }


        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            switch (args.length) {
                case 1:
                    return OPTIONS.stream().filter(i -> i.startsWith(args[0])).collect(Collectors.toList());
                case 2:
                    return getWarpCompletion(sender, args, 1);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.singletonList("warp.");
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
                            data.wayPoints.object2ObjectEntrySet().stream()
                                    .map(i -> getWarpInfoMessage(i.getKey(), i.getValue()))
                                    .collect(Collectors.joining("\n")))
            );
        }

        @Override
        public @NotNull List<String> getAliases() {
            return Collections.emptyList();
        }
    }

    private static @NotNull String getWarpInfoMessage(String name, EntityPos data) {
        final int paddingLen = 15;
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        int nameWidth = renderer.getStringWidth(name);
        int spaceWidth = renderer.getCharWidth(' ');
        int paddedCount = Math.max(((spaceWidth * paddingLen - nameWidth) / spaceWidth), 0);

        NumberFormat posFormat = NumberFormat.getNumberInstance();
        posFormat.setMaximumFractionDigits(1);
        posFormat.setRoundingMode(RoundingMode.HALF_UP);
        return "§2" +
                name +
                Strings.repeat(" ", paddedCount) +
                "§8-" +
                Strings.repeat(" ", 7) +
                "§d" +
                data;
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandWarpTeleport());
        e.registerServerCommand(new CommandWarpList());
        e.registerServerCommand(new CommandWarpDel());
        e.registerServerCommand(new CommandWarpSet());
        e.registerServerCommand(new CommandWarpOperation());
    }
}
