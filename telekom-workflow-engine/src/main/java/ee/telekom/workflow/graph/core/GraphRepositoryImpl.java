package ee.telekom.workflow.graph.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.GraphValidator;
import ee.telekom.workflow.graph.WorkflowException;

public class GraphRepositoryImpl implements GraphRepository{

    private Map<String, Set<Graph>> repo = new ConcurrentHashMap<>();
    private GraphValidator validator = new GraphValidatorImpl();

    @Override
    public void addGraph( final Graph graph ){
        List<String> errors = validator.validate( graph );
        if( !errors.isEmpty() ){
            throw new WorkflowException( "Cannot add graph with errors: " + errors );
        }
        Set<Graph> versions = repo.get( graph.getName() );
        if( versions == null ){
            versions = new TreeSet<>( GraphVersionComparator.INSTANCE );
            repo.put( graph.getName(), versions );
        }
        versions.add( graph );
    }

    @Override
    public Graph getGraph( String name, Integer version ){
        if( version == null ){
            Set<Graph> versions = repo.get( name );
            return (versions == null || versions.isEmpty()) ? null : versions.iterator().next();
        }
        Set<Graph> versions = repo.get( name );
        if( versions == null || versions.isEmpty() ){
            return null;
        }
        for( Graph graphVersion : versions ){
            if( graphVersion.getVersion() == version ){
                return graphVersion;
            }
        }
        return null;
    }

    @Override
    public Set<Graph> getGraphs( final String name ){
        Set<Graph> versions = repo.get( name );
        if( versions == null ){
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet( versions );
    }

    @Override
    public Set<Graph> getGraphs(){
        final Set<Graph> result = new HashSet<>();
        for( Set<Graph> versions : repo.values() ){
            for( Graph graph : versions ){
                result.add( graph );
            }
        }
        return Collections.unmodifiableSet( result );
    }

    private static class GraphVersionComparator implements Comparator<Graph>{

        private static final GraphVersionComparator INSTANCE = new GraphVersionComparator();

        @Override
        public int compare( Graph o1, Graph o2 ){
            int v1 = o1.getVersion();
            int v2 = o2.getVersion();
            return v1 == v2 ? 0 : (v1 < v2 ? 1 : -1);
        }

    }

}