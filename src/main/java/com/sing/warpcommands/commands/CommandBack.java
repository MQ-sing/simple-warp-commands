package com.sing.warpcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sing.warpcommands.commands.utils.Utils;
import com.sing.warpcommands.data.CapabilityPlayer;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Optional;

public class CommandBack {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Utils.command("back").executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            final Optional<CapabilityPlayer.PlayerLocations.Position> pos = CapabilityPlayer.get(player).map(cap -> cap.backPosition);
            if (pos.isPresent()) {
                pos.get().teleport(player);
            }
            return 1;
        }));
        final LiteralCommandNode<CommandSource> setback = dispatcher.register(Utils.command("back!").executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            CapabilityPlayer.get(player).ifPresent(cap -> cap.backPosition.relocate(player));
            ctx.getSource().sendSuccess(new TranslationTextComponent("locations.beset").withStyle(TextFormatting.DARK_BLUE), false);
            return 1;
        }));
        dispatcher.register(Utils.command("setback").redirect(setback));
    }
}
