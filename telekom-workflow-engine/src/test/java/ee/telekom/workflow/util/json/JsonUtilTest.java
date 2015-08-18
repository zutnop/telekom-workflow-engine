package ee.telekom.workflow.util.json;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.telekom.workflow.util.JsonUtil;

public class JsonUtilTest{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Test
    public void testPrimitives(){
        assertRoundtrip( true );
        assertRoundtrip( false );

        assertRoundtrip( (byte)0 );
        assertRoundtrip( Byte.MIN_VALUE );
        assertRoundtrip( Byte.MAX_VALUE );

        assertRoundtrip( (short)0 );
        assertRoundtrip( Short.MIN_VALUE );
        assertRoundtrip( Short.MAX_VALUE );

        assertRoundtrip( 0 );
        assertRoundtrip( 2 );
        assertRoundtrip( Integer.MIN_VALUE );
        assertRoundtrip( Integer.MAX_VALUE );

        assertRoundtrip( 0l );
        assertRoundtrip( Long.MIN_VALUE );
        assertRoundtrip( Long.MAX_VALUE );

        assertRoundtrip( 0f );
        assertRoundtrip( 2f );
        assertRoundtrip( Float.MIN_VALUE );
        assertRoundtrip( Float.MAX_VALUE );

        assertRoundtrip( 0d );
        assertRoundtrip( 2d );
        assertRoundtrip( Double.MIN_VALUE );
        assertRoundtrip( Double.MAX_VALUE );
    }

    @Test
    public void testNull(){
        assertRoundtrip( null, true, String.class );
        assertRoundtrip( null, true, Integer.class );
        assertRoundtrip( null, true, null );

        assertRoundtrip( null, false, String.class );
        assertRoundtrip( null, false, Integer.class );
        assertRoundtrip( null, false, null );

    }

    @Test
    public void testBoolean(){
        assertRoundtrip( true, true, Boolean.class );
        assertRoundtrip( false, true, Boolean.class );

        assertRoundtrip( true, true, null );
        assertRoundtrip( false, true, null );

        assertRoundtrip( true, false, Boolean.class );
        assertRoundtrip( false, false, Boolean.class );

        assertRoundtrip( true, false, null );
        assertRoundtrip( false, false, null );

    }

    @Test
    public void testString(){
        assertRoundtrip( "test", true, String.class );
        assertRoundtrip( "test", true, null );
        assertRoundtrip( "test", false, String.class );
        assertRoundtrip( "test", false, null );
    }

    @Test
    public void testSimple(){
        test( 0 );
        test( 2 );
        test( 0f );
        test( 2f );
        test( 0d );
        test( 2d );

        test( Boolean.TRUE );
        test( Boolean.FALSE );
        test( Byte.MIN_VALUE );
        test( Byte.MAX_VALUE );
        test( Short.MIN_VALUE );
        test( Short.MAX_VALUE );
        test( Integer.MIN_VALUE );
        test( Integer.MAX_VALUE );
        test( Long.MIN_VALUE );
        test( Long.MAX_VALUE );
        test( Float.MIN_VALUE );
        test( Float.MAX_VALUE );
        test( Double.MIN_VALUE );
        test( Double.MAX_VALUE );

        test( new BigInteger( "0" ) );
        test( new BigInteger( "2" ) );
        test( new BigInteger( "9" + Long.MAX_VALUE ) );
        test( new BigDecimal( 0 ) );
        test( new BigDecimal( "9" + Long.MAX_VALUE ) );
        test( new BigDecimal( "9.9" ).add( new BigDecimal( Long.MAX_VALUE ) ) );
        test( TestEnum.ONE );
        test( TestEnum.TWO );
        Date now = new Date();
        test( now );
        test( new java.sql.Date( now.getTime() ) );
        test( new java.sql.Time( now.getTime() ) );
        test( new java.sql.Timestamp( now.getTime() ) );
        test( new Date( 0 ) );
        test( new java.sql.Date( 0 ) );
        test( new java.sql.Time( 0 ) );
        test( new java.sql.Timestamp( 0 ) );
    }

    @Test
    public void testPrimitiveArray(){
        test( new boolean[]{} );
        test( new boolean[]{true, false} );
        test( new boolean[][]{} );
        test( new boolean[][]{null} );
        test( new boolean[][]{new boolean[]{true, false}, null} );

        test( new Boolean[]{} );
        test( new Boolean[]{true, false, null} );
        test( new Boolean[][]{} );
        test( new Boolean[][]{null} );
        test( new Boolean[][]{new Boolean[]{true, false, null}, null} );
        test( new Object[]{true, false, null} );

        test( new byte[]{} );
        test( new byte[]{0, Byte.MIN_VALUE, Byte.MAX_VALUE} );
        test( new byte[][]{} );
        test( new byte[][]{null} );
        test( new byte[][]{new byte[]{0, Byte.MIN_VALUE, Byte.MAX_VALUE}, null} );

        test( new Byte[]{} );
        test( new Byte[]{0, Byte.MIN_VALUE, Byte.MAX_VALUE, null} );
        test( new Byte[][]{} );
        test( new Byte[][]{null} );
        test( new Byte[][]{new Byte[]{0, Byte.MIN_VALUE, Byte.MAX_VALUE, null},
                null} );
        test( new Object[]{0, Byte.MIN_VALUE, Byte.MAX_VALUE, null} );

        test( new short[]{} );
        test( new short[]{0, Short.MIN_VALUE, Short.MAX_VALUE} );
        test( new short[][]{} );
        test( new short[][]{null} );
        test( new short[][]{new short[]{0, Short.MIN_VALUE, Short.MAX_VALUE},
                null} );

        test( new Short[]{} );
        test( new Short[]{0, Short.MIN_VALUE, Short.MAX_VALUE, null} );
        test( new Short[][]{} );
        test( new Short[][]{null} );
        test( new Short[][]{
                new Short[]{0, Short.MIN_VALUE, Short.MAX_VALUE, null}, null} );
        test( new Object[]{0, Short.MIN_VALUE, Short.MAX_VALUE, null} );

        test( new int[]{} );
        test( new int[]{0, Integer.MIN_VALUE, Integer.MAX_VALUE} );
        test( new int[][]{} );
        test( new int[][]{null} );
        test( new int[][]{new int[]{0, Integer.MIN_VALUE, Integer.MAX_VALUE},
                null} );

        test( new Integer[]{} );
        test( new Integer[]{0, Integer.MIN_VALUE, Integer.MAX_VALUE, null} );
        test( new Integer[][]{} );
        test( new Integer[][]{null} );
        test( new Integer[][]{
                new Integer[]{0, Integer.MIN_VALUE, Integer.MAX_VALUE, null},
                null} );
        test( new Object[]{0, Integer.MIN_VALUE, Integer.MAX_VALUE, null} );

        test( new long[]{} );
        test( new long[]{0l, Long.MIN_VALUE, Long.MAX_VALUE} );
        test( new long[][]{} );
        test( new long[][]{null} );
        test( new long[][]{new long[]{0l, Long.MIN_VALUE, Long.MAX_VALUE}, null} );

        test( new Long[]{} );
        test( new Long[]{0l, Long.MIN_VALUE, Long.MAX_VALUE, null} );
        test( new Long[][]{} );
        test( new Long[][]{null} );
        test( new Long[][]{new Long[]{0l, Long.MIN_VALUE, Long.MAX_VALUE, null},
                null} );
        test( new Object[]{0, Long.MIN_VALUE, Long.MAX_VALUE, null} );

        test( new float[]{} );
        test( new float[]{0f, 2f, Float.MIN_VALUE, Float.MAX_VALUE} );
        test( new float[][]{} );
        test( new float[][]{null} );
        test( new float[][]{
                new float[]{0f, 2f, Float.MIN_VALUE, Float.MAX_VALUE}, null} );

        test( new Float[]{} );
        test( new Float[]{0f, 2f, Float.MIN_VALUE, Float.MAX_VALUE, null} );
        test( new Float[][]{} );
        test( new Float[][]{null} );
        test( new Float[][]{
                new Float[]{0f, 2f, Float.MIN_VALUE, Float.MAX_VALUE, null},
                null} );
        test( new Object[]{0f, 2f, Float.MIN_VALUE, Float.MAX_VALUE, null} );

        test( new double[]{} );
        test( new double[]{0, 2d, Double.MIN_VALUE, Double.MAX_VALUE} );
        test( new double[][]{} );
        test( new double[][]{null} );
        test( new double[][]{
                new double[]{0d, 2d, Double.MIN_VALUE, Double.MAX_VALUE}, null} );

        test( new Double[]{} );
        test( new Double[]{0d, 2d, Double.MIN_VALUE, Double.MAX_VALUE, null} );
        test( new Double[][]{} );
        test( new Double[][]{null} );
        test( new Double[][]{
                new Double[]{0d, 2d, Double.MIN_VALUE, Double.MAX_VALUE, null},
                null} );
        test( new Object[]{0d, 2d, Double.MIN_VALUE, Double.MAX_VALUE, null} );
    }

    @Test
    public void testObjectArray(){
        test( new Object[]{} );
        test( new Object[]{null} );
        test( new Object[]{new Object(), new Object[]{true, false, null}, null} );

        test( new Object[][]{} );
        test( new Object[][]{null} );
        test( new Object[][]{new Object[]{new Object(),
                new Object[]{true, false, null}, new TestObject[][]{null}, null}} );

        test( new String[]{} );
        test( new String[]{"test", null} );
        test( new Object[]{"test", null} );

        test( new Date() );
        test( new Date[]{} );
        test( new Date[]{new Date(), null} );
        test( new Object[]{new Date(), null} );

        test( new TestEnum[]{} );
        test( new TestEnum[]{TestEnum.ONE, null} );
        test( new TestEnum[][]{new TestEnum[]{TestEnum.ONE}, null} );
        test( new Object[]{TestEnum.ONE, null} );

        test( new TestObject[]{null} );
        test( new TestObject[][]{null} );
    }

    @Test
    public void testSingletons(){
        test( Collections.singleton( Boolean.FALSE ) );
        test( Collections.singletonList( Boolean.FALSE ) );
        test( Collections.singletonMap( "key", Boolean.FALSE ) );
        test( Collections.singletonMap( Boolean.TRUE, Boolean.FALSE ) );

        test( Collections.singleton( Byte.MIN_VALUE ) );
        test( Collections.singletonList( Byte.MIN_VALUE ) );
        test( Collections.singletonMap( "key", Byte.MIN_VALUE ) );
        test( Collections.singletonMap( Byte.MAX_VALUE, Byte.MIN_VALUE ) );

        test( Collections.singleton( Short.MIN_VALUE ) );
        test( Collections.singletonList( Short.MIN_VALUE ) );
        test( Collections.singletonMap( "key", Short.MIN_VALUE ) );
        test( Collections.singletonMap( Short.MAX_VALUE, Short.MIN_VALUE ) );

        test( Collections.singleton( Integer.MIN_VALUE ) );
        test( Collections.singletonList( Integer.MIN_VALUE ) );
        test( Collections.singletonMap( "key", Integer.MIN_VALUE ) );
        test( Collections.singletonMap( Integer.MAX_VALUE, Integer.MIN_VALUE ) );

        test( Collections.singleton( Long.MIN_VALUE ) );
        test( Collections.singletonList( Long.MIN_VALUE ) );
        test( Collections.singletonMap( "key", Long.MIN_VALUE ) );
        test( Collections.singletonMap( Long.MAX_VALUE, Long.MIN_VALUE ) );

        test( Collections.singleton( Float.MIN_VALUE ) );
        test( Collections.singletonList( Float.MIN_VALUE ) );
        test( Collections.singletonMap( "key", Float.MIN_VALUE ) );
        test( Collections.singletonMap( Float.MAX_VALUE, Float.MIN_VALUE ) );

        test( Collections.singleton( Double.MIN_VALUE ) );
        test( Collections.singletonList( Double.MIN_VALUE ) );
        test( Collections.singletonMap( "key", Double.MIN_VALUE ) );
        test( Collections.singletonMap( Double.MAX_VALUE, Double.MIN_VALUE ) );

        test( Collections.singleton( new BigInteger( "0" ) ) );
        test( Collections.singletonList( new BigInteger( "0" ) ) );
        test( Collections.singletonMap( "key", new BigInteger( "0" ) ) );
        test( Collections
                .singletonMap( new BigInteger( "10" ), new BigInteger( "0" ) ) );

        test( Collections.singleton( new BigDecimal( 0 ) ) );
        test( Collections.singletonList( new BigDecimal( 0 ) ) );
        test( Collections.singletonMap( "key", new BigDecimal( 0 ) ) );
        test( Collections.singletonMap( new BigDecimal( "10" ), new BigDecimal( 0 ) ) );

        test( Collections.singleton( "test" ) );
        test( Collections.singletonList( "test" ) );
        test( Collections.singletonMap( "key", "test" ) );

        test( Collections.singleton( new Date() ) );
        test( Collections.singletonList( new Date() ) );
        test( Collections.singletonMap( "key", new Date() ) );
        test( Collections.singletonMap( new Date( 0 ), new Date() ) );

        test( Collections.singleton( TestEnum.ONE ) );
        test( Collections.singletonList( TestEnum.ONE ) );
        test( Collections.singletonMap( "key", TestEnum.ONE ) );
        test( Collections.singletonMap( TestEnum.TWO, TestEnum.ONE ) );

        test( Collections.singleton( new TestObject() ) );
        test( Collections.singletonList( new TestObject() ) );
        test( Collections.singletonMap( "key", new TestObject() ) );
        test( Collections.singletonMap( new TestObject(), new TestObject() ) );

        test( Collections.singleton( new Object[]{} ) );
        test( Collections.singletonList( new Object[]{} ) );
        test( Collections.singletonMap( "key1", new Object[]{} ) );
        test( Collections.singletonMap( new Object[]{}, new Object[]{} ) );

        test( Collections.singleton( new String[]{"hello"} ) );
        test( Collections.singletonList( new String[]{"hello"} ) );
        test( Collections.singletonMap( "key1", new String[]{"hello"} ) );
        test( Collections.singletonMap( new String[]{"hello"},
                new String[]{"hello"} ) );

        test( Collections.singleton( new short[]{} ) );
        test( Collections.singletonList( new short[]{} ) );
        test( Collections.singletonMap( new short[]{}, new short[]{} ) );
        test( Collections.singletonMap( new short[]{}, new short[]{} ) );

        test( Collections.singleton( new short[]{1} ) );
        test( Collections.singletonList( new short[]{1} ) );
        test( Collections.singletonMap( "key1", new short[]{1} ) );
        test( Collections.singletonMap( new short[]{1}, new short[]{1} ) );
    }

    @Test
    public void testArraysAsList(){
        test( Arrays.asList( null, null ) );
        test( Arrays.asList( new Date(), null ) );
        test( Arrays.asList( TestEnum.ONE, null ) );
        test( Arrays.asList( new Object[]{} ) );
        test( Arrays.asList( new String[]{"hello"} ) );
        test( Arrays.asList( new short[]{1} ) );
    }

    @Test
    public void testObject(){
        test( new TestObject() );
        test( new TestChildObject() );
        test( TestObject.createSimple() );
        test( TestObject.createComplex() );
        test( TestChildObject.createSimple() );
        test( TestChildObject.createComplex() );

        test( TestObject.createSimple() );
        test( new TestObject[]{TestObject.createSimple(), null} );
        test( Collections.singleton( TestObject.createSimple() ) );
        test( Arrays.asList( TestObject.createSimple(), null ) );
        test( Collections.singletonMap( "simple", TestObject.createSimple() ) );
    }

    @Test
    public void testMapWithNullValue(){
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put( "one", "one" );
        attributes.put( "two", null );
        test( attributes );
    }

    private void test( Object object ){
        assertRoundtrip( object, true, object.getClass() );
        assertRoundtrip( object, true, null );
        assertRoundtrip( object, false, object.getClass() );
    }

    private void assertRoundtrip( boolean object1 ){
        String json1 = JsonUtil.serialize( object1 );
        boolean object2 = JsonUtil.deserializeBoolean( json1 );
        String json2 = JsonUtil.serialize( object2 );
        compare( object1, json1, object2, json2 );
    }

    private void assertRoundtrip( byte object1 ){
        String json1 = JsonUtil.serialize( object1 );
        byte object2 = JsonUtil.deserializeByte( json1 );
        String json2 = JsonUtil.serialize( object2 );
        compare( object1, json1, object2, json2 );
    }

    private void assertRoundtrip( short object1 ){
        String json1 = JsonUtil.serialize( object1 );
        short object2 = JsonUtil.deserializeShort( json1 );
        String json2 = JsonUtil.serialize( object2 );
        compare( object1, json1, object2, json2 );
    }

    private void assertRoundtrip( int object1 ){
        String json1 = JsonUtil.serialize( object1 );
        int object2 = JsonUtil.deserializeInt( json1 );
        String json2 = JsonUtil.serialize( object2 );
        compare( object1, json1, object2, json2 );
    }

    private void assertRoundtrip( long object1 ){
        String json1 = JsonUtil.serialize( object1 );
        long object2 = JsonUtil.deserializeLong( json1 );
        String json2 = JsonUtil.serialize( object2 );
        compare( object1, json1, object2, json2 );
    }

    private void assertRoundtrip( float object1 ){
        String json1 = JsonUtil.serialize( object1 );
        float object2 = JsonUtil.deserializeFloat( json1 );
        String json2 = JsonUtil.serialize( object2 );
        compare( object1, json1, object2, json2 );
    }

    private void assertRoundtrip( double object1 ){
        String json1 = JsonUtil.serialize( object1 );
        double object2 = JsonUtil.deserializeDouble( json1 );
        String json2 = JsonUtil.serialize( object2 );
        compare( object1, json1, object2, json2 );
    }

    private void assertRoundtrip( Object object1, boolean serializeType,
                                  Class<?> hint ){
        String json1;
        try{
            json1 = JsonUtil.serialize( object1, serializeType );
        }
        catch( Exception e ){
            String message = "serialization error with arguments "
                    + "\nobject=" + deepToString( object1 ) + "\nserializeType="
                    + serializeType + "\n";
            throw new RuntimeException( message, e );
        }
        Object object2;
        try{
            object2 = JsonUtil.deserialize( json1, hint );
        }
        catch( Exception e ){
            String message = "deserialization error with arguments "
                    + "\njson=" + json1 + "\nhint=" + hint;
            throw new RuntimeException( message, e );
        }
        String json2 = JsonUtil.serialize( object2, serializeType );
        compare( object1, json1, object2, json2 );
    }

    private void compare( Object object1, String json1, Object object2,
                          String json2 ){
        String comparison = "\njson1=" + json1 + "\njson2=" + json2
                + "\nobject1=" + deepToString( object1 ) + "\nobject2="
                + deepToString( object2 );
        if( !deepEquals( json1, json2 ) ){
            Assert.fail( "json representations are not identical: " + comparison );
        }
        if( !deepEquals( object1, object2 ) ){
            Assert.fail( "object representations are not identical : "
                    + comparison );
        }
    }

    private boolean deepEquals( Object o1, Object o2 ){
        // Testing whether both objects are null or only one
        if( o1 == null && o2 == null ){
            return true;
        }
        if( (o1 == null && o2 != null) || (o1 != null && o2 == null) ){
            log( "null mismatch" );
            return false;
        }

        if( !o1.getClass().equals( o2.getClass() ) ){
            return false;
        }

        if( Object.class.equals( o1.getClass() ) ){
            return true;
        }

        if( o1 instanceof Number ){
            boolean equal = o1.equals( o2 );
            if( !equal ){
                log( "number mismatch " + o1 + " " + o2 );
            }
            return equal;
        }

        if( o1 instanceof Date ){
            boolean equal = o1.equals( o2 );
            if( !equal ){
                log( "date mismatch " + o1 + " " + o2 );
            }
            return equal;
        }

        if( o1 instanceof Collection ){
            List<?> l1 = new ArrayList<Object>( (Collection<?>)o1 );
            List<?> l2 = new ArrayList<Object>( (Collection<?>)o2 );
            if( l1.size() != l2.size() ){
                log( "collection size mismatch" );
                return false;
            }
            for( int i = 1; i < l1.size(); i++ ){
                if( !deepEquals( l1.get( i ), l2.get( i ) ) ){
                    log( "collection element mismatch" );
                    return false;
                }
            }
            return true;
        }

        if( o1 instanceof Map ){
            Map<?, ?> m1 = (Map<?, ?>)o1;
            Map<?, ?> m2 = (Map<?, ?>)o2;
            if( m1.size() != m2.size() ){
                log( "map size mismatch" );
                return false;
            }
            for( Object key1 : m1.keySet() ){
                Object key2 = null;
                // Two arrays are equal only if they are identical.
                // In order to support array's as keys, we are manually
                // looking for key2 which is a key equal to key1.
                for( Object key2candidate : m2.keySet() ){
                    if( deepEquals( key1, key2candidate ) ){
                        key2 = key2candidate;
                        break;
                    }
                }
                if( !deepEquals( m1.get( key1 ), m2.get( key2 ) ) ){
                    log( "map element mismatch" );
                    return false;
                }
            }
            return true;
        }

        // Testing whether both objects are arrays and if so, whether their
        // elements are identical
        if( o1.getClass().isArray() ){
            Class<?> o1ComponentType = o1.getClass().getComponentType();
            Class<?> o2ComponentType = o2.getClass().getComponentType();

            if( !o1ComponentType.equals( o2ComponentType ) ){
                log( "array component type mismatch" );
                return false;
            }

            if( boolean.class.equals( o1ComponentType ) ){
                boolean equal = Arrays.equals( (boolean[])o1, (boolean[])o2 );
                if( !equal ){
                    log( "boolean array mismatch" );
                }
                return equal;
            }
            else if( byte.class.equals( o1ComponentType ) ){
                boolean equal = Arrays.equals( (byte[])o1, (byte[])o2 );
                if( !equal ){
                    log( "byte array mismatch" );
                }
                return equal;
            }
            else if( short.class.equals( o1ComponentType ) ){
                boolean equal = Arrays.equals( (short[])o1, (short[])o2 );
                if( !equal ){
                    log( "short array mismatch" );
                }
                return equal;
            }
            else if( int.class.equals( o1ComponentType ) ){
                boolean equal = Arrays.equals( (int[])o1, (int[])o2 );
                if( !equal ){
                    log( "int array mismatch" );
                }
                return equal;
            }
            else if( long.class.equals( o1ComponentType ) ){
                boolean equal = Arrays.equals( (long[])o1, (long[])o2 );
                if( !equal ){
                    log( "long array mismatch" );
                }
                return equal;
            }
            else if( float.class.equals( o1ComponentType ) ){
                boolean equal = Arrays.equals( (float[])o1, (float[])o2 );
                if( !equal ){
                    log( "float array mismatch" );
                }
                return equal;
            }
            else if( double.class.equals( o1ComponentType ) ){
                boolean equal = Arrays.equals( (double[])o1, (double[])o2 );
                if( !equal ){
                    log( "double array mismatch" );
                }
                return equal;
            }
            else{
                Object[] a1 = (Object[])o1;
                Object[] a2 = (Object[])o2;
                if( a1.length != a2.length ){
                    log( "object array size mismatch" );
                    return false;
                }
                for( int i = 0; i < a1.length; i++ ){
                    if( !deepEquals( a1[i], a2[i] ) ){
                        log( "object array element mismatch" );
                        return false;
                    }
                }
                return true;
            }
        }

        boolean equal = o1.equals( o2 );
        if( !equal ){
            log( "object equality mismatch" );
        }
        return equal;
    }

    private String deepToString( Object o ){
        if( o == null ){
            return "null";
        }

        if( o.getClass().isArray() ){
            Class<?> componentType = o.getClass().getComponentType();
            if( boolean.class.equals( componentType ) ){
                return Arrays.toString( (boolean[])o );
            }
            else if( byte.class.equals( componentType ) ){
                return Arrays.toString( (byte[])o );
            }
            else if( short.class.equals( componentType ) ){
                return Arrays.toString( (short[])o );
            }
            else if( int.class.equals( componentType ) ){
                return Arrays.toString( (int[])o );
            }
            else if( long.class.equals( componentType ) ){
                return Arrays.toString( (long[])o );
            }
            else if( float.class.equals( componentType ) ){
                return Arrays.toString( (float[])o );
            }
            else if( double.class.equals( componentType ) ){
                return Arrays.toString( (double[])o );
            }
            else{
                Object[] a = (Object[])o;
                String result = "[";
                for( Object e : a ){
                    result = result + deepToString( e ) + ",";
                }
                if( a.length > 0 ){
                    result = result.subSequence( 0, result.length() - 1 ) + "]";
                }
                else{
                    result = result + "]";
                }
                return result;
            }
        }

        if( o instanceof Collection ){
            Collection<?> c = (Collection<?>)o;
            String result = "[";
            for( Object e : c ){
                result = result + deepToString( e ) + ",";
            }
            if( c.size() > 0 ){
                result = result.subSequence( 0, result.length() - 1 ) + "]";
            }
            else{
                result = result + "]";
            }
            return result;
        }

        if( o instanceof Map ){
            Map<?, ?> m = (Map<?, ?>)o;
            String result = "[";
            for( Map.Entry<?, ?> e : m.entrySet() ){
                result = result + deepToString( e.getKey() ) + ":"
                        + deepToString( e.getValue() ) + ",";
            }
            if( m.size() > 0 ){
                result = result.subSequence( 0, result.length() - 1 ) + "]";
            }
            else{
                result = result + "]";
            }
            return result;
        }

        return o.toString();
    }

    private void log( String message ){
        log.debug( message );
    }

}
