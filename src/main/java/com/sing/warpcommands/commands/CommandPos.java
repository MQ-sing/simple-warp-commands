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

public class CommandPos {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        //Teleport
        dispatcher.register(Utils.command("pos").executes((ctx) -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            final Optional<CapabilityPlayer.PlayerLocations.Position> pos = CapabilityPlayer.get(player).map(cap -> cap.recordedPosition);
            if (pos.isPresent()) {
                pos.get().teleport(player);
                Utils.sendSuccess("pos", TextFormatting.GOLD, ctx.getSource());
            }
            return 1;
        }));
        final LiteralCommandNode<CommandSource> setpos = dispatcher.register(Utils.command("pos!").executes((ctx) -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            CapabilityPlayer.get(player).ifPresent(cap -> {
                cap.recordedPosition.relocate(player);
                ctx.getSource().sendSuccess(new TranslationTextComponent("locations.beset").withStyle(TextFormatting.BLUE), false);
            });
            return 1;
        }));
        dispatcher.register(Utils.command("setpos").redirect(setpos));
    }
}
