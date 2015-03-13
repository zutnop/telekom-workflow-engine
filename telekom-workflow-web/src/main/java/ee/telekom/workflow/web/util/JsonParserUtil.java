package ee.telekom.workflow.web.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Utility to provide NULL safe manual Json parsing operations
 */
public class JsonParserUtil{

    private static final JsonParser parser = new JsonParser();

    public static JsonObject parseJson( String body ){
        return parser.parse( body ).getAsJsonObject();
    }

    public static String getAsNullSafeString( JsonObject object, String memberName ){
        if( object != null ){
            JsonElement element = object.get( memberName );
            return element == null || element instanceof JsonNull ? null : element.getAsString();
        }
        return null;
    }

    public static Integer getAsNullSafeInteger( JsonObject object, String memberName ){
        if( object != null ){
            JsonElement element = object.get( memberName );
            return element == null || element instanceof JsonNull ? null : element.getAsInt();
        }
        return null;
    }

    public static String toNullSafeJsonString( JsonObject object, String memberName ){
        if( object != null ){
            JsonElement element = object.get( memberName );
            return element != null ? element.toString() : null;
        }
        return null;
    }
}
