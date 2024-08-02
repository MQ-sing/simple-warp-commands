package com.sing.warpcommands.commands.utils;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractCommand implements ICommand {
    @Contract("->fail")
    public void badUsage() throws WrongUsageException {
        throw new WrongUsageException(getName() + ".usage");
    }

    @Override
    public final @NotNull String getUsage(@NotNull ICommandSender sender) {
        return this.getName() + ".usage";
    }

    @Override
    public boolean checkPermission(@NotNull MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(3, this.getName());
    }

    @Override
    public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public boolean isUsernameIndex(String @NotNull [] args, int index) {
        return false;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return Collections.emptyList();
    }

    public static EntityPlayerMP asPlayer(ICommandSender sender) throws PlayerNotFoundException {
        if (!(sender instanceof EntityPlayerMP)) throw new PlayerNotFoundException("commands.generic.player.notFound");
        return (EntityPlayerMP) sender;
    }

    public static List<String> optionsStartsWith(String arg, String... options) {
        return optionsStartsWith(arg, Arrays.asList(options));
    }

    public static List<String> optionsStartsWith(String arg, Collection<String> options) {
        List<String> res = options.stream().filter(i -> i.regionMatches(true, 0, arg, 0, arg.length())).collect(Collectors.toList());
        return res.isEmpty() ? new ArrayList<>(options) : res;
    }

    public void argumentsInLength(String[] args, int len) throws WrongUsageException {
        if (args.length != len) badUsage();
    }

    public void argumentsAtLeast(String[] args, int len) throws WrongUsageException {
        if (args.length < len) badUsage();
    }

    public String firstArgOnly(String[] args) throws WrongUsageException {
        if (args.length != 1) badUsage();
        return args[0];
    }

    public void noArguments(String[] args) throws WrongUsageException {
        if (args.length != 0) badUsage();
    }

    public void sendSuccess(ICommandSender sender, Object... args) {
        sender.sendMessage(new TextComponentTranslation(this.getName() + ".success", args));
    }

    public void sendSuccess(String key, ICommandSender sender, Object... args) {
        sender.sendMessage(new TextComponentTranslation(key + ".success", args));
    }

    @Contract("null->fail")
    public void nonNull(Object obj) throws CommandException {
        if (obj == null) throw new CommandException("Null");
    }
}
