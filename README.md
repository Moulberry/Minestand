# Minestand
Annotation-based command framework for Minestom

## Advantages over other frameworks
Minestand works by converting your classes into Minestom commands  
All the command parsing, handling, etc. is done by Minestom, allowing this library to be relatively lightweight  
Uses MethodHandles/LambdaMetaFactory for invokation instead of reflection  

## Getting Started
1. Add Minestand as a dependency:
```
dependencies {
     implementation 'com.github.Moulberry:Minestand:-SNAPSHOT'
}
```
2. Create a command (see below for example)
3. Register the command using `Minestand#register`


# Example Usage (from test/main/java/net/gauntletmc/command/examples)
```java
@Alias("example")
public class CommandExample {

    @DefaultCommand
    public void thisIsDefault(Player sender) {
        sender.sendMessage(Component.text("Try /example echo"));
    }

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
```
