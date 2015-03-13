package ee.telekom.workflow.api;

import java.util.LinkedList;
import java.util.List;

/**
 * A tree implementation where a tree node may carry content. Every node is aware of its parent, its previous and next sibling, and its child node.
 * The implementation provides a method to find the tree node which is next in pre-order iteration. 
 */
public class Tree<T> {
    private T content;
    private Tree<T> parent;
    private Tree<T> previous;
    private Tree<T> next;
    private LinkedList<Tree<T>> children = new LinkedList<>();

    public static <T> Tree<T> root( Class<T> clazz ){
        return new Tree<T>( null );
    }

    public static <T> Tree<T> of( T content ){
        return new Tree<T>( content );
    }

    private Tree( T content ){
        this.content = content;
    }

    public T getContent(){
        return content;
    }

    public Tree<T> getParent(){
        return parent;
    }

    public boolean hasPrevious(){
        return previous != null;
    }

    public Tree<T> getPrevious(){
        return previous;
    }

    public boolean hasNext(){
        return next != null;
    }

    public Tree<T> getNext(){
        return next;
    }

    public boolean hasChildren(){
        return !children.isEmpty();
    }

    public Tree<T> getFirstChild(){
        return children.getFirst();
    }

    public Tree<T> getLastChild(){
        return children.getLast();
    }

    public List<Tree<T>> getChildren(){
        return children;
    }

    public Tree<T> addChild( Tree<T> child ){
        if( !children.isEmpty() ){
            children.getLast().next = child;
            child.previous = children.getLast();
        }
        child.parent = this;
        children.add( child );
        return child;
    }

    public Tree<T> addSibling( Tree<T> sibling ){
        return parent.addChild( sibling );
    }

    /**
     * Iterates tree in pre-order (http://en.wikipedia.org/wiki/Tree_traversal)
     */
    public Tree<T> findPreOrderNext(){
        if( hasChildren() ){
            return getFirstChild();
        }
        if( hasNext() ){
            return getNext();
        }
        Tree<T> parent = getParent();
        while( parent != null ){
            if( parent.hasNext() ){
                return parent.getNext();
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        appendAsText( sb, "" );
        return sb.toString();
    }

    private void appendAsText( StringBuilder sb, String identation ){
        if( parent == null ){
            sb.append( "<root>\n" );
        }
        else{
            sb.append( identation + content + "\n" );
        }
        identation = " " + identation;
        for( Tree<T> child : children ){
            child.appendAsText( sb, identation );
        }
    }
}
