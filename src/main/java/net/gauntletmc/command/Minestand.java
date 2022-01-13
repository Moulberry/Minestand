package net.gauntletmc.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Minestand {

    static BiConsumer<CommandSender, String> ERROR_MESSAGE_HANDLER = (sender, str) -> {
        sender.sendMessage(Component.text(str, NamedTextColor.RED));
    };
    static BiFunction<CommandSender, String, Boolean> PERMISSION_HANDLER = CommandSender::hasPermission;
    static final Map<String, Collection<String>> COMPLETIONS = new HashMap<>();

    public static void setPermissionHandler(BiFunction<CommandSender, String, Boolean> handler) {
        PERMISSION_HANDLER = handler;
    }

    public static void setErrorMessageHandler(BiConsumer<CommandSender, String> handler) {
        ERROR_MESSAGE_HANDLER = handler;
    }

    public static void registerCompletion(String completionId, Collection<String> collection) {
        COMPLETIONS.put(completionId, collection);
    }

    public static void registerPackage(String packageName) {
        try {
            ClassPath classPath = ClassPath.from(Minestand.class.getClassLoader());
            ImmutableSet<ClassPath.ClassInfo> infos = classPath.getTopLevelClasses();
            for(ClassPath.ClassInfo info : infos) {
                Class<?> clazz = Class.forName(info.getName());
                register(clazz);
            }
        } catch(IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void register(Class<?> clazz) {
        MinestandParser.ParsedClass parsed = MinestandParser.parseClass(clazz);
        MinecraftServer.getCommandManager().register(MinestandBridge.createCommand(parsed));
    }

}
