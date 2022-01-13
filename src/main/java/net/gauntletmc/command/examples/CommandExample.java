package net.gauntletmc.command.examples;

import net.gauntletmc.command.Minestand;
import net.gauntletmc.command.annotations.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.List;

@Alias("example")
public class CommandExample {

    public CommandExample() {
        Minestand.registerCompletion("fruits", List.of(
                "peach", "fig", "apple", "banana", "guava", "apricot"
        ));
    }

    @Alias("teleportUp")
    public void teleportUp(Player sender, @Name("player") Player player, @Optional @Name("amount") int amount) {
        if (player == null) player = sender;
        player.teleport(player.getPosition().add(0, amount <= 0 ? 5 : amount, 0));
    }

    @Alias("echo")
    public static void print(Player sender, @Name("text") @Greedy @Completions("fruits") String thingy) {
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
