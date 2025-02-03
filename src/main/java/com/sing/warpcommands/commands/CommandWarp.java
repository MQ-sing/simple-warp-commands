package com.sing.warpcommands.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sing.warpcommands.Configure;
import com.sing.warpcommands.WarpCommandsMod;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.data.WorldDataWaypoints;
import com.sing.warpcommands.network.ClientCache;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandWarp {
    private static final WaypointException WAYPOINT_NOT_FOUND = new WaypointException("warp.not_found");
    private static final SimpleCommandExceptionType NO_EXIST_WAYPOINT = new SimpleCommandExceptionType(new TranslationTextComponent("warps.no_warp"));
    private static final SimpleCommandExceptionType BAD_WAYPOINT_MOVE = new SimpleCommandExceptionType(new TranslationTextComponent("warps.invalid_move"));

    private static class WayPointNameArgument implements ArgumentType<String> {
        public static String string(StringReader reader) {
            int space = reader.getString().indexOf(' ', reader.getCursor());
            if (space == -1) space = reader.getTotalLength();
            final String text = reader.getString().substring(reader.getCursor(), space);
            reader.setCursor(space);
            return text;
        }
        @Override
        public String parse(StringReader reader) {
            return string(reader);
        }
    }

    private static class WayPointArgument implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) {
            return WayPointNameArgument.string(reader);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            class StoreStruct implements Comparable<StoreStruct> {
                public final int startIndex;
                public final String name;

                public StoreStruct(int startIndex, String name) {
                    this.startIndex = startIndex;
                    this.name = name;
                }

                @Override
                public int compareTo(@NotNull StoreStruct o) {
                    return Comparator.<StoreStruct>comparingInt(i -> i.startIndex)
                            .thenComparing(i -> i.name).compare(this, o);
                }
            }
            Set<StoreStruct> suggestions = new TreeSet<>();
            if (context.getSource() instanceof CommandSource) {
                CommandSource source = (CommandSource) context.getSource();
                ServerWorld world = source.getLevel();
                final WorldDataWaypoints.IWaypointList waypoints = WorldDataWaypoints.get(world);
                final RegistryKey<World> target = world.dimension();
                for (Map.Entry<String, EntityPos> waypoint : waypoints.entries()) {
                    final int matchIndex = WarpCommandsMod.matchSubStr(waypoint.getKey(), builder.getRemaining());
                    if (Configure.couldTeleportTo(target, waypoint.getValue().dim) && matchIndex != -1) {
                        suggestions.add(new StoreStruct(matchIndex, waypoint.getKey()));
                    }
                }
            } else if (context.getSource() instanceof ClientSuggestionProvider) {
                final Map<String, RegistryKey<World>> keyMap = ClientCache.get();
                final ClientWorld world = Minecraft.getInstance().level;
                final RegistryKey<World> dimension = world != null ? world.dimension() : null;
                for (Map.Entry<String, RegistryKey<World>> waypoint : keyMap.entrySet()) {
                    final int matchIndex = WarpCommandsMod.matchSubStr(waypoint.getKey(), builder.getRemaining());
                    if ((dimension == null || Configure.couldTeleportTo(dimension, waypoint.getValue())) && matchIndex != -1) {
                        suggestions.add(new StoreStruct(matchIndex, waypoint.getKey()));
                    }
                }
            } else return Suggestions.empty();
            suggestions.forEach(i -> builder.suggest(i.name));
            return builder.buildFuture();
        }

        public static WorldDataWaypoints.IWaypointList getData(CommandContext<CommandSource> ctx) {
            return WorldDataWaypoints.get(ctx.getSource().getLevel());
        }

        public static EntityPos getWaypoint(CommandContext<CommandSource> ctx, String name) throws CommandSyntaxException {
            final EntityPos pos = getData(ctx).get(name);
            if (pos == null) throw WAYPOINT_NOT_FOUND.create(name);
            return pos;
        }
    }

    private static IFormattableTextComponent waypointName(String s) {
        return new StringTextComponent(s).setStyle(Style.EMPTY.withColor(Color.fromLegacyFormat(TextFormatting.YELLOW)));
    }

    public static class WaypointException implements CommandExceptionType {
        private final String translationKey;

        public WaypointException(String translationKey) {
            this.translationKey = translationKey;
        }

        public CommandSyntaxException create(String name) {
            return new CommandSyntaxException(this, new TranslationTextComponent(translationKey, waypointName(name)));
        }
    }


    public static final WaypointException WAYPOINT_ALREADY_EXIST = new WaypointException("setwarp.replace");

    public static void register(CommandDispatcher<CommandSource> dispatcher) {

        dispatcher.register(Utils.command("warp").then(
                Commands.argument("waypoint", new WayPointArgument()).executes(ctx -> {
                    final String name = ctx.getArgument("waypoint", String.class);
                    EntityPos target = WayPointArgument.getWaypoint(ctx, name);
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                    target.teleport(player);
                    Utils.sendSuccess("warp", Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE).withItalic(true), ctx.getSource(), waypointName(name));
                    return 1;
                })
        ));
        final LiteralCommandNode<CommandSource> setwarp = dispatcher.register(Utils.command("setwarp").then(
                Commands.argument("name", new WayPointNameArgument()).executes(ctx -> {
                    final String name = ctx.getArgument("name", String.class);
                    final WorldDataWaypoints.IWaypointList waypoints = WayPointArgument.getData(ctx);
                    Entity entity = ctx.getSource().getEntityOrException();
                    if (waypoints.has(name)) throw WAYPOINT_ALREADY_EXIST.create(name);
                    waypoints.set(name, new EntityPos(entity));
                    WarpCommandsMod.syncWaypoints(waypoints, ctx.getSource().getServer());
                    Utils.sendSuccess("setwarp", TextFormatting.GREEN, ctx.getSource(), waypointName(name));
                    return 1;
                })
        ));
        dispatcher.register(Utils.command("warp+").redirect(setwarp));
        final LiteralCommandNode<CommandSource> delwarp = dispatcher.register(Utils.command("delwarp").then(
                Commands.argument("waypoint", new WayPointArgument()).executes(ctx -> {
                    final String name = ctx.getArgument("waypoint", String.class);
                    WorldDataWaypoints.IWaypointList data = WayPointArgument.getData(ctx);
                    if (data.remove(name) == null) throw WAYPOINT_NOT_FOUND.create(name);
                    Utils.sendSuccess("delwarp", TextFormatting.DARK_AQUA, ctx.getSource(), waypointName(name));
                    WarpCommandsMod.syncWaypoints(data, ctx.getSource().getServer());
                    return 1;
                })));
        dispatcher.register(Utils.command("warp-").redirect(delwarp));
        dispatcher.register(Utils.command("warps")
                .executes(CommandWarp::listWaypoints)
                .then(Commands.literal("list").executes(CommandWarp::listWaypoints))
                .then(Commands.literal("move").then(Commands.argument("waypoint", new WayPointArgument()).executes(ctx -> {
                    final String name = ctx.getArgument("waypoint", String.class);
                    final WorldDataWaypoints.IWaypointList waypoints = WayPointArgument.getData(ctx);
                    final EntityPos pos = waypoints.get(name);
                    if (pos == null) throw WAYPOINT_NOT_FOUND.create(name);
                    if (ctx.getSource().getLevel().dimension() != pos.dim) throw BAD_WAYPOINT_MOVE.create();
                    pos.relocate(ctx.getSource().getEntityOrException());
                    Utils.sendSuccess("warps.move", TextFormatting.AQUA, ctx.getSource(), waypointName(name));
                    return 1;
                }))).then(Commands.literal("get").then(Commands.argument("waypoint", new WayPointArgument()).executes(ctx -> {
                    final String name = ctx.getArgument("waypoint", String.class);
                    final EntityPos pos = WayPointArgument.getWaypoint(ctx, name);
                    ctx.getSource().sendSuccess(getWarpInfoMessage(name, pos, Configure.couldTeleportTo(ctx.getSource().getLevel().dimension(), pos.dim)), false);
                    return 1;
                }))).then(Commands.literal("rename").then(Commands.argument("waypoint", new WayPointArgument()).then(
                        Commands.argument("name", new WayPointNameArgument()).executes(ctx -> {
                            final String name = ctx.getArgument("waypoint", String.class);
                            final WorldDataWaypoints.IWaypointList waypoints = WayPointArgument.getData(ctx);
                            final EntityPos moves = waypoints.remove(name);
                            if (moves == null) throw WAYPOINT_NOT_FOUND.create(name);
                            final String newName = ctx.getArgument("name", String.class);
                            waypoints.set(newName, moves);
                            WarpCommandsMod.syncWaypoints(waypoints, ctx.getSource().getServer());
                            Utils.sendSuccess("warps.rename", TextFormatting.AQUA, ctx.getSource(), waypointName(name), waypointName(newName));
                            return 1;
                        })
                ))));
    }

    private static int listWaypoints(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        final WorldDataWaypoints.IWaypointList data = WayPointArgument.getData(ctx);
        if (data.size() == 0) throw NO_EXIST_WAYPOINT.create();
        final Entity entity = ctx.getSource().getEntityOrException();
        final RegistryKey<World> dim = entity.level.dimension();
        class ListElem implements Comparable<ListElem> {
            public final EntityPos pos;
            public final String name;
            public final double distance;

            public ListElem(EntityPos pos, String name, Entity entity) {
                this.pos = pos;
                this.name = name;
                this.distance = pos.distanceSquared(entity.getX(), entity.getY(), entity.getZ());
            }

            @Override
            public int compareTo(@NotNull ListElem target) {
                return Comparator.<ListElem>comparingDouble(elem -> elem.distance)
                        .thenComparing(elem -> elem.pos.dim != dim)
                        .thenComparing(elem -> !Configure.couldTeleportTo(dim, elem.pos.dim))
                        .thenComparing(elem -> elem.pos.dim)
                        .thenComparing(elem -> elem.name)
                        .compare(this, target);
            }
        }
        final Collection<Map.Entry<String, EntityPos>> entries = data.entries();
        Set<ListElem> elems = new TreeSet<>();
        for (Map.Entry<String, EntityPos> entry : entries) {
            final EntityPos pos = entry.getValue();
            elems.add(new ListElem(pos, entry.getKey(), entity));
        }
        ctx.getSource().sendSuccess(
                Utils.joinTextComponent(
                        elems.stream()
                                .map(i -> getWarpInfoMessage(i.name, i.pos, Configure.couldTeleportTo(dim, i.pos.dim)))
                                .collect(Collectors.toList()),
                        new StringTextComponent("\n"))
                , false);

        return data.size();
    }

    private static String dotString(int count) {
        final char[] res = new char[count];
        Arrays.fill(res, '·');
        return new String(res);
    }

    private static @NotNull IFormattableTextComponent getWarpInfoMessage(String name, EntityPos data, boolean ableToTeleport) {
        FontRenderer renderer = Minecraft.getInstance().font;
        int nameWidth = renderer.width(name);
        int spaceWidth = renderer.width("·");
        final String dataString = data.toString();
        final int dataWidth = renderer.width(dataString);
        int paddedCount = Math.max(((Minecraft.getInstance().gui.getChat().getWidth() - nameWidth - dataWidth - 5) / spaceWidth), 0);
        return waypointName(name)
                .append(new StringTextComponent(
                        dotString(paddedCount)).withStyle(TextFormatting.DARK_GRAY))
                .append(
                        new StringTextComponent(dataString).withStyle(TextFormatting.LIGHT_PURPLE)
                ).withStyle(Style.EMPTY.withItalic(!ableToTeleport));
    }
}
