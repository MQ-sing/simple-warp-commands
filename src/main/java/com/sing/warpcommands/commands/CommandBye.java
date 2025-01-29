package com.sing.warpcommands.commands;

import com.sing.warpcommands.commands.utils.AbstractCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class CommandBye extends AbstractCommand {

    @Override
    public @NotNull String getName() {
        return "bye";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        noArguments(args);
        EntityPlayerMP player = asPlayer(sender);
        player.onKillCommand();
    }
}
