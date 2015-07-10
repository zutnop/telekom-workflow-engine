package ee.telekom.workflow.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;

/**
 * Utility that serialises and deserialises any deep object tree without cyclic references.
 * <p>
 * Common JSON libraries such as jackson or gson all support to serialise such deep object trees.
 * However, none of the known libraries supports to to deserialise json strings that describe
 * Object[] or Map<String,Object> fields or classes that contain fields of collections.
 * <p>
 * The general problem is that the class information of a given object instance is lost during
 * serialisation. E.g. it is not possible to deserialise the object array
 * <pre>new SportMatch[] {new FootballMatch("Arsenal","Chelsea"), new TennisMatch("Nadal","Djokovic")}</pre>
 * based on the json representation
 * <pre>[{oponent1:"Arsenal", oponent2:"Chelsea"},{oponent1:"Nadal", oponent2:"Djokovic"}]</pre>
 *
 * This library overcomes this issue and also serialises class information where the class type
 * cannot be deducted otherwise. When serialising it allows to choose whether to serialise class
 * type information for the root object. Not serialising root object class type information is
 * appropriate if you can predict the root object class type at deserialization time. E.g.
 * <pre>
 *   // if you can predict the type
 *   String json = JsonUtil.serialize(new SportMatch[] {new FootballMatch("Arsenal","Chelsea"), new TennisMatch("Nadal","Djokovic")},false);
 *   SportMatch[] o = JsonUtil.deserialize(json, SportMatch[].class);
 *
 *   // if you cannot predict the type
 *   String json = JsonUtil.serialize(new SportMatch[] {new FootballMatch("Arsenal","Chelsea"), new TennisMatch("Nadal","Djokovic")},true);
 *   Object o = JsonUtil.deserialize(json);
 * </pre>
 */
public class JsonUtil{

    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    private static final String ARRAYS_ASLIST_CLASSNAME = Arrays.asList( "" ).getClass().getName();
    private static final String SINGLETON_LIST_CLASSNAME = Collections.singletonList( "" ).getClass().getName();
    private static final String SINGLETON_SET_CLASSNAME = Collections.singleton( "" ).getClass().getName();
    private static final String SINGLETON_MAP_CLASSNAME = Collections.singletonMap( "", "" ).getClass().getName();

    private static final Class<?> ARRAYS_ASLIST_CLASS = Arrays.asList( "" ).getClass();
    private static final Class<?> SINGLETON_LIST_CLASS = Collections.singletonList( "" ).getClass();
    private static final Class<?> SINGLETON_SET_CLASS = Collections.singleton( "" ).getClass();
    private static final Class<?> SINGLETON_MAP_CLASS = Collections.singletonMap( "", "" ).getClass();

    public static String serialize( boolean object ){
        return gson.toJson( convert( object, boolean.class, false ) );
    }

    public static String serialize( byte object ){
        return gson.toJson( convert( object, byte.class, false ) );
    }

    public static String serialize( short object ){
        return gson.toJson( convert( object, short.class, false ) );
    }

    public static String serialize( int object ){
        return gson.toJson( convert( object, int.class, false ) );
    }

    public static String serialize( long object ){
        return gson.toJson( convert( object, long.class, false ) );
    }

    public static String serialize( float object ){
        return gson.toJson( convert( object, float.class, false ) );
    }

    public static String serialize( double object ){
        return gson.toJson( convert( object, double.class, false ) );
    }

    public static String serialize( Object object, boolean serializeType ){
        if( object == null ){
            return null;
        }
        Class<?> type = object.getClass();
        return gson.toJson( convert( object, type, serializeType ) );
    }

    public static String serializeCollection( Collection<?> object, boolean serializeType, boolean serializeElementType ){
        if( object == null ){
            return null;
        }
        Class<?> type = object.getClass();
        return gson.toJson( convertCollection( object, type, serializeType, serializeElementType ) );
    }

    public static boolean deserializeBoolean( String json ){
        return gson.fromJson( json, boolean.class );
    }

    public static byte deserializeByte( String json ){
        return gson.fromJson( json, byte.class );
    }

    public static short deserializeShort( String json ){
        return gson.fromJson( json, short.class );
    }

    public static int deserializeInt( String json ){
        return gson.fromJson( json, int.class );
    }

    public static long deserializeLong( String json ){
        return gson.fromJson( json, long.class );
    }

    public static float deserializeFloat( String json ){
        return gson.fromJson( json, float.class );
    }

    public static double deserializeDouble( String json ){
        return gson.fromJson( json, double.class );
    }

    public static Object deserialize( String json ){
        if( json == null ){
            return null;
        }
        return deserialize( json, null );
    }

    public static <T> T deserialize( String json, Class<T> type ){
        if( json == null ){
            return null;
        }
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse( json );
        @SuppressWarnings("unchecked")
        T result = (T)deconvert( element, type );
        return result;
    }

    public static <K, V> HashMap<K, V> deserializeHashMap( String json, Class<K> key, Class<V> value ){
        @SuppressWarnings("unchecked")
        HashMap<K, V> result = deserialize( json, HashMap.class );
        return result;
    }

    public static <T> Collection<T> deserializeCollection( String json, @SuppressWarnings("rawtypes") Class<? extends Collection> type, Class<T> elementType ){
        if( json == null ){
            return null;
        }
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse( json );
        @SuppressWarnings("unchecked")
        Collection<T> result = (Collection<T>)deconvertCollection( element, type, elementType );
        return result;
    }

    private static JsonElement convert( Object object, Class<?> type, boolean serializeType ){
        if( object == null ){
            return JsonNull.INSTANCE;
        }
        else if( isSimple( object.getClass() ) ){
            return convertSimple( object, type, serializeType );
        }
        else if( isArray( object.getClass() ) ){
            return convertArray( object, type, serializeType );
        }
        else if( isCollection( object.getClass() ) ){
            return convertCollection( object, type, serializeType, true );
        }
        else if( isMap( object.getClass() ) ){
            return convertMap( object, type, serializeType );
        }
        else{
            return convertObject( object, type, serializeType );
        }
    }

    private static Object deconvert( JsonElement element, Class<?> expectedType ){
        if( element.isJsonNull() ){
            return null;
        }
        else if( element.isJsonPrimitive() ){
            return deconvertSimple( element, expectedType );
        }

        Class<?> objectType;
        JsonElement objectElement;
        if( expectedType == null || isContainer( element ) ){
            JsonObject container = element.getAsJsonObject();
            String typeName = container.get( "c" ).getAsString();
            objectType = getClass( typeName );
            objectElement = container.get( "v" );
        }
        else{
            objectType = expectedType;
            objectElement = element;
        }

        if( isSimple( objectType ) ){
            return deconvertSimple( objectElement, objectType );
        }
        else if( isArray( objectType ) ){
            return deconvertArray( objectElement, objectType );
        }
        else if( isCollection( objectType ) ){
            return deconvertCollection( objectElement, objectType, null );
        }
        else if( isMap( objectType ) ){
            return deconvertMap( objectElement, objectType );
        }
        else{
            return deconvertObject( objectElement, objectType );
        }
    }

    private static boolean isSimple( Class<?> type ){
        return Boolean.class.isAssignableFrom( type )
                || Number.class.isAssignableFrom( type )
                || String.class.isAssignableFrom( type )
                || Date.class.isAssignableFrom( type ) || type.isEnum();
    }

    private static JsonElement convertSimple( Object object, Class<?> type, boolean serializeType ){
        JsonPrimitive primitive = null;
        if( object instanceof Boolean ){
            primitive = new JsonPrimitive( (Boolean)object );
        }
        else if( object instanceof Number ){
            primitive = new JsonPrimitive( (Number)object );
        }
        else if( object instanceof String ){
            primitive = new JsonPrimitive( (String)object );
        }
        else if( object instanceof Date ){
            primitive = new JsonPrimitive( formatDate( (Date)object ) );
        }
        else if( object.getClass().isEnum() ){
            primitive = new JsonPrimitive( ((Enum<?>)object).name() );
        }
        if( serializeType && !(object instanceof Boolean) && !(object instanceof String) ){
            JsonObject container = new JsonObject();
            container.add( "c", new JsonPrimitive( getTypeName( type ) ) );
            container.add( "v", primitive );
            return container;
        }
        else{
            return primitive;
        }
    }

    private static Object deconvertSimple( JsonElement element, Class<?> type ){
        String value = element.getAsString();
        if( Boolean.class.equals( type )
                || boolean.class.equals( type )
                || element.getAsJsonPrimitive().isBoolean() ){
            return Boolean.valueOf( value );
        }
        else if( Byte.class.equals( type ) || byte.class.equals( type ) ){
            return Byte.valueOf( value );
        }
        else if( Short.class.equals( type ) || short.class.equals( type ) ){
            return Short.valueOf( value );
        }
        else if( Integer.class.equals( type ) || int.class.equals( type ) ){
            return Integer.valueOf( value );
        }
        else if( Long.class.equals( type ) || long.class.equals( type ) ){
            return Long.valueOf( value );
        }
        else if( Float.class.equals( type ) || float.class.equals( type ) ){
            return Float.valueOf( value );
        }
        else if( Double.class.equals( type ) || double.class.equals( type ) ){
            return Double.valueOf( value );
        }
        else if( BigInteger.class.equals( type ) ){
            return new BigInteger( value );
        }
        else if( BigDecimal.class.equals( type ) ){
            return new BigDecimal( value );
        }
        else if( Date.class.equals( type ) ){
            return parseDate( value );
        }
        else if( type != null && type.isEnum() ){
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<Enum> enumClazz = (Class<Enum>)type;
            @SuppressWarnings("unchecked")
            Object result = Enum.valueOf( enumClazz, value );
            return result;
        }
        else if( String.class.equals( type ) || element.getAsJsonPrimitive().isString() ){
            return value;
        }
        throw new RuntimeException( "Unable to deconvert element " + element );
    }

    private static boolean isArray( Class<?> type ){
        return type.isArray();
    }

    private static JsonElement convertArray( Object object, Class<?> type,
                                             boolean serializeType ){
        JsonArray array = new JsonArray();
        Class<?> componentType = type.getComponentType();
        if( boolean.class.equals( componentType ) ){
            for( boolean element : (boolean[])object ){
                array.add( convert( element, componentType, false ) );
            }
        }
        else if( byte.class.equals( componentType ) ){
            for( byte element : (byte[])object ){
                array.add( convert( element, componentType, false ) );
            }
        }
        else if( short.class.equals( componentType ) ){
            for( short element : (short[])object ){
                array.add( convert( element, componentType, false ) );
            }
        }
        else if( int.class.equals( componentType ) ){
            for( int element : (int[])object ){
                array.add( convert( element, componentType, false ) );
            }
        }
        else if( long.class.equals( componentType ) ){
            for( long element : (long[])object ){
                array.add( convert( element, componentType, false ) );
            }
        }
        else if( float.class.equals( componentType ) ){
            for( float element : (float[])object ){
                array.add( convert( element, componentType, false ) );
            }
        }
        else if( double.class.equals( componentType ) ){
            for( double element : (double[])object ){
                array.add( convert( element, componentType, false ) );
            }
        }
        else{
            Class<?> innermostComponentType = getInnermostComponentType( type );
            boolean serializeElementType = !innermostComponentType.isPrimitive();
            for( Object element : (Object[])object ){
                array.add( convert( element,
                        element == null ? null : element.getClass(),
                        serializeElementType ) );
            }
        }
        if( serializeType ){
            JsonObject container = new JsonObject();
            container.add( "c", new JsonPrimitive( getTypeName( type ) ) );
            container.add( "v", array );
            return container;
        }
        else{
            return array;
        }
    }

    private static Object deconvertArray( JsonElement element, Class<?> type ){
        JsonArray array = element.getAsJsonArray();
        Class<?> componentType = type.getComponentType();
        if( boolean.class.equals( componentType ) ){
            boolean[] result = new boolean[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                result[i] = array.get( i ).getAsBoolean();
            }
            return result;
        }
        else if( byte.class.equals( componentType ) ){
            byte[] result = new byte[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                result[i] = array.get( i ).getAsByte();
            }
            return result;
        }
        else if( short.class.equals( componentType ) ){
            short[] result = new short[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                result[i] = array.get( i ).getAsShort();
            }
            return result;
        }
        else if( int.class.equals( componentType ) ){
            int[] result = new int[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                result[i] = array.get( i ).getAsInt();
            }
            return result;
        }
        else if( long.class.equals( componentType ) ){
            long[] result = new long[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                result[i] = array.get( i ).getAsLong();
            }
            return result;
        }
        else if( float.class.equals( componentType ) ){
            float[] result = new float[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                result[i] = array.get( i ).getAsFloat();
            }
            return result;
        }
        else if( double.class.equals( componentType ) ){
            double[] result = new double[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                result[i] = array.get( i ).getAsDouble();
            }
            return result;
        }
        else{
            Object resultObject = Array.newInstance( componentType, array.size() );
            Object[] result = (Object[])resultObject;
            for( int i = 0; i < array.size(); i++ ){
                Object arrayElement = deconvert( array.get( i ), componentType );
                result[i] = arrayElement;
            }
            return result;
        }
    }

    private static boolean isCollection( Class<?> type ){
        return List.class.isAssignableFrom( type ) || Set.class.isAssignableFrom( type );
    }

    private static JsonElement convertCollection( Object object, Class<?> type, boolean serializeType, boolean serializeElementType ){
        JsonArray array = new JsonArray();
        for( Object element : (Collection<?>)object ){
            array.add( convert( element, element == null ? null : element.getClass(), serializeElementType ) );
        }
        if( serializeType ){
            JsonObject container = new JsonObject();
            container.add( "c", new JsonPrimitive( getTypeName( type ) ) );
            container.add( "v", array );
            return container;
        }
        else{
            return array;
        }
    }

    private static Collection<?> deconvertCollection( JsonElement element, Class<?> type, Class<?> elementType ){
        JsonArray array = element.getAsJsonArray();
        if( ARRAYS_ASLIST_CLASS.equals( type ) ){
            Object[] values = new Object[array.size()];
            for( int i = 0; i < array.size(); i++ ){
                Object arrayElement = deconvert( array.get( i ), elementType );
                values[i] = arrayElement;
            }
            return Arrays.asList( values );
        }
        else if( SINGLETON_LIST_CLASS.equals( type ) ){
            return Collections.singletonList( deconvert( array.get( 0 ),
                    elementType ) );
        }
        else if( SINGLETON_SET_CLASS.equals( type ) ){
            return Collections.singleton( deconvert( array.get( 0 ), elementType ) );
        }
        else{
            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>)instantiate( type );
            for( int i = 0; i < array.size(); i++ ){
                collection.add( deconvert( array.get( i ), elementType ) );
            }
            return collection;
        }
    }

    private static boolean isMap( Class<?> type ){
        return Map.class.isAssignableFrom( type );
    }

    private static JsonElement convertMap( Object object, Class<?> type,
                                           boolean serializeType ){
        Set<?> keySet = ((Map<?, ?>)object).keySet();
        return isSetOfStrings( keySet ) ? convertStringKeyMap( object, type, serializeType ) : convertObjectKeyMap( object, type, serializeType );
    }

    private static Map<?, ?> deconvertMap( JsonElement element, Class<?> type ){
        if (type.equals(Map.class)){
            // default implementation if type points to the interface
            type = HashMap.class;
        }
        return element.isJsonObject() ? deconvertStringKeyMap( element, type ) : deconvertObjectKeyMap( element, type );
    }

    private static JsonElement convertObjectKeyMap( Object object, Class<?> type, boolean serializeType ){
        JsonArray array = new JsonArray();
        for( Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet() ){
            Object key = entry.getKey();
            Object value = entry.getValue();
            JsonArray entryContainer = new JsonArray();
            entryContainer.add( convert( key, value == null ? null : value.getClass(), true ) );
            entryContainer.add( convert( value, value == null ? null : value.getClass(), true ) );
            array.add( entryContainer );
        }
        if( serializeType ){
            JsonObject container = new JsonObject();
            container.add( "c", new JsonPrimitive( getTypeName( type ) ) );
            container.add( "v", array );
            return container;
        }
        else{
            return array;
        }

    }

    private static Map<?, ?> deconvertObjectKeyMap( JsonElement element, Class<?> type ){
        JsonArray jsonArray = element.getAsJsonArray();
        if( SINGLETON_MAP_CLASS.equals( type ) ){
            Object eKey = deconvert( jsonArray.get( 0 ).getAsJsonArray().get( 0 ), null );
            Object eValue = deconvert( jsonArray.get( 0 ).getAsJsonArray().get( 1 ), null );
            return Collections.singletonMap( eKey, eValue );
        }
        else{
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>)instantiate( type );
            for( int i = 0; i < jsonArray.size(); i++ ){
                Object eKey = deconvert( jsonArray.get( i ).getAsJsonArray().get( 0 ), null );
                Object eValue = deconvert( jsonArray.get( i ).getAsJsonArray().get( 1 ), null );
                map.put( eKey, eValue );
            }
            return map;
        }
    }

    private static JsonElement convertStringKeyMap( Object object, Class<?> type, boolean serializeType ){
        JsonObject jsonObject = new JsonObject();
        for( Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet() ){
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            JsonElement valueElement = convert( value, value == null ? null : value.getClass(), true );
            jsonObject.add( key, valueElement );
        }
        if( serializeType ){
            JsonObject container = new JsonObject();
            container.add( "c", new JsonPrimitive( getTypeName( type ) ) );
            container.add( "v", jsonObject );
            return container;
        }
        else{
            return jsonObject;
        }
    }

    private static Map<String, ?> deconvertStringKeyMap( JsonElement element, Class<?> type ){
        JsonObject jsonObject = element.getAsJsonObject();
        if( SINGLETON_MAP_CLASS.equals( type ) ){
            Map.Entry<String, JsonElement> entry = jsonObject.entrySet().iterator().next();
            String eKey = entry.getKey();
            Object eValue = deconvert( entry.getValue(), null );
            return Collections.singletonMap( eKey, eValue );
        }
        else{
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)instantiate( type );
            for( Map.Entry<String, JsonElement> entry : jsonObject.entrySet() ){
                String eKey = entry.getKey();
                Object eValue = deconvert( entry.getValue(), null );
                map.put( eKey, eValue );
            }
            return map;
        }
    }

    private static JsonElement convertObject( Object object, Class<?> type, boolean serializeType ){
        JsonObject jsonObject = new JsonObject();
        for( Map.Entry<String, Field> entry : getFieldMap( object.getClass() )
                .entrySet() ){
            String name = entry.getKey();
            Field field = entry.getValue();
            field.setAccessible( true );
            Object value = getValue( field, object );
            boolean serializeFieldType = value != null && !field.getType().isPrimitive() && !value.getClass().equals( field.getType() );
            JsonElement valueElement = convert( value, value == null ? null : value.getClass(), serializeFieldType );
            jsonObject.add( name, valueElement );
        }
        if( serializeType ){
            JsonObject container = new JsonObject();
            container.add( "c", new JsonPrimitive( getTypeName( type ) ) );
            container.add( "v", jsonObject );
            return container;
        }
        else{
            return jsonObject;
        }
    }

    private static Object deconvertObject( JsonElement element, Class<?> type ){
        JsonObject jsonObject = element.getAsJsonObject();
        Object object = instantiate( type );
        Map<String, Field> fieldMap = getFieldMap( object.getClass() );
        for( Map.Entry<String, JsonElement> entry : jsonObject.entrySet() ){
            String name = entry.getKey();
            Field field = fieldMap.get( name );
            field.setAccessible( true );
            Object value = deconvert( entry.getValue(), field.getType() );
            setValue( field, object, value );
        }
        return object;
    }

    private static String getTypeName( Class<?> clazz ){
        if( !clazz.isArray() ){
            return clazz.getName();
        }
        String result = "[]";
        Class<?> componentType = clazz.getComponentType();
        while( componentType.isArray() ){
            result = result + "[]";
            componentType = componentType.getComponentType();
        }
        result = componentType.getName() + result;
        return result;
    }

    private static Class<?> getClass( String typeName ){
        if( typeName.endsWith( "[]" ) ){
            int index = typeName.indexOf( '[' );
            String componentTypeName = typeName.substring( 0, index );
            int levels = (typeName.length() - index) / 2;
            Class<?> componentType = loadClass( componentTypeName );
            int[] dimensions = new int[levels];
            return Array.newInstance( componentType, dimensions ).getClass();
        }
        else{
            return loadClass( typeName );
        }
    }

    private static Class<?> loadClass( String className ){
        if( boolean.class.getName().equals( className ) ){
            return boolean.class;
        }
        else if( byte.class.getName().equals( className ) ){
            return byte.class;
        }
        else if( short.class.getName().equals( className ) ){
            return short.class;
        }
        else if( int.class.getName().equals( className ) ){
            return int.class;
        }
        else if( long.class.getName().equals( className ) ){
            return long.class;
        }
        else if( float.class.getName().equals( className ) ){
            return float.class;
        }
        else if( double.class.getName().equals( className ) ){
            return double.class;
        }
        else if( ARRAYS_ASLIST_CLASSNAME.equals( className ) ){
            return ARRAYS_ASLIST_CLASS;
        }
        else if( SINGLETON_LIST_CLASSNAME.equals( className ) ){
            return SINGLETON_LIST_CLASS;
        }
        else if( SINGLETON_SET_CLASSNAME.equals( className ) ){
            return SINGLETON_SET_CLASS;
        }
        else if( SINGLETON_MAP_CLASSNAME.equals( className ) ){
            return SINGLETON_MAP_CLASS;
        }
        else{
            try{
                return Thread.currentThread().getContextClassLoader().loadClass( className );
            }
            catch( ClassNotFoundException e ){
                throw new RuntimeException( e );
            }
        }
    }

    private static boolean isContainer( JsonElement element ){
        if( !element.isJsonObject() ){
            return false;
        }
        JsonObject object = (JsonObject)element;
        return object.get( "c" ) != null && object.get( "v" ) != null;
    }

    private static boolean isSetOfStrings( Set<?> set ){
        for( Object element : set ){
            if( !(element == null || element instanceof String) ){
                return false;
            }
        }
        return true;
    }

    private static Object instantiate( Class<?> clazz ){
        try{
            return clazz.newInstance();
        }
        catch( InstantiationException e ){
            throw new RuntimeException( e );
        }
        catch( IllegalAccessException e ){
            throw new RuntimeException( e );
        }
    }

    private static Map<String, Field> getFieldMap( Class<?> clazz ){
        Map<String, Field> fields = new LinkedTreeMap<String, Field>();
        Class<?> current = clazz;
        String prefix = "";
        while( !Object.class.equals( current ) ){
            for( Field field : current.getDeclaredFields() ){
                if( Modifier.isStatic( field.getModifiers() ) && Modifier.isFinal( field.getModifiers() ) ){
                    // ignore STATIC FINAL fields
                    continue;
                }
                fields.put( prefix + field.getName(), field );
            }
            current = current.getSuperclass();
            prefix = prefix + "super.";
        }
        return fields;
    }

    private static void setValue( Field field, Object object, Object value ){
        try{
            field.set( object, value );
        }
        catch( IllegalArgumentException e ){
            throw new RuntimeException( e );
        }
        catch( IllegalAccessException e ){
            throw new RuntimeException( e );
        }
    }

    private static Object getValue( Field field, Object object ){
        try{
            return field.get( object );
        }
        catch( IllegalArgumentException e ){
            throw new RuntimeException( e );
        }
        catch( IllegalAccessException e ){
            throw new RuntimeException( e );
        }
    }

    private static Class<?> getInnermostComponentType( Class<?> type ){
        if( !type.isArray() ){
            return type;
        }
        else{
            return type.getComponentType();
        }
    }

    private static String formatDate( Date date ){
        return createFormater().format( date );
    }

    private static Date parseDate( String date ){
        try{
            return createFormater().parse( date );
        }
        catch( ParseException e ){
            throw new RuntimeException( "Unexpected date format " + date );
        }
    }

    private static SimpleDateFormat createFormater(){
        return new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss.S" );
    }
}
