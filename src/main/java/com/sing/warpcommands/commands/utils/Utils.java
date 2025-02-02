package com.sing.warpcommands.commands.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.Collection;
import java.util.Optional;

public class Utils {
    public static Optional<BlockPos> getPlayerBedLocation(EntityPlayer player, MinecraftServer server) {
        return Optional.ofNullable(player.getBedLocation(0)).map(pos ->
                EntityPlayer.getBedSpawnLocation(server.getWorld(0), pos, false)
        ).map(x -> x.add(0.5, 0.1, 0.5));
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

    public static ITextComponent joinTextComponent(Collection<? extends ITextComponent> collection, ITextComponent splitter) {
        final TextComponentString result = new TextComponentString("");
        boolean first = true;
        for (ITextComponent component : collection) {
            if (!first) {
                result.appendSibling(splitter);
            }
            first = false;
            result.appendSibling(component);
        }
        return result;
    }
}
