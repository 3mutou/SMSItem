package com.ydl.sms.aspect;

import com.ydl.context.BaseContextHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 通过切面方式，自定义注解，实现实体基础数据的注入（创建者、创建时间、修改者、修改时间）
 */
@Component //交给spring
@Aspect
@Slf4j
public class DefaultParamsAspect {
    @SneakyThrows
    @Before("@annotation(com.ydl.sms.annotation.DefaultParams)")
    public void beforeEvent(JoinPoint point) {

        // TODO 自动注入基础属性（创建者、创建时间、修改者、修改时间）
        //System.out.println("走到切面方法中！");

        //1通过方法参数实体类 有没有id来判断 是新增还是修改
        //threadlocal  中获取userId
        Long userId = BaseContextHandler.getUserId();

        //拿这个方法的参数
        Object[] args = point.getArgs();
        for (Object arg : args) {
            Class<?> classes = arg.getClass(); //参数类型 string 签名类型
            Method method = getMethod(classes, "getId");
            Object id=null;
            if(method!=null){
                id=method.invoke(arg);
            }

            //1.1 新增  创建人 创建时间 修改人 修改时间
            if(id==null){
                method=getMethod(classes,"setCreateUser",String.class); //设置创建人的方法
                if(method!=null){
                    method.invoke(arg, userId.toString());
                }
                //entity.setCreateTime(LocalDateTime.MAX);
                method=getMethod(classes,"setCreateTime",LocalDateTime.class);
                if(method!=null){
                    method.invoke(arg, LocalDateTime.now());
                }
            }

            //1.2 修改  修改人 修改时间
            method=getMethod(classes,"setUpdateUser",String.class); //设置创建人的方法
            if(method!=null){
                method.invoke(arg, userId.toString());
            }
            method=getMethod(classes,"setUpdateTime",LocalDateTime.class);
            if(method!=null){
                method.invoke(arg, LocalDateTime.now());
            }
        }

    }

    /**
     * 获得方法对象
     * @param classes
     * @param name 方法名
     * @param types 参数类型
     * @return
     */
    private Method getMethod(Class classes, String name, Class... types) {
        try {
            return classes.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
