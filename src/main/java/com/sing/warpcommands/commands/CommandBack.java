package com.sing.warpcommands.commands;

import com.sing.warpcommands.data.CapabilityPlayer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class CommandBack extends CommandBase {

    @Override
    public @NotNull String getName() {
        return "back";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "back.usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));
        if (!(sender instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) sender;
        CapabilityPlayer.PlayerLocations loc = player.getCapability(CapabilityPlayer.cap, null);
        if (loc == null) return;
        if (loc.backPosition == null) throw new CommandException("back.not_found");
        loc.backPosition.setTo(player, loc);
    }
}
