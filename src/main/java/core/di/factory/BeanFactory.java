package core.di.factory;

import com.google.common.collect.Maps;
import core.exception.NoSuchBeanConstructorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactory.class);

    private Set<Class<?>> preInstanticateBeans;

    private Map<Class<?>, Object> beans = Maps.newHashMap();

    public BeanFactory(Set<Class<?>> preInstanticateBeans) {
        this.preInstanticateBeans = preInstanticateBeans;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    public void initialize() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> clazz : preInstanticateBeans) {
            Object instance = createInstance(clazz);
            beans.put(clazz, instance);
        }
    }

    private Object createInstance(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> concreteClass = BeanFactoryUtils.findConcreteClass(clazz, preInstanticateBeans);
        Constructor<?> constructor = findConstructor(concreteClass);
        List<Object> parameters = new ArrayList<>();

        for (Class<?> typeClass : constructor.getParameterTypes()) {
            parameters.add(getParameterByClass(typeClass));
        }

        return constructor.newInstance(parameters.toArray());
    }

    private Constructor<?> findConstructor(Class<?> concreteClass) {
        Constructor<?> constructor = BeanFactoryUtils.getInjectedConstructor(concreteClass);

        if (Objects.nonNull(constructor)) {
            return constructor;
        }

        Constructor<?>[] constructors = concreteClass.getConstructors();

        if (constructors.length > 0) {
            return constructors[0];
        }

        throw new NoSuchBeanConstructorException(concreteClass);
    }

    private Object getParameterByClass(Class<?> typeClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object bean = getBean(typeClass);

        if (Objects.nonNull(bean)) {
            return bean;
        }
        return createInstance(typeClass);
    }
}
