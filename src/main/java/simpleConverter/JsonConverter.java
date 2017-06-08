package simpleConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class JsonConverter {


    private static final Set<Class<?>> WRAPPERS = getWrappers();


    public String toJson(Object objectToConvert) throws IllegalAccessException {
        if (objectToConvert == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        Class<?> cls = objectToConvert.getClass();

        if (isCollectionOrMapAOrArrayOrStringOrEnumOrPrimitiveTypeOrWrapperType(cls)) {
            appendObjectWithoutFields(objectToConvert, sb);
        } else {
            appendObjectWithFields(objectToConvert, sb);
        }
        return sb.toString();
    }


    private void appendObjectWithoutFields(Object objectToConvert, StringBuilder sb) throws IllegalAccessException {
        Class<?> cls = objectToConvert.getClass();
        addValues(sb, objectToConvert, cls);
    }

    private void appendObjectWithFields(Object objectToConvert, StringBuilder sb) throws IllegalAccessException {

        Class<?> cls = objectToConvert.getClass();
        Field[] fields = cls.getDeclaredFields();

        appendOpeningBrackets(objectToConvert, sb);

        for (Field field : fields) {
            appendFieldAndFieldValue(field, objectToConvert, sb);
        }
        sb.deleteCharAt(sb.lastIndexOf(","));

        appendClosingBrackets(objectToConvert, sb);
    }

    private void addValues(StringBuilder sb, Object fieldValue, Class<?> fieldType) throws IllegalAccessException {

        if (isStringOrEnum(fieldType)) {
            surroundWithQuotation(fieldValue, sb);
        } else if (isNotCollectionAndMapAndArrayAndPrimitiveTypeAndWrapperType(fieldType)) {
            sb.append(toJson(fieldValue));
        } else if (isCollection(fieldType)) {
            appendElementsOfCollection(fieldValue, sb);
        } else if (fieldType.isArray()) {
            appendElementsOfArray(fieldValue, sb);
        } else if (isMap(fieldType)) {
            appendElementsOfMap(fieldValue, sb);
        } else {
            sb.append(fieldValue.toString());
        }

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

    private void appendObject(Object obj, StringBuilder sb) throws IllegalAccessException {
        Class<?> cls = obj.getClass();
        if (!isWrapperType(obj.getClass()) && !isString(cls)) {
            sb.append(toJson(obj));
            sb.append(",");
        } else if (isString(cls)) {
            surroundWithQuotation(obj, sb);
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

    private boolean isNotCollectionAndMapAndArrayAndPrimitiveTypeAndWrapperType(Class<?> cls) {
        return !cls.isPrimitive() && !isCollection(cls) && !cls.isArray() && !isMap(cls) && !isWrapperType(cls);
    }

    private boolean isCollectionOrMapAOrArrayOrStringOrEnumOrPrimitiveTypeOrWrapperType(Class<?> cls) {
        return cls.isPrimitive() || isCollection(cls) || cls.isArray() || isMap(cls) || isStringOrEnum(cls) || isWrapperType(cls);
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

    private void surroundWithQuotation(Object obj, StringBuilder sb) {
        sb.append("\"");
        sb.append(obj.toString());
        sb.append("\"");
    }

    private void appendElementsOfCollection(Object fieldValue, StringBuilder sb) throws IllegalAccessException {
        Collection<?> collection = (Collection) fieldValue;
        sb.append("[");
        for (Object obj : collection) {
            appendObject(obj, sb);
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
    }

    private void appendElementsOfArray(Object fieldValue, StringBuilder sb) throws IllegalAccessException {
        sb.append("[");
        int length = Array.getLength(fieldValue);
        for (int i = 0; i < length; i++) {
            Object arrayObject = Array.get(fieldValue, i);
            appendObject(arrayObject, sb);
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
    }

    private void appendElementsOfMap(Object fieldValue, StringBuilder sb) {
        Map<?, ?> map = (Map) fieldValue;
        sb.append("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            surroundWithQuotation(entry.getKey(), sb);
            sb.append(":");
            surroundWithQuotation(entry.getValue(), sb);
            sb.append(":,");
            sb.deleteCharAt(sb.lastIndexOf(":"));
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");
    }

    private void appendFieldAndFieldValue(Field field, Object objectToConvert, StringBuilder sb) throws IllegalAccessException {
        field.setAccessible(true);
        Object fieldValue = field.get(objectToConvert);
        Class<?> fieldType = field.getType();

        if (fieldValue == null) {
            return;
        }
        surroundWithQuotation(field.getName(), sb);
        sb.append(":");

        addValues(sb, fieldValue, fieldType);
        sb.append(",");

    }

    private void appendClosingBrackets(Object object, StringBuilder sb) {
        Class<?> cls = object.getClass();
        if (isCollection(cls)) {
            sb.append("]");
        } else {
            sb.append("}");
        }

    }

    private void appendOpeningBrackets(Object object, StringBuilder sb) {
        Class<?> cls = object.getClass();
        if (isCollection(cls)) {
            sb.append("[");
        } else {
            sb.append("{");
        }
    }

}