package com.sing.warpcommands.commands.utils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sing.warpcommands.Configure;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Optional;

public class Utils {
    public static Optional<EntityPos> solvePlayerRespawnLocation(ServerPlayerEntity player) {
        BlockPos respawnPosition = player.getRespawnPosition();
        final ServerWorld world = player.server.getLevel(player.getRespawnDimension());
        if (respawnPosition == null || world == null) return Optional.empty();
        return PlayerEntity.findRespawnPositionAndUseSpawnBlock(world, respawnPosition, 0, false, false).map(pos -> new EntityPos(pos.x, pos.y, pos.z, 90, 0, player.getRespawnDimension()));
    }

    public static LiteralArgumentBuilder<CommandSource> command(String name) {
        return Commands.literal(name).requires(source -> source.hasPermission(Configure.permissionRequired.get()));
    }

    public static void sendSuccess(String key, TextFormatting style, CommandSource sender, Object... args) {
        sender.sendSuccess(new TranslationTextComponent(key + ".success", args).withStyle(style), false);
    }

    public static void sendSuccess(String key, Style style, CommandSource sender, Object... args) {
        sender.sendSuccess(new TranslationTextComponent(key + ".success", args).withStyle(style), false);
    }

    public static int findFirstSymbol(String str, int startsWith) {
        for (int i = startsWith; i < str.length(); i++) {
            final int type = Character.getType(str.charAt(i));
            //Symbols and punctuations
            if (type >= 20 && type <= 30) return i;
        }
        return -1;
    }

    @FunctionalInterface
    public interface IStringStartsWithCond {
        boolean startsAt(String str, int startsAt, String toFind);
    }

    public static int matchesSubStr(String match, String str, IStringStartsWithCond cond) {
        str = str.toLowerCase();
        match = match.toLowerCase();
        for (int currentIndex = 0; ; currentIndex++) {
            if (cond.startsAt(match, currentIndex, str)) {
                return currentIndex;
            }
            currentIndex = findFirstSymbol(match, currentIndex);
            if (currentIndex == -1) {
                return -1;
            }
        }
    }

    public static IFormattableTextComponent joinTextComponent(Collection<? extends ITextComponent> collection, ITextComponent splitter) {
        final StringTextComponent result = new StringTextComponent("");
        boolean first = true;
        for (ITextComponent component : collection) {
            if (!first) {
                result.append(splitter);
            }
            first = false;
            result.append(component);
        }
        return result;
    }
}
