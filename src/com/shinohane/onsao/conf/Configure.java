package com.shinohane.onsao.conf;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This field stands for a configurable item
 */
@Target(value={ElementType.FIELD})
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Configure {
	
	/**
	 * Resource ID of icon
	 */
	int icon() default 0;

	/**
	 * Resource ID of title text
	 */
	int title() default 0;

	/**
	 * Resource ID of description text
	 */
	int describe() default 0;
	
	/**
	 * Key
	 */
	String key();

	/**
	 * Representation type of this item
	 */
	ReprType repr() default ReprType.Text;
	
	/**
	 * Is this item visible
	 */
	boolean visible() default true;
	
	/**
	 * Tweakable by user?
	 */
	boolean enabled() default true;
	
	/**
	 * Extra data string
	 */
	String data() default "";
}
