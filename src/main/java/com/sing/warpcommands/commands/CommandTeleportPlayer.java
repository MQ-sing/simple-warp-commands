package com.sing.warpcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sing.warpcommands.Configure;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.minecraft.command.arguments.EntityArgument.NO_PLAYERS_FOUND;


public class CommandTeleportPlayer {
    private static class OtherPlayersArgument implements ArgumentType<String> {
        @Override
        public String parse(StringReader stringReader) throws CommandSyntaxException {
            return stringReader.readString();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            if (!(context.getSource() instanceof CommandSource)) return builder.buildFuture();
            CommandSource source = (CommandSource) context.getSource();
            ServerWorld world = source.getLevel();
            List<String> names = world.players().stream().filter(player -> Configure.couldTeleportTo(world.dimension(), player.level.dimension())).map(player -> player.getGameProfile().getName()).collect(Collectors.toList());
            for (String name : names) {
                if (Utils.matchesSubStr(builder.getRemaining(), name, (a, starts, b) -> a.startsWith(b, starts)) != -1)
                    builder.suggest(name);
            }
            return builder.buildFuture();
        }

        public static ServerPlayerEntity player(String name, CommandContext<CommandSource> ctx) throws CommandSyntaxException {
            final ServerPlayerEntity player = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
            if (player == null) throw NO_PLAYERS_FOUND.create();
            return player;
        }
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Utils.command("tpp").then(
                Commands.argument("target", new OtherPlayersArgument()).executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                    execute(player, OtherPlayersArgument.player(ctx.getArgument("target", String.class), ctx));
                    return 1;
                })
        ).executes(ctx -> {
            if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity)) return 0;
            ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntity();
            final MinecraftServer server = ctx.getSource().getServer();
            final List<ServerPlayerEntity> list = server.getPlayerList().getPlayers().stream().filter(otherPlayer -> otherPlayer != player).collect(Collectors.toList());
            if (list.isEmpty()) throw NO_PLAYERS_FOUND.create();
            execute(player, list.size() == 1 ? list.get(0) : list.get(player.getRandom().nextInt(list.size())));
            return 1;
        }));
    }

    private static void execute(ServerPlayerEntity from, ServerPlayerEntity to) throws CommandSyntaxException {
        EntityPos.teleport(from, to);
    }
}
