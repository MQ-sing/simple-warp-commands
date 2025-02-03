package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.utils.EntityPos;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandSpawn {
    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    static class CommandSpawnTeleport extends AbstractCommand {

        @Override
        public String getName() {
            return "spawn";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            EntityPlayerMP player = asPlayer(sender);
            final WorldServer world = server.getWorld(player.getSpawnDimension());
            if (args.length == 1 && !args[0].equals("world") || args.length > 1) badUsage();
            final Optional<EntityPos> targetPos = args.length == 0 ? Utils.getPlayerBedLocation(player, server) : Optional.empty();
            targetPos.orElseGet(() -> new EntityPos(world.provider.getDimension(), world.provider.getRandomizedSpawnPoint())).teleport(player);
            sendSuccess(TextFormatting.AQUA, player);
        }

        @Override
        public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
            return args.length == 1 ? optionsStartsWith(args[0], "world") : Collections.emptyList();
        }
    }

    public static void init(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandSpawnTeleport());
    }
}
