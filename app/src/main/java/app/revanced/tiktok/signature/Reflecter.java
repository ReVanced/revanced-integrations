package app.revanced.tiktok.signature;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A wrapper for an {@link Object} or {@link Class} upon which reflective calls
 * can be made.
 * <p>
 * An example of using <code>Reflect</code> is <code><pre>
 * // Static import all reflection methods to decrease verbosity
 * import static org.joor.Reflect.*;
 *
 * // Wrap an Object / Class / class name with the on() method:
 * on("java.lang.String")
 * // Invoke constructors using the preload() method:
 * .preload("Hello World")
 * // Invoke methods using the call() method:
 * .call("toString")
 * // Retrieve the wrapped object
 *
 * @author Lukas Eder
 * @author Irek Matysiewicz
 */
public class Reflecter {

    /**
     * A unchecked wrapper for any of Java's checked reflection exceptions:
     * <p>
     * These exceptions are
     * <ul>
     * <li> {@link ClassNotFoundException}</li>
     * <li> {@link IllegalAccessException}</li>
     * <li> {@link IllegalArgumentException}</li>
     * <li> {@link InstantiationException}</li>
     * <li> {@link InvocationTargetException}</li>
     * <li> {@link NoSuchMethodException}</li>
     * <li> {@link NoSuchFieldException}</li>
     * <li> {@link SecurityException}</li>
     * </ul>
     *
     * @author Lukas Eder
     */
    public static class ReflectException extends RuntimeException {

        public ReflectException(String message) {
            super(message);
        }

        public ReflectException(String message, Throwable cause) {
            super(message, cause);
        }

        public ReflectException() {
            super();
        }

        public ReflectException(Throwable cause) {
            super(cause);
        }
    }

    // ---------------------------------------------------------------------
    // Static API used as entrance points to the fluent API
    // ---------------------------------------------------------------------

    /**
     * Wrap a class name.
     * <p>
     * This is the same as calling <code>on(Class.forName(name))</code>
     *
     * @param name A fully qualified class name
     * @return A wrapped class object, to be used for further reflection.
     * @throws ReflectException If any reflection exception occurred.
     * @see #on(Class)
     */
    public static Reflecter on(String name) throws ReflectException {
        return on(forName(name));
    }

    /**
     * Wrap a class name, loading it via a given class loader.
     * <p>
     * This is the same as calling
     * <code>on(Class.forName(name, classLoader))</code>
     *
     * @param name A fully qualified class name.
     * @param classLoader The class loader in whose context the class should be
     *            loaded.
     * @return A wrapped class object, to be used for further reflection.
     * @throws ReflectException If any reflection exception occurred.
     * @see #on(Class)
     */
    public static Reflecter on(String name, ClassLoader classLoader) throws ReflectException {
        return on(forName(name, classLoader));
    }

    /**
     * Wrap a class.
     * <p>
     * Use this when you want to access static fields and methods on a
     * {@link Class} object, or as a basis for constructing objects of that
     * class using {@link #create(Object...)}
     *
     * @param clazz The class to be wrapped
     * @return A wrapped class object, to be used for further reflection.
     */
    public static Reflecter on(Class<?> clazz) {
        return new Reflecter(clazz);
    }

    /**
     * Wrap an object.
     * <p>
     * Use this when you want to access instance fields and methods on any
     * {@link Object}
     *
     * @param object The object to be wrapped
     * @return A wrapped object, to be used for further reflection.
     */
    public static Reflecter on(Object object) {
        return new Reflecter(object);
    }

    /**
     * Conveniently render an {@link AccessibleObject} accessible.
     * <p>
     * To prevent {@link SecurityException}, this is only done if the argument
     * object and its declaring class are non-public.
     *
     * @param accessible The object to render accessible
     * @return The argument object rendered accessible
     */
    public static <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) {
            return null;
        }

        if (accessible instanceof Member) {
            Member member = (Member) accessible;

            if (Modifier.isPublic(member.getModifiers()) &&
                    Modifier.isPublic(member.getDeclaringClass().getModifiers())) {

                return accessible;
            }
        }

        // [jOOQ #3392] The accessible flag is set to false by default, also for public members.
        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }

        return accessible;
    }

    // ---------------------------------------------------------------------
    // Members
    // ---------------------------------------------------------------------

    /**
     * The wrapped object
     */
    private final Object object;

    /**
     * A flag indicating whether the wrapped object is a {@link Class} (for
     * accessing static fields and methods), or any other type of {@link Object}
     * (for accessing instance fields and methods).
     */
    private final boolean isClass;

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    private Reflecter(Class<?> type) {
        this.object = type;
        this.isClass = true;
    }

    private Reflecter(Object object) {
        this.object = object;
        this.isClass = false;
    }

    // ---------------------------------------------------------------------
    // Fluent Reflection API
    // ---------------------------------------------------------------------

    /**
     * Get the wrapped object
     *
     * @param <T> A convenience generic parameter for automatic unsafe casting
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T) object;
    }

    /**
     * Set a field value.
     * <p>
     * This is roughly equivalent to {@link Field#set(Object, Object)}. If the
     * wrapped object is a {@link Class}, then this will set a value to a static
     * member field. If the wrapped object is any other {@link Object}, then
     * this will set a value to an instance member field.
     *
     * @param name The field name
     * @param value The new field value
     * @return The same wrapped object, to be used for further reflection.
     * @throws ReflectException If any reflection exception occurred.
     */
    public Reflecter set(String name, Object value) throws ReflectException {
        try {
            Field field = field0(name);
            field.set(object, unwrap(value));
            return this;
        }
        catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * Get a field value.
     * <p>
     * This is roughly equivalent to {@link Field#get(Object)}. If the wrapped
     * object is a {@link Class}, then this will get a value from a static
     * member field. If the wrapped object is any other {@link Object}, then
     * this will get a value from an instance member field.
     * <p>
     * If you want to "navigate" to a wrapped version of the field, use
     * {@link #field(String)} instead.
     *
     * @param name The field name
     * @return The field value
     * @throws ReflectException If any reflection exception occurred.
     * @see #field(String)
     */
    public <T> T get(String name) throws ReflectException {
        return field(name).<T>get();
    }

    /**
     * Get a wrapped field.
     * <p>
     * This is roughly equivalent to {@link Field#get(Object)}. If the wrapped
     * object is a {@link Class}, then this will wrap a static member field. If
     * the wrapped object is any other {@link Object}, then this wrap an
     * instance member field.
     *
     * @param name The field name
     * @return The wrapped field
     * @throws ReflectException If any reflection exception occurred.
     */
    public Reflecter field(String name) throws ReflectException {
        try {
            Field field = field0(name);
            return on(field.get(object));
        }
        catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private Field field0(String name) throws ReflectException {
        Class<?> type = type();

        // Try getting a public field
        try {
            return type.getField(name);
        }

        // Try again, getting a non-public field
        catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(type.getDeclaredField(name));
                }
                catch (NoSuchFieldException ignore) {}

                type = type.getSuperclass();
            }
            while (type != null);

            throw new ReflectException(e);
        }
    }

    /**
     * Get a Map containing field names and wrapped values for the fields'
     * values.
     * <p>
     * If the wrapped object is a {@link Class}, then this will return static
     * fields. If the wrapped object is any other {@link Object}, then this will
     * return instance fields.
     * <p>
     * These two calls are equivalent <code><pre>
     * on(object).field("myField");
     * on(object).fields().get("myField");
     * </pre></code>
     *
     * @return A map containing field names and wrapped values.
     */
    public Map<String, Reflecter> fields() {
        Map<String, Reflecter> result = new LinkedHashMap<String, Reflecter>();
        Class<?> type = type();

        do {
            for (Field field : type.getDeclaredFields()) {
                if (!isClass ^ Modifier.isStatic(field.getModifiers())) {
                    String name = field.getName();

                    if (!result.containsKey(name))
                        result.put(name, field(name));
                }
            }

            type = type.getSuperclass();
        }
        while (type != null);

        return result;
    }

    /**
     * Call a method by its name.
     * <p>
     * This is a convenience method for calling
     * <code>call(name, new Object[0])</code>
     *
     * @param name The method name
     * @return The wrapped method result or the same wrapped object if the
     *         method returns <code>void</code>, to be used for further
     *         reflection.
     * @throws ReflectException If any reflection exception occurred.
     * @see #call(String, Object...)
     */
    public Reflecter call(String name) throws ReflectException {
        return call(name, new Object[0]);
    }

    /**
     * Call a method by its name.
     * <p>
     * This is roughly equivalent to {@link Method#invoke(Object, Object...)}.
     * If the wrapped object is a {@link Class}, then this will invoke a static
     * method. If the wrapped object is any other {@link Object}, then this will
     * invoke an instance method.
     * <p>
     * Just like {@link Method#invoke(Object, Object...)}, this will try to wrap
     * primitive types or unwrap primitive type wrappers if applicable. If
     * several methods are applicable, by that rule, the first one encountered
     * is called. i.e. when calling <code><pre>
     * on(...).call("method", 1, 1);
     * </pre></code> The first of the following methods will be called:
     * <code><pre>
     * public void method(int param1, Integer param2);
     * public void method(Integer param1, int param2);
     * public void method(Number param1, Number param2);
     * public void method(Number param1, Object param2);
     * public void method(int param1, Object param2);
     * </pre></code>
     * <p>
     * The best matching method is searched for with the following strategy:
     * <ol>
     * <li>public method with exact signature match in class hierarchy</li>
     * <li>non-public method with exact signature match on declaring class</li>
     * <li>public method with similar signature in class hierarchy</li>
     * <li>non-public method with similar signature on declaring class</li>
     * </ol>
     *
     * @param name The method name
     * @param args The method arguments
     * @return The wrapped method result or the same wrapped object if the
     *         method returns <code>void</code>, to be used for further
     *         reflection.
     * @throws ReflectException If any reflection exception occurred.
     */
    public Reflecter call(String name, Object... args) throws ReflectException {
        Class<?>[] types = types(args);

        // Try invoking the "canonical" method, i.e. the one with exact
        // matching argument types
        try {
            Method method = exactMethod(name, types);
            return on(method, object, args);
        }

        // If there is no exact match, try to find a method that has a "similar"
        // signature if primitive argument types are converted to their wrappers
        catch (NoSuchMethodException e) {
            try {
                Method method = similarMethod(name, types);
                return on(method, object, args);
            } catch (NoSuchMethodException e1) {
                throw new ReflectException(e1);
            }
        }
    }

    /**
     * Searches a method with the exact same signature as desired.
     * <p>
     * If a public method is found in the class hierarchy, this method is returned.
     * Otherwise a private method with the exact same signature is returned.
     * If no exact match could be found, we let the {@code NoSuchMethodException} pass through.
     */
    private Method exactMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type();

        // first priority: find a public method with exact signature match in class hierarchy
        try {
            return type.getMethod(name, types);
        }

        // second priority: find a private method with exact signature match on declaring class
        catch (NoSuchMethodException e) {
            do {
                try {
                    return type.getDeclaredMethod(name, types);
                }
                catch (NoSuchMethodException ignore) {}

                type = type.getSuperclass();
            }
            while (type != null);

            throw new NoSuchMethodException();
        }
    }

    /**
     * Searches a method with a similar signature as desired using
     * {@link #isSimilarSignature(Method, String, Class[])}.
     * <p>
     * First public methods are searched in the class hierarchy, then private
     * methods on the declaring class. If a method could be found, it is
     * returned, otherwise a {@code NoSuchMethodException} is thrown.
     */
    private Method similarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = type();

        // first priority: find a public method with a "similar" signature in class hierarchy
        // similar interpreted in when primitive argument types are converted to their wrappers
        for (Method method : type.getMethods()) {
            if (isSimilarSignature(method, name, types)) {
                return method;
            }
        }

        // second priority: find a non-public method with a "similar" signature on declaring class
        do {
            for (Method method : type.getDeclaredMethods()) {
                if (isSimilarSignature(method, name, types)) {
                    return method;
                }
            }

            type = type.getSuperclass();
        }
        while (type != null);

        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type() + ".");
    }

    /**
     * Determines if a method has a "similar" signature, especially if wrapping
     * primitive argument types would result in an exactly matching signature.
     */
    private boolean isSimilarSignature(Method possiblyMatchingMethod, String desiredMethodName, Class<?>[] desiredParamTypes) {
        return possiblyMatchingMethod.getName().equals(desiredMethodName) && match(possiblyMatchingMethod.getParameterTypes(), desiredParamTypes);
    }

    /**
     * Call a constructor.
     * <p>
     * This is a convenience method for calling
     * <code>preload(new Object[0])</code>
     *
     * @return The wrapped new object, to be used for further reflection.
     * @throws ReflectException If any reflection exception occurred.
     * @see #create(Object...)
     */
    public Reflecter create() throws ReflectException {
        return create(new Object[0]);
    }

    /**
     * Call a constructor.
     * <p>
     * This is roughly equivalent to {@link Constructor#newInstance(Object...)}.
     * If the wrapped object is a {@link Class}, then this will preload a new
     * object of that class. If the wrapped object is any other {@link Object},
     * then this will preload a new object of the same type.
     * <p>
     * Just like {@link Constructor#newInstance(Object...)}, this will try to
     * wrap primitive types or unwrap primitive type wrappers if applicable. If
     * several constructors are applicable, by that rule, the first one
     * encountered is called. i.e. when calling <code><pre>
     * on(C.class).preload(1, 1);
     * </pre></code> The first of the following constructors will be applied:
     * <code><pre>
     * public C(int param1, Integer param2);
     * public C(Integer param1, int param2);
     * public C(Number param1, Number param2);
     * public C(Number param1, Object param2);
     * public C(int param1, Object param2);
     * </pre></code>
     *
     * @param args The constructor arguments
     * @return The wrapped new object, to be used for further reflection.
     * @throws ReflectException If any reflection exception occurred.
     */
    public Reflecter create(Object... args) throws ReflectException {
        Class<?>[] types = types(args);

        // Try invoking the "canonical" constructor, i.e. the one with exact
        // matching argument types
        try {
            Constructor<?> constructor = type().getDeclaredConstructor(types);
            return on(constructor, args);
        }

        // If there is no exact match, try to find one that has a "similar"
        // signature if primitive argument types are converted to their wrappers
        catch (NoSuchMethodException e) {
            for (Constructor<?> constructor : type().getDeclaredConstructors()) {
                if (match(constructor.getParameterTypes(), types)) {
                    return on(constructor, args);
                }
            }

            throw new ReflectException(e);
        }
    }

    /**
     * Create a proxy for the wrapped object allowing to typesafely invoke
     * methods on it using a custom interface
     *
     * @param proxyType The interface type that is implemented by the proxy
     * @return A proxy for the wrapped object
     */
    @SuppressWarnings("unchecked")
    public <P> P as(Class<P> proxyType) {
        final boolean isMap = (object instanceof Map);
        final InvocationHandler handler = new InvocationHandler() {
            @SuppressWarnings("null")
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();

                // Actual method name matches always come first
                try {
                    return on(object).call(name, args).get();
                }

                // [#14] Emulate POJO behaviour on wrapped map objects
                catch (ReflectException e) {
                    if (isMap) {
                        Map<String, Object> map = (Map<String, Object>) object;
                        int length = (args == null ? 0 : args.length);

                        if (length == 0 && name.startsWith("get")) {
                            return map.get(property(name.substring(3)));
                        }
                        else if (length == 0 && name.startsWith("is")) {
                            return map.get(property(name.substring(2)));
                        }
                        else if (length == 1 && name.startsWith("set")) {
                            map.put(property(name.substring(3)), args[0]);
                            return null;
                        }
                    }

                    throw e;
                }
            }
        };

        return (P) Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[] { proxyType }, handler);
    }

    /**
     * Get the POJO property name of an getter/setter
     */
    private static String property(String string) {
        int length = string.length();

        if (length == 0) {
            return "";
        }
        else if (length == 1) {
            return string.toLowerCase();
        }
        else {
            return string.substring(0, 1).toLowerCase() + string.substring(1);
        }
    }

    // ---------------------------------------------------------------------
    // Object API
    // ---------------------------------------------------------------------

    /**
     * Check whether two arrays of types match, converting primitive types to
     * their corresponding wrappers.
     */
    private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < actualTypes.length; i++) {
                if (actualTypes[i] == NULL.class)
                    continue;

                if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i])))
                    continue;

                return false;
            }

            return true;
        }
        else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return object.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Reflecter) {
            return object.equals(((Reflecter) obj).get());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return object.toString();
    }

    // ---------------------------------------------------------------------
    // Utility methods
    // ---------------------------------------------------------------------

    /**
     * Wrap an object created from a constructor
     */
    private static Reflecter on(Constructor<?> constructor, Object... args) throws ReflectException {
        try {
            return on(accessible(constructor).newInstance(args));
        }
        catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * Wrap an object returned from a method
     */
    private static Reflecter on(Method method, Object object, Object... args) throws ReflectException {
        try {
            accessible(method);

            if (method.getReturnType() == void.class) {
                method.invoke(object, args);
                return on(object);
            }
            else {
                return on(method.invoke(object, args));
            }
        }
        catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * Unwrap an object
     */
    private static Object unwrap(Object object) {
        if (object instanceof Reflecter) {
            return ((Reflecter) object).get();
        }
        return object;
    }

    /**
     * Get an array of types for an array of objects
     *
     * @see Object#getClass()
     */
    private static Class<?>[] types(Object... values) {
        if (values == null) {
            return new Class[0];
        }

        Class<?>[] result = new Class[values.length];

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            result[i] = value == null ? NULL.class : value.getClass();
        }

        return result;
    }

    /**
     * Load a class
     *
     * @see Class#forName(String)
     */
    private static Class<?> forName(String name) throws ReflectException {
        try {
            return Class.forName(name);
        }
        catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?> forName(String name, ClassLoader classLoader) throws ReflectException {
        try {
            return Class.forName(name, true, classLoader);
        }
        catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * Get the type of the wrapped object.
     *
     * @see Object#getClass()
     */
    public Class<?> type() {
        if (isClass) {
            return (Class<?>) object;
        }
        else {
            return object.getClass();
        }
    }

    /**
     * Get a wrapper type for a primitive type, or the argument type itself, if
     * it is not a primitive type.
     */
    public static Class<?> wrapper(Class<?> type) {
        if (type == null) {
            return null;
        }
        else if (type.isPrimitive()) {
            if (boolean.class == type) {
                return Boolean.class;
            }
            else if (int.class == type) {
                return Integer.class;
            }
            else if (long.class == type) {
                return Long.class;
            }
            else if (short.class == type) {
                return Short.class;
            }
            else if (byte.class == type) {
                return Byte.class;
            }
            else if (double.class == type) {
                return Double.class;
            }
            else if (float.class == type) {
                return Float.class;
            }
            else if (char.class == type) {
                return Character.class;
            }
            else if (void.class == type) {
                return Void.class;
            }
        }

        return type;
    }

    private static class NULL {}
}