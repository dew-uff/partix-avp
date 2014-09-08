package commons;
import java.util.ArrayList;

import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;

/*
 * This class represents parser information about a query  
 */

public class Q {
	
	private String query;
	private String error;
	private String fromInnerQuery;
	private String fromOuterQueryText;
	private String fromSubqueryAlias;
    private String vpTable;
    private String vpAttribute;
	private ArrayList<String> fromSelectAliasTextList;
	private boolean isFromClauseInnerSelect;
	private ArrayList<Range> rangeList;
	private Parser parser;
	private ParserIni parserIni;
	private boolean isPartitionable;
	private ArrayList<String> returnList = new ArrayList<String>(0);
	private ArrayList<String> returnAliasTextList;
	private boolean setVpQueryWasCalled = false;
	private PartiXVPDatabaseMetaData meta;
	
	public Q(String query, ArrayList<Range> rangeList, PartiXVPDatabaseMetaData meta) {
		/* 
		 * parser
		 * 
		 * preencher lista de colunas presentes na consulta
		 * 
		 * listar de colunas do group by
		 * 
		 * compositores
		 */	
		this.meta = meta;
		this.query = query;
		this.rangeList = rangeList;
		//parser = new Parser(query,meta);
		parserIni = new ParserIni(query,meta);		
		error = parserIni.getErrorIni();
		if( !(error.equals("")) )
				
		this.isFromClauseInnerSelect = parserIni.isFromClauseInnerSelect();
		if(this.isFromClauseInnerSelect) {
			fromSelectAliasTextList = parserIni.getFromSelectAliasTextList();
			fromOuterQueryText = parserIni.getFromOuterQueryText();
			fromSubqueryAlias = parserIni.getFromSubqueryAlias();
			fromInnerQuery = parserIni.getFromSubqueryText();
			parserIni = new ParserIni(fromInnerQuery,meta);
			error = parserIni.getErrorIni();
			if(!(error.equals(""))) {
				System.out.println("ParserSilentException");
			}
				
		}
		processLists();
	}

	/*
	 * indica se query pode ser particionada virtualmente
	 */
	public boolean isPartitionable(){
		return this.isPartitionable;
	}	
	
	/* 
	 * fornece lista de tabelas de query candidatas a fragmentacao
	 */
	public ArrayList<String> getPartitionableTables(){
		return returnList;
	}
		
	public void processLists() {	  	  	
	  	for(int i=0; i<parserIni.getTableList().size();i++){
	  		for(int j=0;j<rangeList.size();j++){
	  			if( (parserIni.getTableList().get(i)).toUpperCase().equals((rangeList.get(j)).getTableName().toUpperCase()) )
	  				returnList.add(parserIni.getTableList().get(i).toUpperCase());
	  		}
	  	}
		if(returnList.size()!=0) 
			this.isPartitionable=true;
		else
			this.isPartitionable=false;
	  }
	
	public ArrayList<String> getAttributesQuery() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getAttributesQuery()\"."));
		return parser.getWhereAttList();
	}
	
	/*
	 * fornece versao vp de acordo com a tabela escolhida para fragmentacao
	 */
	public void setVpQuery(String table) {		
		setVpQueryWasCalled = true;
        vpTable = table;
        vpAttribute = null;
		for(int i = 0; i < rangeList.size(); i++) {
			if(rangeList.get(i).getTableName().toUpperCase().equals(table.toUpperCase())) {
				vpAttribute = rangeList.get(i).getField();
				break;
			}
		}		
		if(returnList.contains(table.toUpperCase())){
			if(isFromClauseInnerSelect)
				parser = new Parser(fromInnerQuery,meta,parserIni,rangeList,table,vpAttribute);
			else
				parser = new Parser(this.query,this.meta,parserIni,rangeList,table,vpAttribute);
			
			error = parser.getError();
			if( !(error.equals("")) )
					System.out.println((error));			
		}
		else System.out.println(("A tabela "+table+" não é candidata a fragmentação para esta consulta."));		
	}
	
	public String getVpQuery() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getVpQuery()\"."));
		return parser.getVpQuery();
	}
		
	public Object[] getVpGroupByList() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getVpGroupByList()\"."));		
		return parser.getGroupByList();
    }
	
	public ArrayList<Column> getQvpColumnsList() {
	   	if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getQvpColumnsList()\"."));
		return parser.getQvpColumnsList();
    }
	
	public int getSelectAggregationFunctionCount() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getSelectAggregationFunctionCount()\"."));
		return parser.getSelectAggregationFunctionCount();
	}
	
	public Object[][] getSelectCompositor() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getSelectCompositor()\"."));
	    return parser.getSelectCompositor();
	}
	
	public ArrayList<String> getAliasTextList() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getAliasCompositor()\"."));
	    if(isFromClauseInnerSelect){
			returnAliasTextList = parser.getAliasTextList();
			for(int i=0; i<fromSelectAliasTextList.size();i++){
				returnAliasTextList.set(i, fromSelectAliasTextList.get(i));
			}
			return returnAliasTextList;
	    }
	    else
			return parser.getAliasTextList();
    }
	
	public Object[] getHavingCompositor() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getHavingCompositor()\"."));
        return parser.getHavingCompositor();
    }	
	public OrderByRef[] getOrderByIndexList() {
		if(!setVpQueryWasCalled)
				System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getOrderByIndexList()\"."));
	     return parser.getOrderByIndexList();
	}
	
    public ArrayList<String> getGroupByTextList() {
		   	if(!setVpQueryWasCalled)
				System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getGroupByTextList()\"."));
			return parser.getGroupByTextList();
	}

    public ArrayList<String> getOrderByTextList() {
	   	if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getOrderByTextList()\"."));
		return parser.getOrderByTextList();
    }	
	
	public ArrayList<String> getSelectTextList() {
		   	if(!setVpQueryWasCalled)
				System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getSelectTextList()\"."));
			return parser.getSelectTextList();
	 }
	
	public int getQvpCount() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getQvpCount()\"."));
		return parser.getQvpCount();
	}	
	
	public String getLimitText() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getLimitText()\"."));
		return parser.getLimitText();
	}	

	public String getAllOrDistinctText() {
		if(!setVpQueryWasCalled)
			System.out.println(("O método \"Q.setVpQuery(String table)\" deve ser executado antes de \"Q.getAllOrDistinctText()\"."));
		return parser.getAllOrDistinctText();
	}
	
	public String getFromOuterQueryText(){
		return fromOuterQueryText;
	}
	
	public String getFromSubqueryAlias(){
		return fromSubqueryAlias;
	}
	
	public boolean isFromClauseInnerSelect() {
		return isFromClauseInnerSelect;
	}
	public ArrayList<Range> getRangeList(){
		return this.rangeList;
	}
    public String getVpTable() {
        return vpTable;
    }
    public String getVpAttribute() {
        return vpAttribute;
    }
}
