package com.shinohane.onsao.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * The Class/Method is bound from JNI code, To keep the 
 * compatibility of the code, you shouldn't modify them.
 */
@Target(value={ElementType.METHOD, ElementType.TYPE})
@Retention(value=java.lang.annotation.RetentionPolicy.SOURCE)
public @interface NativeReferenced {

}
