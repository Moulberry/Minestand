# Minestand
Annotation-based command framework for Minestom

# Getting Started
1. Add Minestand as a maven dependency:
```
dependencies {
     implementation 'com.github.Moulberry:Minestand:-SNAPSHOT'
}```
2. Create a command (see below for example)
3. Register the command using `Minestand#register`


# Example Usage (from src/main/java/net/gauntletmc/command/examples)
```java
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
}```
