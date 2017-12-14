package com.mob.demo.mobim.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the annotated element represents a packed color
 * int, {@code AARRGGBB}. If applied to an int array, every element
 * in the array represents a color integer.
 * <p>
 * Example:
 * <pre>{@code
 *  public abstract void setTextColor(&#64;ColorInt int color);
 * }</pre>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.FIELD})
public @interface ColorInt {
}
