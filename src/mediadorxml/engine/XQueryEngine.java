package mediadorxml.engine;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import wrapper.sedna.XQueryResult;

import mediadorxml.algebra.util.IdGenerator;
import mediadorxml.engine.flworprocessor.FLWOR;
import mediadorxml.exceptions.AlgebraParserException;
import mediadorxml.exceptions.FragmentReductionException;
import mediadorxml.exceptions.OptimizerException;
import mediadorxml.javaccparser.SimpleNode;
import mediadorxml.javaccparser.XQueryParser;

public class XQueryEngine {
	
	protected transient StringBuilder xqueryResultStr;
	protected transient ArrayList<FLWOR> flworList;
	private XQueryResult xqResult;
	
	public XQueryEngine(){
		this.flworList = new ArrayList<FLWOR>();
	}
	
	public void execute(String xquery) {
		this.execute(xquery, false);
	}
	
	public void execute(String xquery, boolean debug) {
		//System.out.println(xquery);
		IdGenerator.reset();
		
		this.xqueryResultStr = new StringBuilder();
		//this.xqResult = new XQueryResult();
		
		try{
			
			long startParseTime = System.nanoTime();
						
			// Parser da XQuery			
			XQueryParser xqParsed = new XQueryParser(new StringReader(xquery));

			SimpleNode node = xqParsed.Start();

			long parseTime = (System.nanoTime() - startParseTime)/1000000;
			System.out.println ("Tempo total para o parser da consulta " + parseTime + " ms");
			
			// Processamento e execução da XQuery
			this.processSimpleNode(node, debug);
	
//			xqResult.setSuccess(true);
//			xqResult.setTimeMsCompile(parseTime);
//			for (int i=0; i<flworList.size(); i++){
//				FLWOR flwor = flworList.get(i);
//				xqResult.setSuccess(xqResult.isSuccess() & flwor.getXqResult().isSuccess());
//				xqResult.setTimeMsCompile(xqResult.getTimeMsCompile() + flwor.getXqResult().getTimeMsCompile());
//				System.out.println("valores locais"+xqResult.getTimeMsLocal()+"/"+flwor.getXqResult().getTimeMsLocal());
//				
//				xqResult.setTimeMsLocal(xqResult.getTimeMsLocal() + flwor.getXqResult().getTimeMsLocal());
//				xqResult.setTimeMsCommunicRemote(xqResult.getTimeMsCommunicRemote() + flwor.getXqResult().getTimeMsCommunicRemote());
//				xqResult.setTimeMsRemote(xqResult.getTimeMsRemote() + flwor.getXqResult().getTimeMsRemote());
//				xqResult.setTotalBytes(xqResult.getTotalBytes() + flwor.getXqResult().getTotalBytes());
//				xqResult.setNumberQueriesExecuted(xqResult.getNumberQueriesExecuted() + flwor.getXqResult().getNumberQueriesExecuted());
//							
//			}
//			xqResult.setResult(this.xqueryResultStr.toString());
//			System.out.println("xqResult: " + xqResult.getResult());
		}
		catch(Exception exc){
			exc.printStackTrace();
//			xqResult.setSuccess(false);
//			xqResult.setResult(exc.getMessage());	
		}
	}
	
	protected void processSimpleNode(final SimpleNode node, final boolean debug) 
		throws OptimizerException, FragmentReductionException, AlgebraParserException, IOException{
		
		if (debug)
			System.out.println(node.toString() + " - " + node.getText() + "\r\n");
		//
		boolean processChild = true;
		
		String element = node.toString();

		if (element == "ElmtConstructor"){
			this.xqueryResultStr.append("<");
		}
		else if (element == "QName"){
			this.xqueryResultStr.append(node.getText());
			this.xqueryResultStr.append(">\r\n");
		}
		else if (element == "ElmtContent"){
			//
		}
		else if (element == "EndTag"){
			this.xqueryResultStr.append("</");
		}
		else if (element == "FLWORExpr"){
			
			// Compilação e execução do FLWOR
			FLWOR flwor = new FLWOR();
			
			// Compilar o FLWOR
			flwor.compile(node, debug);
						
			this.flworList.add(flwor);
			
			if (debug)
				System.out.println(flwor.toString());
			
			processChild = false;
		}
			
		if (processChild & (node.jjtGetNumChildren()>0)){
			for (int i=0; i<node.jjtGetNumChildren(); i++) {
				this.processSimpleNode((SimpleNode)node.jjtGetChild(i), debug);
			}
		}
		
	}
	
	public ArrayList getFlworList(){
		return this.flworList;
	}
	
}
