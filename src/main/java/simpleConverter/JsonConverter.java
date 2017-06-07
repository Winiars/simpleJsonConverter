package simpleConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by JIT on 05.06.2017.
 */
public class JsonConverter {


    private static final Set<Class<?>> WRAPPERS = getWrappers();


    public String toJson(Object objectToConvert) throws IllegalAccessException {
        if (objectToConvert == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        Class<?> cls = objectToConvert.getClass();

        if(Collection.class.isAssignableFrom(cls)){
            Collection<?> collection = (Collection) objectToConvert;
            sb.append("[");
            for (Object obj : collection) {
                appendObject(obj, sb);
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
            return sb.toString();
        }

        if(cls.isArray()){
            sb.append("[");
            int length = Array.getLength(objectToConvert);
            for (int i = 0; i < length; i++) {
                Object arrayObject = Array.get(objectToConvert, i);
                appendObject(arrayObject, sb);
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
            return sb.toString();
        }


        Field[] fields = cls.getDeclaredFields();

        addBeginningBrackets(sb, objectToConvert);

        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldValue = field.get(objectToConvert);
            Class<?> fieldType = field.getType();

            if (fieldValue == null) {
                continue;
            }
            sb.append("\"");
            sb.append(field.getName());
            sb.append("\":");

            addValues(sb, fieldValue, fieldType);

        }
        sb.deleteCharAt(sb.lastIndexOf(","));

        if (Collection.class.isAssignableFrom(cls)) {
            sb.append("]");
        } else {
            sb.append("}");
        }
        return sb.toString();
    }

    private void addBeginningBrackets(StringBuilder sb, Object object) {
        Class<?> cls = object.getClass();
        if (Collection.class.isAssignableFrom(cls)) {
            sb.append("[");
        } else {
            sb.append("{");
        }
    }

    private void addValues(StringBuilder sb, Object fieldValue, Class<?> fieldType) throws IllegalAccessException {

        if (String.class.isAssignableFrom(fieldType) || Enum.class.isAssignableFrom(fieldType)) {
            sb.append("\"");
            sb.append(fieldValue.toString());
            sb.append("\"");
        } else if (!fieldType.isPrimitive() && !Collection.class.isAssignableFrom(fieldType) && !fieldType.isArray()&&!Map.class.isAssignableFrom(fieldType)) {
            sb.append(toJson(fieldValue));
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            Collection<?> collection = (Collection) fieldValue;
            sb.append("[");
            for (Object obj : collection) {
                appendObject(obj, sb);
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
        } else if (fieldType.isArray()) {
            sb.append("[");
            int length = Array.getLength(fieldValue);
            for (int i = 0; i < length; i++) {
                Object arrayObject = Array.get(fieldValue, i);
                appendObject(arrayObject, sb);
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
        } else if (Map.class.isAssignableFrom(fieldType)) {
            Map<?, ?> map = (Map) fieldValue;
            sb.append("{");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sb.append("\"");
                sb.append(entry.getKey().toString());
                sb.append("\":");
                sb.append("\"");
                sb.append(entry.getValue().toString());
                sb.append("\":,");
                sb.deleteCharAt(sb.lastIndexOf(":"));
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("}");
        } else {
            sb.append(fieldValue.toString());
        }
        sb.append(",");
    }

    public static boolean isWrapperType(Class<?> cls) {
        return WRAPPERS.contains(cls);
    }

    private static Set<Class<?>> getWrappers() {
        Set<Class<?>> wrappers = new HashSet<Class<?>>();
        wrappers.add(Integer.class);
        wrappers.add(Double.class);
        wrappers.add(Boolean.class);
        wrappers.add(Character.class);
        wrappers.add(Byte.class);
        wrappers.add(Short.class);
        wrappers.add(Long.class);
        wrappers.add(Float.class);
        wrappers.add(Void.class);
        return wrappers;
    }

    private void appendObject(Object obj, StringBuilder sb) throws IllegalAccessException {
        if (!isWrapperType(obj.getClass()) && !obj.getClass().isAssignableFrom(String.class)) {
            sb.append(toJson(obj));
            sb.append(",");
        } else if (obj.getClass().isAssignableFrom(String.class)) {
            sb.append("\"");
            sb.append(obj.toString());
            sb.append("\",");
        } else {
            sb.append(obj.toString());
            sb.append(",");
        }
    }
}