/*
 * Created on 08/04/2005
 */
package globalqueryprocessor.clusterqueryprocessor.queryplanner;

import java.sql.SQLException;
import java.util.ArrayList;

import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;
import commons.PartiXVPDatabaseMetaData;
import commons.Q;
import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.RangeStatistics;


/**
 * @author Bernardo
 */
public class QueryPlanner {
	// Marcelo, troquei os parâmetros do parser. Estou passando o metadata todo.
	// Seguindo
	// o padrão do JDBC, não é possível fazer uma busca por informações de
	// colunas sem
	// antes informar qual é a tabela que contem essas colunas.
	public static QueryInfo parser(String sql,
			PartiXVPDatabaseMetaData meta, int numNQPs) throws SQLException {

		/*
		 * Tomar aqui a decisao entre Intra e Inter usando informacoes de quais
		 * tabelas sao particionaveis
		 */
		sql = sql.trim();

		QueryInfo qi = new QueryInfo();
		qi.setSql(sql);


		if (qi.getSql().toUpperCase().startsWith("SELECT")) {
			if (qi.getSql().toUpperCase().startsWith("SELECT INTRA"))
				qi.setQueryType(QueryInfo.INTRA_QUERY);
			else {
				ArrayList<Range> rangeList = meta.getRangeList();
				
				// Put ";" if it is needed
				if (!sql.endsWith(";"))
					sql += ";";

				Q q = null;
		
				q = new Q(sql, rangeList, meta);
				
				if (q.isPartitionable()) {
					qi.setQueryType(QueryInfo.INTRA_QUERY);
					ArrayList<String> tableList = q.getPartitionableTables();

					String choosenTable = null;
					long max = -1;
					for (int i = 0; i < tableList.size(); i++) {
						long value = getCardinality(tableList.get(i), rangeList);
						if (value > max) {
							max = value;
							choosenTable = tableList.get(i);
						}
					}

					
						q.setVpQuery(choosenTable);
					

					String vpQuery = null;
					vpQuery = q.getVpQuery();
					// Remove ";"
					vpQuery = vpQuery.substring(0, vpQuery.length() - 1);
           
					boolean performLoadBalancing = true;
					boolean getSystemResourceStatistics = false;

					QueryAvpDetail queryAvpDetail = new QueryAvpDetail();
					queryAvpDetail.setQuery(vpQuery);					

					Range pargresRange = getRange(choosenTable, rangeList);

					
					int[] argsFirstVal = new int[q.getQvpCount()];
					for(int i = 0, j = 1; i < argsFirstVal.length; i++, ++j)
						argsFirstVal[i] = i + j;
					int[] argsLastVal = new int[q.getQvpCount()];
					for(int i = 0, j = 2; i < argsLastVal.length; i++, ++j)
						argsLastVal[i] = i + j;					
                    
                    // TODO:REVER ISSO!
					RangeStatistics statistics = new RangeStatistics(1, 
							pargresRange.getRangeEnd()
									- pargresRange.getRangeInit() + 1,
							(float) (1.0 / numNQPs));
                    
                    //TODO Rever isso
					localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range range = new localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range(-1, 
							(int) pargresRange.getRangeInit(),
							(int) pargresRange.getRangeEnd() + 1, argsFirstVal,
							argsLastVal, numNQPs, statistics);
					
                    queryAvpDetail.setRange(range);
					queryAvpDetail.setNumNQPs(numNQPs);
					queryAvpDetail.setPartitionSizes(null); // TODO: REVERRRR
															// ISSO!
					queryAvpDetail.setGetStatistics(false);
					queryAvpDetail.setLocalResultComposition(true);
					
					queryAvpDetail.setPerformDynamicLoadBalancing(performLoadBalancing);
					queryAvpDetail.setGetSystemResourceStatistics(getSystemResourceStatistics);

					qi.setQueryAvpDetail(queryAvpDetail);
					qi.setVpColumns(q.getQvpColumnsList());
					qi.setNumVpAggregationColumns(q.getSelectAggregationFunctionCount());
					qi.setGroupByTextList(q.getGroupByTextList());
                    qi.setSelectTextList(q.getSelectTextList());
                    qi.setOrderByTextList(q.getOrderByTextList());
                    qi.setAliasTextList(q.getAliasTextList());
                    qi.setLimitText(q.getLimitText());
                    qi.setVpAttribute(q.getVpAttribute());
                    qi.setVpTable(q.getVpTable());
                    qi.setOrderByRangeList(meta.getOrderByRangeList());
                    if (q.getGroupByTextList().size() == 0)
                        qi.setDistributedComposition(false);
                    else
                        qi.setDistributedComposition(true);
                    
                /*    if (q.getOrderByTextList().size() == 0)
                        qi.setDistributedSort(false);
                    else
                        qi.setDistributedSort(true);*/
				} else
					qi.setQueryType(QueryInfo.INTER_QUERY);
			}
		} else {
			qi.setQueryType(QueryInfo.UPDATE_QUERY);
		}
		return qi;
	}

	public static Range getRange(String tableName, ArrayList<Range> rangeList) {
		for (int i = 0; i < rangeList.size(); i++) {
			if (rangeList.get(i).getTableName().toUpperCase().equals(
					tableName.toUpperCase()))
				return rangeList.get(i);
		}
		return null;
	}

	public static long getCardinality(String tableName, ArrayList<Range> rangeList) {
		Range range = getRange(tableName, rangeList);
		return range.getCardinality();
	}
}
