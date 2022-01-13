package net.gauntletmc.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import net.gauntletmc.command.functional.ErrorMessageHandler;
import net.gauntletmc.command.functional.PermissionHandler;
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

    static ErrorMessageHandler ERROR_MESSAGE_HANDLER = (sender, str) -> {
        sender.sendMessage(Component.text(str, NamedTextColor.RED));
    };
    static PermissionHandler PERMISSION_HANDLER = CommandSender::hasPermission;

    public static void setPermissionHandler(PermissionHandler handler) {
        PERMISSION_HANDLER = handler;
    }

    public static void setErrorMessageHandler(ErrorMessageHandler handler) {
        ERROR_MESSAGE_HANDLER = handler;
    }

    public static void registerPackage(String packageName) {
        try {
            ClassPath classPath = ClassPath.from(Minestand.class.getClassLoader());
            ImmutableSet<ClassPath.ClassInfo> infos = classPath.getTopLevelClasses(packageName);
            for(ClassPath.ClassInfo info : infos) {
                System.out.println("Loading class: " + info.getName());
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
