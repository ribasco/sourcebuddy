package com.ibasco.sourcebuddy.annotations;

import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Controller
@Inherited
public @interface AbstractController {

}
