package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.BaseInfo;
import org.springframework.core.ResolvableType;

import java.io.Serializable;
import java.util.List;

/**
 * BaseDao层、
 *
 * @author 小66
 * @date 2020-06-13 20:45
 **/
public interface BaseService<T extends BaseInfo<I>, I extends Serializable> {
//    通用Mapper、
//    default GenericMapper<I> getMappr() {
//        throw new RuntimeException("必须实现getMapper()接口");
//    }

    /**
     * Spring 不推荐 在接口上添加注解、因此多写一层抽象类
     * 因为接口上的注解, 无法被实现类继承！
     * <B>重写</B> 和 <B>实现</B> 父类(父接口)的方法, Override都会导致 子类无法继承父类注解！
     *
     * @param id
     * @return
     */
    T findInfoById(I id);

    List<T> findAll();

    /**
     * 获取泛型的具体类型、
     *
     * @return T.getClass 具体类型、
     */
    default Class<T> getType() {
        ResolvableType resolvableType = ResolvableType.forClass(getClass());
        // 获取当前类的类型、
        Class<?> resolve = resolvableType.resolve();
        // 获取当前类的所有的泛型的ResolvableType
        ResolvableType[] generics = resolvableType.getGenerics();
        // 获取指定 父类或接口 的ResolvableType、
        // 如果指定的类型不是当前类型的父类或接口,则返回 ResolvableType NONE = new ResolvableType(EmptyType.INSTANCE, null, null, 0);
        ResolvableType asMethod = resolvableType.as(BaseService.class);
        ResolvableType[] types = asMethod.getGenerics();
        // 获取BaseService接口的第一个泛型的具体类型
        Class<?> clazz = types[0].resolve();
        @SuppressWarnings("unchecked")
        Class<T> tClass = (Class<T>) ResolvableType.forClass(getClass()).as(BaseService.class).getGeneric(0).resolve();
        return tClass;
    }
}
