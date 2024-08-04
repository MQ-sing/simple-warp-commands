package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.minecraft.command.CommandBase.notifyCommandListener;

public class CommandNewTeleport extends AbstractCommand {

    @Override
    public @NotNull String getName() {
        return "tpp";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP target = (
                args.length == 1 ?
                        Optional.ofNullable(
                                server.getPlayerList().getPlayerByUsername(args[0])
                        ) :
                server.getPlayerList()
                        .getPlayers().stream()
                        .filter(i -> i != sender)
                        .findAny()
        ).orElseThrow(() -> new CommandException(I18n.format("tpplus.no_target")));

        EntityPlayerMP player = asPlayer(sender);
        new EntityPos(target).teleport(player, player.getCapability(CapabilityPlayer.cap, null));
        notifyCommandListener(sender, this, "tpplus.success", target.getName());
    }

    @Override
    public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
        return optionsStartsWith(args[0], server.getPlayerList()
                .getPlayers().stream()
                .filter(i -> i != sender)
                .map(EntityPlayer::getName)
                .collect(Collectors.toList()));
    }
}
