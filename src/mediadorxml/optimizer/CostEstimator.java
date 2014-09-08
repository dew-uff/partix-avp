package mediadorxml.optimizer;

import java.io.IOException;

import mediadorxml.algebra.operators.AbstractOperator;
import mediadorxml.catalog.CatalogManager;
import mediadorxml.catalog.util.LocalView;
import mediadorxml.catalog.util.WrapperLocation;
import mediadorxml.config.Config;

public class CostEstimator {
	
	protected int IOweight;

	public CostEstimator() {
		this.IOweight = Config.getCostEstimatorIOweight();
	}
	
	/*
	 * Método simplificado de estimativa de custo de execução de um plano algébrico
	 */
	public double estimatePlanExecutionCost(AbstractOperator operator) throws IOException{
		double cost = 0;
	
		// Selects sobre visões locais
		if (operator.getName().equals("Select") && (!operator.hasChild())){
			// Se o operador não possui filhos, custo = IO
			WrapperLocation wl = operator.getExecutionSite();
			CatalogManager cm = CatalogManager.getUniqueInstance();
			LocalView lv = cm.getLocalView(operator.getApt().getAptRootNode().getLabel());
			operator.setEstimNodesProces(lv.getTotalNodes());
			
			// CUSTO = PESO de IO X Total de registros processados
			cost = this.IOweight * operator.getEstimNodesProces();
			
		}
		// Demais operadores (com filhos)
		else if (operator.hasChild()){
			cost = 0;
			// Calculamos o custo para comunicação com cada filho
			for (int i=0; i<operator.getChildOperators().size(); i++){
				AbstractOperator opChild = operator.getChildAt(i);
				
				// Soma custo da execução do operador filho
				cost += this.estimatePlanExecutionCost(opChild);
				
				// Se os operadores serão executados em sites diferentes, incluímos o custo de comunicação entre os sites
				if (!operator.getExecutionSite().getUri().equals(opChild.getExecutionSite().getUri())){
					
					WrapperLocation wlChild = opChild.getExecutionSite();
					
					// CUSTO = PESO DE COMUNICAÇÃO x Total de registros processados
					cost += wlChild.getCommunicationWeight() * opChild.getEstimNodesProces();
					
				}
				
				
				// Estimativa do número de registros resultantes no operador
				
				// JOIN => maior número de registros
				if (operator.getName().equals("Join")){
					int p = operator.getEstimNodesProces();
					if (p < opChild.getEstimNodesProces()){
						operator.setEstimNodesProces(opChild.getEstimNodesProces());
					}
				}
				// UNION => soma dos registros
				else if (operator.getName().equals("Union")){
					int p = operator.getEstimNodesProces();
					p += opChild.getEstimNodesProces();
					operator.setEstimNodesProces(p);
				}
				// SELECT => estimativa dos filhos
				else if (operator.getName().equals("Select")){
					operator.setEstimNodesProces(opChild.getEstimNodesProces());
				}
				// CONSTRUCT => estimativa dos filhos
				else if (operator.getName().equals("Construct")){
					operator.setEstimNodesProces(opChild.getEstimNodesProces());
				}
			}
		}		
	
		return cost;
	}

}
