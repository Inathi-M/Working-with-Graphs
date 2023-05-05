import java.io.*;

import java.util.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.StringTokenizer;

import Graphs.Graph;
//import Graphs.GraphException;

//import Graphs.Edge;
//import Graphs.GraphException;
//import Graphs.Path;
//import Graphs.Vertex;

// Used to signal violations of preconditions for
// various shortest path algorithms.
class GraphException extends RuntimeException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GraphException( String name )
    {
        super( name );
    }
}

// Represents an edge in the graph.
class Edge
{
    public Vertex     dest;   // Second vertex in Edge
    public double     cost;   // Edge cost
    
    public Edge( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
}

// Represents an entry in the priority queue for Dijkstra's algorithm.
class Path implements Comparable<Path>
{
    public Vertex     dest;   // w
    public double     cost;   // d(w)
    
    public Path( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
    
    public int compareTo( Path rhs )
    {
        double otherCost = rhs.cost;
        
        return cost < otherCost ? -1 : cost > otherCost ? 1 : 0;
    }
}

// Represents a vertex in the graph.
class Vertex
{
    public String     name;   // Vertex name
    public List<Edge> adj;    // Adjacent vertices
    public double     dist;   // Cost
    public Vertex     prev;   // Previous vertex on shortest path
    public int        scratch;// Extra variable used in algorithm

    public Vertex( String nm )
      { name = nm; adj = new LinkedList<Edge>( ); reset( ); }

    public void reset( )
    //  { dist = Graph.INFINITY; prev = null; pos = null; scratch = 0; }    
    { dist = SimulatorOne.INFINITY; prev = null; scratch = 0; }
      
   // public PairingHeap.Position<Path> pos;  // Used for dijkstra2 (Chapter 23)
}

// Graph class: evaluate shortest paths.
//
// CONSTRUCTION: with no parameters.
//
// ******************PUBLIC OPERATIONS**********************
// void addEdge( String v, String w, double cvw )
//                              --> Add additional edge
// void printPath( String w )   --> Print path after alg is run
// void unweighted( String s )  --> Single-source unweighted
// void dijkstra( String s )    --> Single-source weighted
// void negative( String s )    --> Single-source negative weighted
// void acyclic( String s )     --> Single-source acyclic
// ******************ERRORS*********************************
// Some error checking is performed to make sure graph is ok,
// and to make sure graph satisfies properties needed by each
// algorithm.  Exceptions are thrown if errors are detected.

public class SimulatorOne
{
    public static final double INFINITY = Double.MAX_VALUE;
    private Map<String,Vertex> vertexMap = new HashMap<String,Vertex>( );

    /**
     * Add a new edge to the graph.
     */
    public void addEdge( String sourceName, String destName, double cost )
    {
        Vertex v = getVertex( sourceName );
        Vertex w = getVertex( destName );
        v.adj.add( new Edge( w, cost ) );
    }

    /**
     * Driver routine to handle unreachables and print total cost.
     * It calls recursive routine to print shortest path to
     * destNode after a shortest path algorithm has run.
     */
    public void printPath( String destName )
    {
        Vertex w = vertexMap.get( destName );
        if( w == null )
            throw new NoSuchElementException( "Destination vertex not found" );
        else if( w.dist == INFINITY )
            System.out.println( destName + " is unreachable" );
        else
        {
            System.out.print( "(Cost is: " + w.dist + ") " );
            printPath( w );
            System.out.println( );
        }
    }

    /**
     * If vertexName is not present, add it to vertexMap.
     * In either case, return the Vertex.
     */
    private Vertex getVertex( String vertexName )
    {
        Vertex v = vertexMap.get( vertexName );
        if( v == null )
        {
            v = new Vertex( vertexName );
            vertexMap.put( vertexName, v );
        }
        return v;
    }

    /**
     * Recursive routine to print shortest path to dest
     * after running shortest path algorithm. The path
     * is known to exist.
     */
    private void printPath( Vertex dest )
    {
        if( dest.prev != null )
        {
            printPath( dest.prev );
            System.out.print( " to " );
        }
        System.out.print( dest.name );
    }
    
    /**
     * Initializes the vertex output info prior to running
     * any shortest path algorithm.
     */
    private void clearAll( )
    {
        for( Vertex v : vertexMap.values( ) )
            v.reset( );
    }
    
    /**
     * Single-source weighted shortest-path algorithm. (Dijkstra) 
     * using priority queues based on the binary heap
     */
    public void dijkstra( String startName )
    {
        PriorityQueue<Path> pq = new PriorityQueue<Path>( );

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( );
        pq.add( new Path( start, 0 ) ); 
        start.dist = 0;
        
        int nodesSeen = 0;
        while( !pq.isEmpty( ) && nodesSeen < vertexMap.size( ) )
        {
            Path vrec = pq.remove( );
            Vertex v = vrec.dest;
            if( v.scratch != 0 )  // already processed v
                continue;
                
            v.scratch = 1;
            nodesSeen++;

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( cvw < 0 )
                    throw new GraphException( "Graph has negative edges" );
                    
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist +cvw;
                    w.prev = v;
                    pq.add( new Path( w, w.dist ) );
                }
            }
        }
    }
    
    public static boolean processRequest( Scanner in, Graph g )
    {
        try
        {
            System.out.print( "Enter start node:" );
            String startName = in.nextLine( );

            System.out.print( "Enter destination node:" );
            String destName = in.nextLine( );

            System.out.print( "Enter algorithm (u, d, n, a ): " );
            String alg = in.nextLine( );
            
            if( alg.equals( "u" ) )
                g.unweighted( startName );
            else if( alg.equals( "d" ) )    
            {
                g.dijkstra( startName );
                g.printPath( destName );
            }
            else if( alg.equals( "n" ) )
                g.negative( startName );
            else if( alg.equals( "a" ) )
                g.acyclic( startName );
                    
            g.printPath( destName );
        }
        catch( NoSuchElementException e )
          { return false; }
        catch( GraphException e )
          { System.err.println( e ); }
        return true;
    }
    
 /**   public static void processRequest1( String start, String dest, Graph g )
    {
        try
        {
            //String startName = in.nextLine( );
            //String destName = in.nextLine( );
            //String alg = in.nextLine( );
            
            g.dijkstra( start );
            //g.printPath( dest );
            //g.printPath( destName );
        }
        catch( NoSuchElementException e )
          { System.err.println( e ); }
        catch( GraphException e )
          { System.err.println( e ); }
    }

	public static void pathN(String start, String dest, Graph g){
			String out;
			String out1;
			String total;

			g.dijkstra(dest );
			//g.printPath( start );
            		out1 = g.printPath1(start);

			g.dijkstra( start );
            		//g.printPath( dest );
			out = g.printPath1(dest);
			
			total = out1+out;
			int length = total.length()/2;
			
			
			System.out.println(total.substring(1,length));
			resetOut();
			g.clearCost();
		}
	 **/
	public static double calculateCost(String start, String dest, Graph g){
			
			// Variables used in calculating cost
			double cost1;
			double cost2;
			double totalCost;
			
			g.dijkstra(dest );
			cost2 = g.getCost();
			g.clearCost();

			g.dijkstra( start );
			String u = g.printPath1(dest);
			cost1 = g.getCost();
			g.clearCost();

			g.resetOut();
			
			return cost1+cost2;
			
		} 
    

	
public static void main(String[] args) throws FileNotFoundException{
	
	Graph g = new Graph();
	
	
	double checkCost = 0.0;
	double currentCost = 0.0;
	//Scanner keyboard = new Scanner(System.in);
	//String name = keyboard.nextLine();
			
	List<String> LNodes = new ArrayList<>();
	List<String> LshopNodeno = new ArrayList<>();
	List<String> LclientNodeno = new ArrayList<>();
	File textfile = new File("Glorifiedtextfile.txt") ;
	ArrayList<Integer> ar1 = new ArrayList<Integer>();
	
	
	Scanner inputfile = new Scanner(textfile);
	String temp_Node;
	int noOfValues;
	int valueLength;
	
	String noOfNodes = inputfile.nextLine();
	int intNodes = Integer.parseInt(noOfNodes); 
	
	
	//int count = 0;
	while(inputfile.hasNextLine()  ) {
		
		
		String lines = inputfile.nextLine();

		StringTokenizer st = new StringTokenizer(lines);
		
	     noOfValues = st.countTokens(); 
	     valueLength = (noOfValues -1)/2;
	    
	    if(noOfValues >= 3 ) {
	        temp_Node = st.nextToken();
	    	for(int a = 0;a < valueLength;a++ ) {
		         String source_node = temp_Node;
		         String dest_node = st.nextToken();
	           	 int weight = Integer.parseInt(st.nextToken());
	           	 g.addEdge(source_node, dest_node, weight);
	    	}
	     //	System.out.println(lines);
		// count++;
	    }else{
	   // count=1;
	    StringTokenizer st1 = new StringTokenizer(lines);
	    int noOfShops = Integer.parseInt(st1.nextToken());
	    String[] shopNodeno = inputfile.nextLine().split(" ");
	    LshopNodeno.addAll(Arrays.asList(shopNodeno));
	
	
	    int noOfClients = Integer.parseInt(inputfile.nextLine());
	    String[] clientNodeno = inputfile.nextLine().split(" ");
	    LclientNodeno.addAll(Arrays.asList(clientNodeno));
	
	    //For looping for displaying output
	    for (int j = 0; j< noOfClients;j++){

             double cost1 = Graph.INFINITY;
             String taxi = "";
             System.out.println("Client "+clientNodeno[j]);
		      //Getting the nearest taxi for client 
            try{
	    	  for(int k = 0; k<noOfShops;k++){

                g.dijkstra(shopNodeno[k]);//Start at shop
                double c = g.getCost(clientNodeno[j]);

                if( c < cost1){
                    cost1 = c;
                    taxi = shopNodeno[k];
                }else if (c == cost1){
                    taxi+= " " + shopNodeno[k];
                }

		      }

              String[] taxis = taxi.split(" ");
              for(String q:taxis){
                  System.out.println("taxi " + q);
                  g.dijkstra(q);
                  g.printPath(clientNodeno[j]);
              }
            }catch(Exception e) {System.out.println("cannot be helped");
                   break;
            }

            double cost2 = Graph.INFINITY;
            String shop = "";
            int checker = 0;

            for(int b =0; b < noOfShops;b++){
                g.dijkstra(clientNodeno[j]); // Start at Client

                double z = g.getCost(shopNodeno[b]);
                if (z < cost2){
                    cost2 = z;
                    shop = shopNodeno[b];}
                 else if(z == cost2) {
                    shop +=" "+shopNodeno[b];
                 }else if (z == cost2)
                 checker++;
           }
           String[] shops = shop.split(" ");
           for (String y: shops) {
               
              System.out.println("shop " + y);
                   
              g.dijkstra(clientNodeno[j]);
              g.printShops(y);
           }

                 
        }
	  }
    } 
  }
}

