package net.gauntletmc.command.functional;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;

import java.util.Collection;

@FunctionalInterface
public interface CommandCompletion {

    Collection<String> apply(CommandSender sender, CommandContext context);

}
