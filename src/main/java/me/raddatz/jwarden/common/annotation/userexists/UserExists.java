package me.raddatz.jwarden.common.annotation.userexists;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface UserExists {
}