package net.gauntletmc.command.examples;

import net.gauntletmc.command.annotations.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

import java.util.Collection;
import java.util.List;

@Alias("example")
public class CommandExample {

    @RequiresPermission("your.permission.name")
    @Alias("teleportUp")
    public void teleportUp(Player sender, @Name("player") Player player, @Optional @Name("amount") int amount) {
        if (player == null) player = sender;
        player.teleport(player.getPosition().add(0, amount <= 0 ? 5 : amount, 0));
    }

    public static Collection<String> getFruits(CommandSender sender, CommandContext context) {
        if (sender instanceof Player player) {
            if (player.getUsername().equalsIgnoreCase("moulberry")) {
                return List.of("mulberry");
            }
        }
        return List.of("peach", "fig", "apple", "banana", "guava", "apricot");
    }

    @Alias("echo")
    public static void print(Player sender, @Name("text") @Greedy @Completions("getFruits") String thingy) {
        sender.sendMessage(Component.text(thingy));
    }

    @Alias("subcategory")
    public static class Subcategory {

        @Alias("ping")
        public static void ping(Player sender) {
            sender.sendMessage(Component.text("Pong!"));
        }

    }

}
