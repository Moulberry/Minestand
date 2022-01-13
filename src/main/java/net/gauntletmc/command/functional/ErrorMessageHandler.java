package net.gauntletmc.command.functional;

import net.minestom.server.command.CommandSender;

@FunctionalInterface
public interface ErrorMessageHandler {

    void accept(CommandSender sender, String message);

}
