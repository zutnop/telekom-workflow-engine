package ee.telekom.workflow.api;

import java.util.LinkedList;
import java.util.List;

import ee.telekom.workflow.api.Element.Type;
import ee.telekom.workflow.graph.Node;

/**
 * A {@link Row} corresponds to a row of DSL method invocations in their typically formatting.
 * In the typical formatting, a row has the syntax of:  [OutputElement] MainElement [InputElement]*.
 * <p>
 * See {@link ElementUtil#isOutputElement(Element)}, {@link ElementUtil#isMainElement(Element)}, and
 * {@link ElementUtil#isInputElement(Element)} for how elements are categorised.
 * <p>
 * For instance, each of the following code rows a code will translate into a {@link Row} when parsing the DSL.
 * <pre>
 *   .if_(1,"condition)
 *     .value("account").call(2,"accountService","findAccount","{accountId}");
 *   .endIf();
 * </pre>
 * A maybe associated with a {@link Node} when parsing the {@link Tree} of rows. The associated node is the created node
 * based on the elements in the given row.
 */
public class Row{
    private Element outputElement;
    private Element mainElement;
    private List<Element> inputElements = new LinkedList<>();
    private Node node;

    public Row( Element... tokens ){
        if( tokens != null ){
            for( Element token : tokens ){
                addToken( token );
            }
        }
    }

    public void addToken( Element element ){
        if( ElementUtil.isOutputElement( element ) ){
            outputElement = element;
        }
        else if( ElementUtil.isMainElement( element ) ){
            mainElement = element;
        }
        else if( ElementUtil.isInputElement( element ) ){
            inputElements.add( element );
        }
        else{
            throw new IllegalStateException( "Cannot add element of unknown type: " + element );
        }
    }

    public Element getMainElement(){
        return mainElement;
    }

    /**
     * Convenience method returning the main element's id.
     * @return the main element's id.
     * @throws NullPointerException if the row's main element does not define an id.
     */
    public int getId(){
        return mainElement.getId();
    }

    /**
     * Convenience method returning the main element's type.
     * @return the main element's type.
     */
    public Type getType(){
        return mainElement.getType();
    }

    public Element getOutputElement(){
        return outputElement;
    }

    /**
     * Returns a list of the row's input elements. The returned value will never be <code>null</code>. 
     */
    public List<Element> getInputElement(){
        return inputElements;
    }

    public void setNode( Node node ){
        this.node = node;
    }

    public Node getNode(){
        return node;
    }

    public boolean hasNode(){
        return node != null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if( outputElement != null ){
            sb.append( outputElement + " " );
        }
        if( mainElement != null ){
            sb.append( mainElement + " " );
        }
        for( Element inputToken : inputElements ){
            sb.append( inputToken + " " );
        }
        return sb.toString();
    }

}