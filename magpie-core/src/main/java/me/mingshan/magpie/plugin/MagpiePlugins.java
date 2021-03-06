/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.mingshan.magpie.plugin;

import me.mingshan.magpie.api.Export;
import me.mingshan.magpie.common.Constants;
import me.mingshan.magpie.property.MagpieFileProperties;
import me.mingshan.magpie.property.MagpieProperties;
import me.mingshan.magpie.property.MagpieProperty;
import me.mingshan.magpie.property.MagpieSystemProperties;
import me.mingshan.magpie.util.ClassUtil;
import me.mingshan.magpie.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The plugins of magpie.
 *
 * @author mingshan
 */
public class MagpiePlugins {
    private static final Logger LOGGER = LoggerFactory.getLogger(MagpiePlugins.class);
    private final AtomicReference<List<Export>> logExports = new AtomicReference<>();
    private MagpieProperties asyncLoggerProperties;

    /**
     * Inner class for lazy load.
     */
    private static class AsyncLoggerPluginsHolder {
        private static final MagpiePlugins INSTANCE = MagpiePlugins.create();
    }

    /**
     * Returns the instance of {@link MagpiePlugins}.
     *
     * @return the instance of {@link MagpiePlugins}
     */
    public static MagpiePlugins getInstance() {
        return AsyncLoggerPluginsHolder.INSTANCE;
    }

    /**
     * No Public
     */
    private MagpiePlugins() {
        asyncLoggerProperties = resolveDynamicProperties(LoadConfigType.SYSTEM);
    }

    /**
     * Creates an instance of {@link MagpiePlugins}.
     *
     * @return the instance of {@link MagpiePlugins}
     */
    private static MagpiePlugins create() {
        return new MagpiePlugins();
    }

    /**
     * Gets the implementations of {@link Export}.
     *
     * @return the implementations of {@link Export}
     */
    public List<Export> getlogExports() {
        if (logExports.get() == null) {
            List<Export> impl = getPluginImplementation(Export.class);
            logExports.compareAndSet(null, impl);
        }

        return logExports.get();
    }

    /**
     * Registers a instance of {@link Export}.
     *
     * @param impl the implementation of {@link Export}
     */
    public void registerLogExports(List<Export> impl) {
        if (!logExports.compareAndSet(null, impl)) {
            throw new IllegalStateException("Another Export was already registered.");
        }
    }

    /**
     * Gets the implementations of plugin.
     *
     * @param clazz the class of plugin
     * @param <T> the generics class
     * @return the implementations of plugin
     */
    private <T> List<T> getPluginImplementation(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        // Gets configuration via system property.
        List<T> ts = getPluginImplementationByProperty(asyncLoggerProperties, clazz);
        LOGGER.info("Find plugin " +  clazz.getSimpleName() + " by system property, implementations："
                + (ts != null ? Arrays.toString(ts.toArray()) : ""));

        if (ts.size() > 0) {
            return ts;
        } else {
            // Gets configuration via file property.
            asyncLoggerProperties = resolveDynamicProperties(LoadConfigType.FILE);
            ts = getPluginImplementationByProperty(asyncLoggerProperties, clazz);
            LOGGER.info("Find plugin " +  clazz.getSimpleName() + " by file property, implementations："
                    + (ts != null ? Arrays.toString(ts.toArray()) : ""));
            if (ts.size() > 0) {
                return ts;
            }
        }

        return findClass(clazz);
    }

    /**
     * Gets plugin implementation via property.
     *
     * @param asyncLoggerProperties the {@link MagpieProperties} implementation
     * @param clazz the interface which will be get
     * @param <T> the generics class
     * @return the implementation
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> getPluginImplementationByProperty(MagpieProperties asyncLoggerProperties,
                                                          Class<T> clazz) {
        String className = clazz.getSimpleName();
        String propertyName = Constants.PLUGIN_PROPERTY_PREFIX + className.toLowerCase() + ".implementation";
        MagpieProperty<String> property = asyncLoggerProperties.getString(propertyName, null);

        if (property != null) {
            List<T> values = new ArrayList<>();
            // Notice, considers that there are more than one implementations for a plugin.
            String implementClassStr = property.get();
            String[] implementClasses = StringUtil.split(implementClassStr, Constants.PLUGIN_PROPERTY_MULTI_DEFAULT_SEPARATOR);
            if (implementClasses != null) {
                for (String implementClass : implementClasses) {
                    try {
                        if (StringUtil.isEmpty(implementClass)) {
                            return Collections.EMPTY_LIST;
                        }
                        Class<?> cls = Class.forName(implementClass);
                        cls = cls.asSubclass(clazz);
                        Constructor constructor = cls.getConstructor();
                        T t = (T) constructor.newInstance();
                        values.add(t);
                    } catch (ClassCastException e) {
                        throw new RuntimeException(className + " implementation is not an instance of "
                                + className + ": " + implementClass);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(className + " implementation class not found: " + implementClass, e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(className + " implementation not able to be instantiated: "
                                + implementClass, e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(className + " implementation not able to be accessed: " + implementClass, e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(className + " implementation class can't get constructor: ", e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(className + " implementation class get instance failed: ", e);
                    }
                }

                return values;
            } else {
                return Collections.EMPTY_LIST;
            }
        }

        return Collections.EMPTY_LIST;
    }

    /**
     * This method needs to be extended.
     *
     * @return {@code syncLoggerProperties} implementation
     */
    private MagpieProperties resolveDynamicProperties(LoadConfigType type) {
        MagpieProperties asyncLoggerProperties;

        switch (type) {
            case SYSTEM:
                asyncLoggerProperties = MagpieSystemProperties.getInstance();
                break;
            case FILE:
                asyncLoggerProperties = MagpieFileProperties.getInstance();
                break;
            default: throw new RuntimeException("Can not find the type of loading configuration.");
        }

        return asyncLoggerProperties;
    }

    /**
     * Finds class via SPI or default implementation.
     *
     * @param clazz the interface which will be get
     * @param <T> the generics class
     * @return the implementation
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> findClass(Class<T> clazz) {
        List<T> objs = new ArrayList<>();
        // SPI
        ServiceLoader<T> serviceLoader =  ServiceLoader.load(clazz);
        for (T t : serviceLoader) {
            if (t != null) {
                LOGGER.info("Find plugin " +  clazz.getSimpleName() + " by SPI, implementation："  + t);
                objs.add(t);
            }
        }

        // If property and spi are null, chooses the default implementation.
        if (objs.isEmpty()) {
            T result;
            try {
                result = (T) ClassUtil.getClassLoader().loadClass(getDefaultImplementClassFullName(clazz.getSimpleName() ));
                LOGGER.info("Find plugin " +  clazz.getSimpleName() + " by Default, implementation："  + result);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class " + clazz + " not found ", e);
            }
            objs.add(result);
        }

        return objs;
    }

    /**
     * Gets the fully qualified name of the default implementation class.
     * @param interfaceName the interface name
     * @return the fully qualified name
     */
    private String getDefaultImplementClassFullName(String interfaceName) {
        String fullName = "Default" + interfaceName + "Impl";
        return Constants.DEFAULT_IMPL_PACKAGE + "." + fullName;
    }

}
