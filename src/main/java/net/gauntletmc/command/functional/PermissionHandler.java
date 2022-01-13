package net.gauntletmc.command.functional;

import net.minestom.server.command.CommandSender;

@FunctionalInterface
public interface PermissionHandler {

    boolean apply(CommandSender sender, String permission);

}
