package net.gauntletmc.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Converts types to other types, using the CommandSender
 * Currently only used to convert RelativeVec into Vec using RelativeVec#fromSender
 */
public class SenderTypeConverter {

    private static final Map<Class<?>, BiFunction<CommandSender, ?, ?>> ADAPTERS = new HashMap<>();

    static {
        ADAPTERS.put(RelativeVec.class, (BiFunction<CommandSender, RelativeVec, Vec>) (sender, relative) -> {
            return relative.fromSender(sender);
        });
        ADAPTERS.put(EntityFinder.class, (BiFunction<CommandSender, EntityFinder, Player>) (sender, finder) -> {
            Player player = finder.findFirstPlayer(sender);
            if (player == null) {
                sender.sendMessage("Couldn't find player!");

                // Hack to prevent execution with null value
                throw new RuntimeException() {
                    @Override
                    public void printStackTrace() {} // Don't log to console
                };
            }
            return player;
        });
    }

    @SuppressWarnings("unchecked")
    static Object adapt(CommandSender sender, @Nullable Object in) {
        if (in == null) return null;

        BiFunction function = ADAPTERS.get(in.getClass());
        if (function != null) {
            return function.apply(sender, in);
        }
        return in;
    }

}
