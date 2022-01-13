package net.gauntletmc.command.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the pretty name for an arguments
 *
 * By default, the arguments will be named "arg1", "arg2", etc.
 * Unless you use the `-parameters` compiler arg, in which case the name will match the param nam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Name {

    String value();

}
