package com.luxf.mybatis.bootmybatisdemo.helper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
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
     * 动态初始化 RedisCache 的value、key等、
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        String[] namesForAnnotation = applicationContext.getBeanNamesForAnnotation(Service.class);
        if (Arrays.asList(namesForAnnotation).contains(beanName)) {
            Class<?> aClass = bean.getClass();
            // 取原始的class名、
            String className = aClass.getName().split("\\$\\$Enhancer")[0];
            List<Method> methods;
            try {
                Class<?> clazz = Class.forName(className);
                // 这里需要改为获取接口的方法, 自己本身的都写了@Cache注解、也就没必要在动态赋值value和key、
                Class<Cacheable> cacheClass = Cacheable.class;
                // 主要3个注解：@Cacheable @CachePut @CacheEvict
                methods = getSupperMethods(clazz, cacheClass);
                Method[] declaredMethods = clazz.getDeclaredMethods();
                // 获取子类的方法、
                List<Method> methodList = methods.parallelStream()
                        .map(getFinalMethodFunction(declaredMethods)).collect(Collectors.toList());
                for (Method method : methodList) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType.isAnnotationPresent(Table.class)) {
                        Table table = returnType.getAnnotation(Table.class);
                        String tableName = table.name();
                        Cacheable cacheable = method.getAnnotation(cacheClass);
                        // 动态修改注解参数、
                        InvocationHandler handler = Proxy.getInvocationHandler(cacheable);
                        Field memberValues;
                        try {
                            memberValues = handler.getClass().getDeclaredField("memberValues");
                            memberValues.setAccessible(true);
                            Map<String, Object> map = (Map<String, Object>) memberValues.get(handler);
                            String[] values = {tableName};
                            map.put("value", values);
                            // AOP拦到的方法参数的值、赋值key时,需要使用SPEL表达式
                            // TODO: key的赋值方式应该根据接口的不同,有所区别
                            map.put("key", "#id");
                            // 优先 push到 'attributeCache' 中去, 底层代码中如果get()得到结果, 就直接返回、不会覆盖之前的。因此需要优先push、
                            cacheOperationSource.getCacheOperations(method, clazz);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    private Function<Method, Method> getFinalMethodFunction(Method[] declaredMethods) {
        return method -> {
            String name = method.getName();
            List<Method> arrayList = new ArrayList<>(Arrays.asList(declaredMethods));
            arrayList = arrayList.stream().filter(m -> name.equals(m.getName())).collect(Collectors.toList());
            arrayList.removeIf(m -> {
                String[] ml = m.toString().split(" ");
                String[] mdl = method.toString().split(" ");
                return ml[ml.length - 2].equals(mdl[mdl.length - 2]);
            });
            return arrayList.get(0);
        };
    }

    @SafeVarargs
    private final List<Method> getSupperMethods(Class clazz, Class<? extends Annotation>... annotationClass) {
        List<Class<?>> allInterfaces = getAllInterfaces(clazz);
        List<Method> methodList = new ArrayList<>();
        allInterfaces.parallelStream().forEach(aClass -> {
            List<Method> methods = Stream.of(aClass.getMethods())
                    .filter(method -> Stream.of(annotationClass).anyMatch(method::isAnnotationPresent))
                    .collect(Collectors.toList());
            methodList.addAll(methods);
        });
        return methodList;
    }

    private List<Class<?>> getAllInterfaces(Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) {
            return Collections.emptyList();
        }

        List<Class<?>> interfaceList = new ArrayList<>();
        Class<?> searchClazz = clazz;
        // 查所有的父类和父接口
        while (searchClazz != null) {
            for (Class<?> iClass : searchClazz.getInterfaces()) {
                interfaceList.add(iClass);
                interfaceList.addAll(getAllInterfaces(iClass));
            }
            searchClazz = searchClazz.getSuperclass();
        }
        return interfaceList;
    }
}