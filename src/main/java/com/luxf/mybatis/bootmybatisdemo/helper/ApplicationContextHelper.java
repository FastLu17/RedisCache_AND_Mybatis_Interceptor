package com.luxf.mybatis.bootmybatisdemo.helper;

import com.luxf.mybatis.bootmybatisdemo.service.AbstractDaoImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 获取Spring容器中的任意Bean、
 * 必须添加到容器中去、
 * <p>
 * 注意：通过 ApplicationContextHelper 在 其他的Bean@PostConstruct的初始化方法中、获取到的application是null、
 *
 * @author 小66
 */
@Component
public class ApplicationContextHelper implements ApplicationContextAware, BeanPostProcessor {

    /**
     * 初始化SpringCache的对象、手动需要配置@Bean交给Spring容器、
     */
    private final CacheOperationSource cacheOperationSource;

    private static ApplicationContext applicationContext;

    @Autowired
    public ApplicationContextHelper(CacheOperationSource cacheOperationSource) {
        this.cacheOperationSource = cacheOperationSource;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHelper.applicationContext = applicationContext;
    }

    /**
     * 获取spring容器中的bean, 通过bean类型获取
     *
     * @param beanClass Bean的class对象
     * @return T 返回指定类型的bean实例
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }

    /**
     * postProcessBeforeInitialization()：所有的Bean对象都会走这个方法、
     * 动态初始化 Redis Cache 的value、key等、
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        String[] namesForAnnotation = applicationContext.getBeanNamesForAnnotation(Service.class);
        if (Arrays.asList(namesForAnnotation).contains(beanName)) {
            Class<?> aClass = bean.getClass();
            boolean assignable = AbstractDaoImpl.class.isAssignableFrom(aClass);
            if (!assignable) {
                return bean;
            }
            // 取原始的class名、
            String className = aClass.getName();
            String proxyFlag = "$$Enhancer";
            if (aClass.getName().contains(proxyFlag)) {
                className = className.split("\\$\\$Enhancer")[0];
            }
            try {
                Class<?> clazz = Class.forName(className);
                // TODO: 按需实现即可, 三个方法内部可以抽取公共部分, 暂不处理
                processCacheable(clazz);
                processCachePut(clazz);
                // processCacheEvict(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    /**
     * 处理Bean对象的{@link Cacheable}注解、
     *
     * @param clazz bean
     * @throws Exception
     */
    private void processCacheable(Class<?> clazz) throws Exception {
        Class<Cacheable> cacheClass = Cacheable.class;
        List<Method> methods = getSupperClassMethods(clazz, cacheClass);
        for (Method method : methods) {
            Map<String, Object> map = getMemberValues(clazz, cacheClass, method);
            if (map != null) {
                // 赋值key时, 需要使用SPEL表达式, 与注解上赋值key的语法相同、
                // TODO: key的赋值方式应该根据接口的不同,有所区别、--> findById(Integer id);
                map.put("key", "#id");
                // 优先 push到 'attributeCache' 中去, 底层代码中如果get()得到结果, 就直接返回、不会覆盖之前的。因此需要优先push、
                cacheOperationSource.getCacheOperations(method, clazz);
            }
        }
    }

    private Map<String, Object> getMemberValues(Class<?> clazz, Class<? extends Annotation> cacheClass, Method method) throws NoSuchFieldException, IllegalAccessException {
        // 获取泛型AbstractDaoImpl<T,I>中 T 的具体类型、
        Class<?> genericType = ResolvableType.forClass(clazz).as(AbstractDaoImpl.class).getGeneric(0).resolve();
        // T findInfoById(I id); 泛型的返回值--> BaseInfo.class,而不是具体的UserInfo.class
        Class<?> returnType = method.getReturnType();
        assert genericType != null;
        if (genericType.isAnnotationPresent(Table.class) && returnType.isAssignableFrom(genericType)) {
            Table table = genericType.getAnnotation(Table.class);
            String tableName = table.name();

            // 动态修改注解参数、
            Annotation cacheable = method.getAnnotation(cacheClass);
            InvocationHandler handler = Proxy.getInvocationHandler(cacheable);
            Field memberValues = handler.getClass().getDeclaredField("memberValues");
            memberValues.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) memberValues.get(handler);
            String[] values = {tableName};
            map.put("value", values);
            map.put("cacheNames", values);
            return map;
        }
        return null;
    }

    /**
     * 处理Bean对象的{@link CachePut}注解、
     *
     * @param clazz bean
     * @throws Exception
     */
    private void processCachePut(Class<?> clazz) throws Exception {
        // 这里需要改为获取父类的方法, 自己本身的都写了@Cache注解、也就没必要在动态赋值value和key、
        Class<CachePut> cacheClass = CachePut.class;
        List<Method> methods = getSupperClassMethods(clazz, cacheClass);
        for (Method method : methods) {
            Map<String, Object> map = getMemberValues(clazz, cacheClass, method);
            if (map != null) {
                // 赋值key时, 需要使用SPEL表达式, 与注解上赋值key的语法相同、
                // TODO: key的赋值方式应该根据接口的不同,有所区别、-->T insertInfo(T info);
                map.put("key", "#result.id");
                // 优先 push到 'attributeCache' 中去, 底层代码中如果get()得到结果, 就直接返回、不会覆盖之前的。因此需要优先push、
                cacheOperationSource.getCacheOperations(method, clazz);
            }
        }
    }

    /**
     * 只获取第一级父类的方法, 根据子类是否实现父类方法, 判断返回getMethods()还是getDeclaredMethods()、
     *
     * @param clazz                 子类
     * @param methodAnnotationClass 存在的父类的注解
     */
    @SafeVarargs
    private final List<Method> getSupperClassMethods(Class clazz, Class<? extends Annotation>... methodAnnotationClass) {
        // 获取当前类的DeclaredMethods、
        List<Method> targetDeclaredMethods = Stream.of(clazz.getDeclaredMethods()).collect(Collectors.toList());
        // 获取父类的DeclaredMethods、
        Method[] declaredMethods = clazz.getSuperclass().getDeclaredMethods();

        List<Method> declaredList = Stream.of(declaredMethods)
                .filter(method -> Stream.of(methodAnnotationClass).anyMatch(method::isAnnotationPresent))
                .collect(Collectors.toList());

        Map<String, Class<?>[]> methodMap = declaredList.stream().collect(Collectors.toMap(Method::getName, Method::getParameterTypes));
        // 判断子类是否有重写具有指定的注解父类方法、
        long count = targetDeclaredMethods.stream().filter(m -> {
            String name = m.getName();
            boolean containsKey = methodMap.containsKey(name);
            if (!containsKey) {
                return false;
            }
            List<Class<?>> mapClassList = Arrays.asList(methodMap.get(name));
            List<Class<?>> parametersClassList = Arrays.asList(m.getParameterTypes());
            // 参数数量相同 并且 参数类型相同
            return mapClassList.size() == parametersClassList.size() && mapClassList.containsAll(parametersClassList);
        }).count();

        // 如果有重写父类的方法、则需要返回父类的getDeclaredMethods(),
        // TODO: 注意、如果返回父类的getMethods(), 则后续动态配置的注解参数无效、
        if (count > 0) {
            return declaredList;
        }
        // 如果没有重写父类的方法, 则需要返回父类的getMethods(), 如果返回父类的getDeclaredMethods(), 则后续动态配置的注解参数无效、
        return Stream.of(clazz.getSuperclass().getMethods())
                .filter(method -> Stream.of(methodAnnotationClass).anyMatch(method::isAnnotationPresent))
                .collect(Collectors.toList());
    }
}