package com.ibasco.sourcebuddy.annotations;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
@Inherited
public @interface AbstractService {

}
