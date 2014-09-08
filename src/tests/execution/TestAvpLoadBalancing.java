package tests.execution;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;

import commons.Logger;
import console.ResultSetPrinter;
import globalqueryprocessor.clusterqueryprocessor.ClusterQueryProcessor;
import util.MyRMIRegistry;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.RangeStatistics;

public class TestAvpLoadBalancing {
	private static Logger logger = Logger.getLogger(TestAvpLoadBalancing.class);

	String cqpAddress;
	int cqpPort;
	int tpchScaleFactor;
	int numVirtualPartitions;
	int numNQPs;
	int numExecutions;
	int queryNum;
	int numQEsPerJob;    
	boolean hasSystemResourceStatistics;

	/** Creates a new instance of TestNode */
	public TestAvpLoadBalancing(String cqpAddress,int cqpPort,int numNQPs,
			int numVirtualPartitions,int numQEsPerJob,int queryNum,int numExecutions, boolean hasSystemResourceStatistics) {
		this.cqpAddress = cqpAddress;
		this.cqpPort = cqpPort;
		this.numNQPs = numNQPs;
		this.numVirtualPartitions = numVirtualPartitions;
		this.numQEsPerJob = numQEsPerJob;
		this.queryNum = queryNum;
		this.numExecutions = numExecutions;
		this.hasSystemResourceStatistics = hasSystemResourceStatistics;
		this.run();

	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if( args.length != 6 ) {
			System.out.println("usage: java TestAvpLoadBalancing node_address port numNQPs idQuery numberOfExecutions SYSMON|NO_SYSMON" );
			return;
		}

		String cqpAddress = args[0].trim();
		int cqpPort = Integer.parseInt( args[1] );
		int numNQPs = Integer.parseInt( args[2] );
		int numVirtualPartitions = numNQPs; // in traditional VP, each NQP works with only one partition
		int numQEsPerJob = 1; // Always equal to one. To have more than one, modify PartitionTuner
		int queryNum = Integer.parseInt( args[3] );
		int numExecutions = Integer.parseInt( args[4] );
		boolean getSystemResourceStatistics;

		if( args[5].equals( "SYSMON" ) )
			getSystemResourceStatistics = true;
		else if( args[5].equals( "NO_SYSMON" ) )
			getSystemResourceStatistics = false;
		else
			throw new IllegalArgumentException( "Invalid option: " + args[6] );

		try {
			//  TestAvpLoadBalancing t = 
			new TestAvpLoadBalancing(cqpAddress,cqpPort,numNQPs,numVirtualPartitions,numQEsPerJob,queryNum,numExecutions,getSystemResourceStatistics);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void run() {
		ClusterQueryProcessor cqp;
		ResultSet result = null;
		@SuppressWarnings("unused") String query;
		int countExec;        
		float totalExecutionTime = 0;        
		/*int numQueries = 3;
        int count;
        int[] resultValueTypeCodes = null;
        int resultGroupIdSize = 0;
		 */
		Range range;
		RangeStatistics rangeStat;
		int []argsFirstVal;
		int []argsLastVal;
		int rangeIni = 0, rangeEnd = 0;
		//boolean performLoadBalancing = true;
		long numBlocksRead, numBlocksWritten;



		try {
			long begin, end, firstExecutionTime = 0;
			int maxResultTuplesToShow = 10;
			boolean quotedDateInterval;
System.out.println("cqpAddress: " + cqpAddress);
System.out.println("cqpPort: " + cqpPort);
System.out.println("remoteAddress: " + "rmi://"+ cqpAddress + ":" + cqpPort + "/ClusterQueryProcessor");
//System.out.println(MyRMIRegistry.lookup(cqpAddress,cqpPort,"rmi://"+ cqpAddress + ":" + cqpPort + "/ConnectionCqp").getClass().getName());
			cqp = (ClusterQueryProcessor) MyRMIRegistry.lookup(cqpAddress,cqpPort,"rmi://"+ cqpAddress + ":" + cqpPort + "/ClusterQueryProcessor");
			System.out.println(cqp.getClusterSize());
			quotedDateInterval = cqp.quotedDateIntervals();

			switch( queryNum ) {

			case 1: // DBLP Query 1
				query = new String( "<results> { " +
						"for $c in doc('dblp')/dblp/inproceedings " +
						"where $c/year >1984 and $c/year <=2007 " +
						"return <inproceeding> " +
						"{$c/title} </inproceeding> " +
						"} </results>" );
				argsFirstVal = new int[1];
				argsFirstVal[0] = 1;
				argsLastVal = new int[1];
				argsLastVal[0] = 2;

				rangeIni = 1;
				rangeEnd = 1087589;

				rangeStat = new RangeStatistics( 50000,	1087588, (float)(1.0 / numNQPs) );
				//(float)tpchStat.maxSizeFractionForVPIndexScan() );
				range = new Range( -1, rangeIni, rangeEnd, argsFirstVal, argsLastVal, numVirtualPartitions, rangeStat );
				break;

			default: System.out.println("Invalid Query Id: " + queryNum );
			return;
			}

			String resultsFileName = Utilities.createResultFileName( queryNum, "AVP_LoadBalancing", numExecutions, numNQPs );
			PrintStream resultsFile = new PrintStream( new FileOutputStream( resultsFileName ) );

			System.out.println( "------------------------------------------------------------" );
			System.out.println( "Limits: " + range.getFirstValue() + " to " + range.getOriginalLastValue() );
			resultsFile.println( "------------------------------------------------------------" );
			resultsFile.println( "File: " + resultsFileName );
			resultsFile.println( "Limits: " + range.getFirstValue() + " to " + range.getOriginalLastValue() );
			for( countExec = 0; countExec < numExecutions; countExec++ ) {
				System.out.println( "------------------------------------------------------------" );
				System.out.println( "Execution Number: " + (countExec + 1) );
				resultsFile.println( "------------------------------------------------------------" );
				resultsFile.println( "Execution Number: " + (countExec + 1) );
				begin = System.currentTimeMillis();
				/*               result = cqp.executeQueryWithAVP( query, range, numNQPs, null, true,
                                                  true, 11, resultValueTypeCodes, resultGroupIdSize, performLoadBalancing,
						  hasSystemResourceStatistics, null );*/
				end = System.currentTimeMillis();
				//ResultSetPrinter.print(result, maxResultTuplesToShow);

				numBlocksRead = 0;
				numBlocksWritten = 0;
				if( hasSystemResourceStatistics ) {
					resultsFile.println("Total number of blocks read from disk = " + numBlocksRead );
					resultsFile.println("Total number of blocks written to disk = " + numBlocksWritten );
					System.out.println("Total number of blocks read from disk = " + numBlocksRead );
					System.out.println("Total number of blocks written to disk = " + numBlocksWritten );
				}
				else {
					resultsFile.println("IO has NOT been measured");
					System.out.println("IO has NOT been measured");
				}
				//resultsFile.println( "There were " + result.size() + " tuples on the result." );
				resultsFile.println( "Elapsed time = " + (end - begin) + " milisseconds." );
				if( countExec > 0 ) // Ignore first execution
					totalExecutionTime += (end - begin);
				else
					firstExecutionTime = end - begin;
				result = null;
				System.gc();
				System.out.println( "Finished execution number: " + (countExec + 1) );
				System.out.println( "------------------------------------------------------------" );
				resultsFile.println( "------------------------------------------------------------" );
			}
			System.out.println( "Finished!" );
			resultsFile.println( "First execution time: " + firstExecutionTime + " ms" );
			resultsFile.println( "Mean execution time (ignoring first execution): " + (totalExecutionTime / ((float)numExecutions-1)) + " ms");
			System.out.println( "First execution time: " + firstExecutionTime + " ms" );
			System.out.println( "Mean execution time (ignoring first execution): " + (totalExecutionTime / ((float)numExecutions-1)) + " ms");

			resultsFile.close();
		} catch (Exception e) {
			System.err.println("TestAVP_LoadBalancing exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

}

