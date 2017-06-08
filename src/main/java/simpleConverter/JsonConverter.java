package simpleConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class JsonConverter {

    private static final Set<Class<?>> WRAPPERS = getWrappers();
    private StringBuilder sb;

    public String toJson(Object objectToConvert)  {
        if (objectToConvert == null) {
            return null;
        }
        this.sb = new StringBuilder();
        makeJsonFromObject(objectToConvert);
        return sb.toString();
    }

    private void makeJsonFromObject(Object objectToConvert)  {

        if (objectToConvert == null) {
           return;
        }

        Class<?> cls = objectToConvert.getClass();

        if (isStringOrEnum(cls)) {
            surroundWithQuotation(objectToConvert);
        } else if (isPrimitiveOrWrapperType(cls)) {
            sb.append(objectToConvert.toString());
        } else if (isCollection(cls)) {
            appendElementsOfCollection(objectToConvert);
        } else if (cls.isArray()) {
            appendElementsOfArray(objectToConvert);
        } else if (isMap(cls)) {
            appendElementsOfMap(objectToConvert);
        } else {
            appendObjectWithFields(objectToConvert);
        }
    }

    private void appendObjectWithFields(Object objectToConvert)  {

        Class<?> cls = objectToConvert.getClass();
        Field[] fields = cls.getDeclaredFields();

        sb.append("{");

        for (Field field : fields) {
           appendFieldAndFieldValue(field, objectToConvert);

        }
        sb.deleteCharAt(sb.lastIndexOf(","));

        sb.append("}");
    }


    private static boolean isWrapperType(Class<?> cls) {
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

    private void appendObject(Object obj){
        if (obj == null) {
            sb.append("null");
            sb.append(",");
            return;
        }
        Class<?> cls = obj.getClass();
        if (!isWrapperType(obj.getClass()) && !isString(cls)) {
            makeJsonFromObject(obj);
            sb.append(",");
        } else if (isString(cls)) {
            surroundWithQuotation(obj);
            sb.append(",");
        } else {
            sb.append(obj.toString());
            sb.append(",");
        }
    }

    private boolean isCollection(Class<?> cls) {
        return Collection.class.isAssignableFrom(cls);
    }

    private boolean isMap(Class<?> cls) {
        return Map.class.isAssignableFrom(cls);
    }

    private boolean isPrimitiveOrWrapperType(Class<?> cls) {
        return cls.isPrimitive() || isWrapperType(cls);
    }

    private boolean isString(Class<?> cls) {
        return String.class.isAssignableFrom(cls);
    }

    private boolean isEnum(Class<?> cls) {
        return Enum.class.isAssignableFrom(cls);
    }

    private boolean isStringOrEnum(Class<?> cls) {
        return isString(cls) || isEnum(cls);
    }

    private void surroundWithQuotation(Object obj) {
        sb.append("\"");
        sb.append(obj.toString());
        sb.append("\"");
    }

    private void appendElementsOfCollection(Object fieldValue) {
        Collection<?> collection = (Collection) fieldValue;
        sb.append("[");
        for (Object obj : collection) {
            appendObject(obj);
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
    }

    private void appendElementsOfArray(Object fieldValue) {
        sb.append("[");
        int length = Array.getLength(fieldValue);
        for (int i = 0; i < length; i++) {
            Object arrayObject = Array.get(fieldValue, i);
            appendObject(arrayObject);
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
    }

    private void appendElementsOfMap(Object fieldValue) {
        Map<?, ?> map = (Map) fieldValue;
        sb.append("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if(entry.getValue()==null) continue;
            surroundWithQuotation(entry.getKey());
            sb.append(":");
            appendObject(entry.getValue());
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");
    }

    private void appendFieldAndFieldValue(Field field, Object objectToConvert) {
        field.setAccessible(true);
        Object fieldValue = null;
        try {
            fieldValue = field.get(objectToConvert);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (fieldValue == null) {
            return;
        }
        surroundWithQuotation(field.getName());
        sb.append(":");

        makeJsonFromObject(fieldValue);
        sb.append(",");
    }

}