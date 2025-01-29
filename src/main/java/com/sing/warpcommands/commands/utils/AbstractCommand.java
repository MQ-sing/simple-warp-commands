package com.sing.warpcommands.commands.utils;

import com.sing.warpcommands.Configure;
import com.sing.warpcommands.data.CapabilityPlayer;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractCommand implements ICommand {
    @Contract("->fail")
    public final void badUsage() throws WrongUsageException {
        throw new WrongUsageException(getName() + ".usage");
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return this.getName() + ".usage";
    }

    @Override
    public final boolean checkPermission(@NotNull MinecraftServer server, ICommandSender sender) {
        return !Configure.requireOp || (sender instanceof EntityPlayerMP && server.getPlayerList().canSendCommands(((EntityPlayerMP) sender).getGameProfile()));
    }

    @Override
    public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    @Override
    public final int compareTo(@NotNull ICommand o) {
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

    public static Entity asEntity(ICommandSender sender) throws EntityNotFoundException {
        if (!(sender instanceof Entity)) throw new EntityNotFoundException("");
        return (Entity) sender;
    }

    public static List<String> optionsStartsWith(String arg, String... options) {
        return optionsStartsWith(arg, Arrays.asList(options));
    }

    public static List<String> optionsStartsWith(String arg, Collection<String> options) {
        List<String> res = options.stream().filter(i -> i.regionMatches(true, 0, arg, 0, arg.length())).collect(Collectors.toList());
        return res.isEmpty() ? new ArrayList<>(options) : res;
    }

    public final void argumentsInLength(String[] args, int len) throws WrongUsageException {
        if (args.length != len) badUsage();
    }

    public final String firstArgOnly(String[] args) throws WrongUsageException {
        if (args.length != 1) badUsage();
        return args[0];
    }

    public final void noArguments(String[] args) throws WrongUsageException {
        if (args.length != 0) badUsage();
    }

    //No arguments and the entity are player
    public final EntityPlayerMP playerOperand(ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException {
        noArguments(args);
        return asPlayer(sender);
    }

    public final void sendSuccess(ICommandSender sender, Object... args) {
        sender.sendMessage(new TextComponentTranslation(this.getName() + ".success", args));
    }

    public final void sendSuccess(String key, ICommandSender sender, Object... args) {
        sender.sendMessage(new TextComponentTranslation(key + ".success", args));
    }

    public final CapabilityPlayer.PlayerLocations getPlayerCapabilities(EntityPlayerMP player) throws CommandException {
        return nonNull(CapabilityPlayer.get(player));
    }

    public final void sendSuccessSet(ICommandSender sender) {
        sender.sendMessage(new TextComponentTranslation("locations.beset"));
    }
    @Contract("null->fail")
    public static <T> T nonNull(T obj) throws CommandException {
        if (obj == null) throw new CommandException("Null");
        return obj;
    }

}
