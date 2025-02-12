package com.sing.warpcommands.commands.utils;

import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.Collection;
import java.util.Optional;

public class Utils {
    public static Optional<EntityPos> getPlayerBedLocation(EntityPlayer player, MinecraftServer server) {
        final int dim = player.getSpawnDimension();
        return Optional.ofNullable(player.getBedLocation(dim)).map(pos ->
                EntityPlayer.getBedSpawnLocation(server.getWorld(0), pos, player.isSpawnForced(dim))
        ).map(x -> new EntityPos(x.add(0.5, 0.1, 0.5), dim));
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
