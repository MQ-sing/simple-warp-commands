package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static net.minecraft.command.CommandBase.notifyCommandListener;

public class CommandTeleportPlayer extends AbstractCommand {

    @Override
    public @NotNull String getName() {
        return "tpp";
    }
    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP target;
        EntityPlayerMP player = asPlayer(sender);
        if (args.length == 1) {
            target = server.getPlayerList().getPlayerByUsername(args[0]);
            if (target == null) throw new PlayerNotFoundException("commands.generic.player.notFound", args[0]);
        } else if (args.length == 0) {
            final List<EntityPlayerMP> list = server.getPlayerList()
                    .getPlayers().stream()
                    .filter(i -> i != sender).collect(Collectors.toList());
            if (list.isEmpty()) throw new PlayerNotFoundException("tpplus.no_target");
            target = list.size() == 1 ? list.get(0) : list.get(ThreadLocalRandom.current().nextInt(list.size()));
        } else throw new WrongUsageException(getName() + ".usage");
        EntityPos.teleport(target, player);
        notifyCommandListener(sender, this, "tpplus.success", target.getName());
    }

    @Override
    public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
        if (args.length > 1) return Collections.emptyList();
        List<String> completions = new ArrayList<>();
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player == sender) continue;
            if (Utils.matchesSubStr(player.getName(), args[0], (a, starts, b) -> a.startsWith(b, starts)) != -1)
                return completions;
        }
        return completions;
    }
}
