package ee.telekom.workflow.facade.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that helps to parse a workflow instance history into a list
 * of human readable steps.
 * <p>
 * The raw workflow instance history is a compact format that enlists a series
 * of execution events separated by a "|". Please refer to {@link Event} to understand 
 * the syntax and meaning of individual events.
 * <p>
 * For instance, this class parses the following series of events <br> 
 * "start|1:1!|1:1=>2|1:2!|2:2=>3|3:2=>4|2:3!|2:3=>-2|2:-2!|1:-2=>6|1:6!|executed|"<br>
 * into <br>
 * "start; token 1 executed node(s) 1, 2; token 2 moved from 2 to 3; token 3 moved from 2 to 4; token 2 executed node(s) 3, -2; token 1 moved from -2 to 6; token 1 executed node(s) 6; execution completed"
 *
 * @author Christian Klock
 */
public class HistoryUtil{

    public static List<String> getExecutionSteps( String history ){
        if( history == null ){
            return Collections.emptyList();
        }
        List<String> result = new LinkedList<>();
        for( List<Event> step : groupByStep( parseHistory( history ) ) ){
            result.add( asText( step ) );
        }
        return result;
    }

    private static String asText( List<Event> step ){
        StringBuilder result = new StringBuilder();
        for( int i = 0; i < step.size(); i++ ){
            Event event = step.get( i );
            Event next1 = (i + 1 < step.size()) ? step.get( i + 1 ) : null;
            Event next2 = (i + 2 < step.size()) ? step.get( i + 2 ) : null;
            if( event.isStart() ){
                result.append( "start" );
            }
            else if( event.isContinue() ){
                result.append( "complete on token " + event.getToken() );
            }
            else if( event.isAbort() ){
                result.append( "abort" );
            }
            else if( event.isWaitState() ){
                result.append( "; reached waitstate" );
            }
            else if( event.isExecuted() ){
                result.append( "; execution completed" );
            }
            else if( event.isAborted() ){
                result.append( "; execution aborted" );
            }
            else if( event.isExecute() ){
                result.append( "; token " + event.getToken() + " executed node(s) " + event.getNode1() );
                while( next1 != null && next2 != null
                        && next1.isMove() && next2.isExecute()
                        && event.getToken() == next1.getToken() && next1.getToken() == next2.getToken()
                        && event.getNode1() == next1.getNode1() && next1.getNode2() == next2.getNode1() ){
                    result.append( ", " + next2.getNode1() );
                    i += 2;
                    event = step.get( i );
                    next1 = (i + 1 < step.size()) ? step.get( i + 1 ) : null;
                    next2 = (i + 2 < step.size()) ? step.get( i + 2 ) : null;
                }
            }
            else if( event.isMove() ){
                result.append( "; token " + event.getToken() + " moved from " + event.getNode1() + " to " + event.getNode2() );
            }
        }
        return result.toString();
    }

    private static List<List<Event>> groupByStep( List<Event> events ){
        List<List<Event>> result = new LinkedList<>();
        for( Event event : events ){
            if( event.isStart() || event.isContinue() || event.isAbort() ){
                result.add( new LinkedList<Event>() );
            }
            result.get( result.size() - 1 ).add( event );
        }
        return result;
    }

    private static List<Event> parseHistory( String history ){
        List<Event> result = new LinkedList<>();
        for( String event : history.split( "\\|" ) ){
            result.add( Event.parse( event ) );
        }
        return result;
    }

    private static class Event{
        // The workflow instance's execution starts.
        private static final Pattern PATTERN_START = Pattern.compile( "start" );
        // The workflow instance continues the execution after having reached a wait state
        private static final Pattern PATTERN_COMPLETE = Pattern.compile( "continue:(\\d+)" );
        // The workflow instance is to be aborted
        private static final Pattern PATTERN_ABORT = Pattern.compile( "abort" );
        // The workflow instance execution reached a wait state.
        private static final Pattern PATTERN_WAITSTATE = Pattern.compile( "waitstate" );
        // The workflow instance is executed (completed)
        private static final Pattern PATTERN_EXECUTED = Pattern.compile( "executed" );
        // The workflow instance is aborted
        private static final Pattern PATTERN_ABORTED = Pattern.compile( "aborted" );
        // A token executes a node.
        private static final Pattern PATTERN_EXECUTION = Pattern.compile( "(\\d+):([-]?\\d+)!" );
        // A token moves forward along a transition.
        private static final Pattern PATTERN_MOVE = Pattern.compile( "(\\d+):([-]?\\d+)=>([-]?\\d+)" );

        enum Type{
            START,
            COMPLETE,
            ABORT,
            WAITSTATE,
            EXECUTED,
            ABORTED,
            EXECUTE,
            MOVE
        }

        private Type type;
        private Integer token;
        private Integer node1;
        private Integer node2;

        private Event( Type type ){
            this( type, null );
        }

        private Event( Type type, String tokenId ){
            this( type, tokenId, null, null );
        }

        private Event( Type type, String token, String node1, String node2 ){
            this.type = type;
            if( token != null ){
                this.token = Integer.valueOf( token );
            }
            if( node1 != null ){
                this.node1 = Integer.valueOf( node1 );
            }
            if( node2 != null ){
                this.node2 = Integer.valueOf( node2 );
            }
        }

        public static Event parse( String event ){
            Matcher m;
            m = PATTERN_START.matcher( event );
            if( m.matches() ){
                return new Event( Type.START, null );
            }
            m = PATTERN_COMPLETE.matcher( event );
            if( m.matches() ){
                return new Event( Type.COMPLETE, m.group( 1 ) );
            }
            m = PATTERN_ABORT.matcher( event );
            if( m.matches() ){
                return new Event( Type.ABORT, null );
            }
            m = PATTERN_WAITSTATE.matcher( event );
            if( m.matches() ){
                return new Event( Type.WAITSTATE, null );
            }
            m = PATTERN_EXECUTED.matcher( event );
            if( m.matches() ){
                return new Event( Type.EXECUTED, null );
            }
            m = PATTERN_ABORTED.matcher( event );
            if( m.matches() ){
                return new Event( Type.ABORTED, null );
            }
            m = PATTERN_EXECUTION.matcher( event );
            if( m.matches() ){
                return new Event( Type.EXECUTE, m.group( 1 ), m.group( 2 ), null );
            }
            m = PATTERN_MOVE.matcher( event );
            if( m.matches() ){
                return new Event( Type.MOVE, m.group( 1 ), m.group( 2 ), m.group( 3 ) );
            }
            throw new IllegalArgumentException( "Unknown event syntax: " + event );
        }

        public Integer getToken(){
            return token;
        }

        public Integer getNode1(){
            return node1;
        }

        public Integer getNode2(){
            return node2;
        }

        public boolean isStart(){
            return Type.START.equals( type );
        }

        public boolean isContinue(){
            return Type.COMPLETE.equals( type );
        }

        public boolean isAbort(){
            return Type.ABORT.equals( type );
        }

        public boolean isWaitState(){
            return Type.WAITSTATE.equals( type );
        }

        public boolean isExecuted(){
            return Type.EXECUTED.equals( type );
        }

        public boolean isAborted(){
            return Type.ABORTED.equals( type );
        }

        public boolean isExecute(){
            return Type.EXECUTE.equals( type );
        }

        public boolean isMove(){
            return Type.MOVE.equals( type );
        }

    }

}
