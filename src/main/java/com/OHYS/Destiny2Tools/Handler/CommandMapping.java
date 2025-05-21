package com.OHYS.Destiny2Tools.Handler;

import java.lang.annotation.*;

//注解类
@Retention(RetentionPolicy.RUNTIME) // 运行时保留
@Target(ElementType.TYPE)         // 只能用在类上
public @interface CommandMapping {
    String value(); // 主命令名称
    String[] aliases() default {}; // 命令别名（可选）
}