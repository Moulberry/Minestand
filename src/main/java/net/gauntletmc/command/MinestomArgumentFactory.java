package net.gauntletmc.command;

import net.gauntletmc.command.annotations.*;
import net.gauntletmc.command.arguments.ArgumentGreedyString;
import net.gauntletmc.command.exception.CommandParseException;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;
import java.util.UUID;

class MinestomArgumentFactory {

    @SuppressWarnings("unchecked")
    static Argument<?> getForClass(Class<?> clazz, Parameter parameter, String name) {
        if (clazz == boolean.class || clazz == Boolean.class) {
            return ArgumentType.Boolean(name);
        } else if (clazz == double.class || clazz == Double.class) {
            return ArgumentType.Double(name);
        } else if (clazz == float.class || clazz == Float.class) {
            return ArgumentType.Float(name);
        } else if (clazz == int.class || clazz == Integer.class) {
            return ArgumentType.Integer(name);
        } else if (clazz == long.class || clazz == Long.class) {
            return ArgumentType.Long(name);
        } else if (clazz == String.class) {
            if (parameter.isAnnotationPresent(Greedy.class)) {
                return new ArgumentGreedyString(name);
            } else {
                return ArgumentType.String(name);
            }
        } else if (clazz == Player.class) {
            return ArgumentType.Entity(name).singleEntity(true).onlyPlayers(true);
        } else if (clazz == Vec.class || clazz == Point.class) {
            if (parameter.isAnnotationPresent(BlockPos.class)) {
                return ArgumentType.RelativeBlockPosition(name);
            } else if (parameter.isAnnotationPresent(Vec2.class)) {
                return ArgumentType.RelativeVec2(name);
            } else {
                return ArgumentType.RelativeVec3(name);
            }
        } else if (clazz == UUID.class) {
            return ArgumentType.UUID(name);
        } else if (clazz.isEnum()) {
            return ArgumentType.Enum(name, (Class<? extends Enum<?>>) clazz);
        } else if (clazz == ItemStack.class) {
            return ArgumentType.ItemStack(name);
        }
        throw new CommandParseException("Unknown argument type: " + clazz);
    }

}
