package com.sing.warpcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.data.CapabilityPlayer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Optional;

public class CommandHome {
    public static final SimpleCommandExceptionType HOME_NOT_FOUND = new SimpleCommandExceptionType(new TranslationTextComponent("home.tip"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Utils.command("home").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                    Optional<CapabilityPlayer.PlayerLocations.Position> loc = CapabilityPlayer.get(player).map(cap -> cap.homePosition);
                    if (loc.isPresent() && loc.get().exist()) {
                        loc.get().teleport(player);
                    } else {
                        Utils.solvePlayerRespawnLocation(player)
                                .orElseThrow(HOME_NOT_FOUND::create)
                                .teleport(player);
                    }
                    Utils.sendSuccess("home", TextFormatting.LIGHT_PURPLE, ctx.getSource());
                    return 1;
                })
        );
        LiteralCommandNode<CommandSource> sethome = dispatcher.register(Utils.command("home!").then(
                Commands.literal("reset").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                    CapabilityPlayer.get(player).ifPresent(cap -> cap.homePosition.clear());
                    Utils.sendSuccess("sethome.reset", TextFormatting.DARK_AQUA, ctx.getSource());
                    return 1;
                })
        ).executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            CapabilityPlayer.get(player).ifPresent(cap -> cap.homePosition.relocate(player));
            Utils.sendSuccess("sethome", TextFormatting.DARK_GREEN, ctx.getSource());
            return 1;
        }));
        dispatcher.register(Utils.command("sethome").redirect(sethome));
    }
}
