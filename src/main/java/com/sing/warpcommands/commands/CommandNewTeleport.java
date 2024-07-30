package com.sing.warpcommands.commands;

import com.sing.warpcommands.data.CapabilityPlayer;
import com.sing.warpcommands.utils.EntityPos;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandNewTeleport extends CommandBase {

    @Override
    public @NotNull String getName() {
        return "tp~";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return null;
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP target = args.length == 1 ?
                getPlayer(server, sender, args[0]) :
                server.getPlayerList().getPlayers().stream().filter(i -> i != sender).findAny().orElseThrow(() -> new CommandException(I18n.format("tpnew.no_target")));
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        new EntityPos(target).setTo(getCommandSenderAsPlayer(sender), player.getCapability(CapabilityPlayer.cap, null));
        notifyCommandListener(sender, this, "tpnew.success", target);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return server.getPlayerList().getPlayers().stream().filter(i -> i != sender).map(EntityPlayer::getName).collect(Collectors.toList());
    }
}
