package ee.telekom.workflow.graph;

import java.util.Map;

import ee.telekom.workflow.graph.el.ReservedVariables;

/**
 * An {@link Environment} is a container of attributes that can be
 * added/changed, read, and removed.
 */
public interface Environment{

    /**
     * Get the attribute of the given name to the given string value.
     * 
     * @param name
     *            the name of the attribute to get
     */
    Object getAttribute( String name );

    /**
     * Sets the attribute of the given name to the given value.
     * 
     * @param name
     *            the name of the attribute to set; you can't use any of the {@link ReservedVariables} names
     * @param value
     *            the value to set the attribute to
     */
    void setAttribute( String name, Object value );

    /**
     * Unsets any attribute with the given name.
     * 
     * @param name
     *            the name of the attribute to remove
     */
    void removeAttribute( String name );

    /**
     * Returns an {@link Iterable} of attribute names.
     * 
     * @return {@link Iterable} of attribute names
     */
    Iterable<String> getAttributeNames();

    /**
     * Checks if an attribute with given name exists.
     * 
     * @param name the name of the attribute to check
     * @return true, if an attribute with given name exists in the environment
     */
    boolean containsAttribute( String name );

    /**
     * Copy all attributes from given environment into this environment.
     * 
     * @param env
     *            the environment to copy
     */
    void importEnvironment( Environment environment );

    /**
     * Returns the environment attributes as unmodifiable map.
     * @return the environment attributes as unmodifiable map.
     */
    Map<String, Object> getAttributesAsMap();

    /**
     * Empties the environment.
     */
    void clear();

}