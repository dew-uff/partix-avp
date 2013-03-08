package mediadorxml.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

import mediadorxml.algebra.basic.PatternTree;
import mediadorxml.algebra.basic.TreeNode;
import mediadorxml.algebra.operators.AbstractOperator;

public class PlanPanel extends JPanel {

	private AbstractOperator operator;
    
	private int gap = 35;
	private int charWidth = 7;
	private int charHeight = 15;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6521760997056553273L;
	
	public PlanPanel(AbstractOperator rootOperator){
		operator = rootOperator;
		//this.setSize(500, 500);
	}
	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.white);

        Graphics2D g2 = (Graphics2D) g;
        
        int x = 350;
        int y = 10;
        
        this.drawOperator(g2, operator, x, y);        
    }
	
	private void drawOperator(Graphics2D g, AbstractOperator op, int x, int y){
		
		int rectWidth = 200;
		int rectHeight = 15;    
		
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nome do operador e opId
        g.drawString(op.getName() + "  (" + op.getOperatorId() + ")", x - rectWidth/2 + 3, y + rectHeight);
        rectHeight += charHeight;
        
        // Predicado do operador
        if ((op.getPredicateList() != null) && (op.getPredicateList().size() > 0)){
        	String pred = "";
        	for (int i=0; i<op.getPredicateList().size(); i++){
        		pred += op.getPredicateList().get(i) + ";";
        	}
	        if (("Pred: ".length() + pred.length())*charWidth > rectWidth){
	        	rectWidth = ("Pred: ".length() + pred.length())*charWidth;
	        }
	        g.drawString("Pred: " + pred, x - rectWidth/2 + 3, y + rectHeight);
	        rectHeight += charHeight;
        }
        
        //Site de execução
        if ((op.getExecutionSite() != null) && (op.getExecutionSite().getUri() != null)){	        
	        if ((op.getExecutionSite().getUriHost().length() + "Site: ".length())*charWidth > rectWidth){
	        	rectWidth = (op.getExecutionSite().getUriHost().length() + "Site: ".length())*charWidth;
	        }
	        g.drawString("Site: " + op.getExecutionSite().getUriHost(), x - rectWidth/2 + 3, y + rectHeight);
	        rectHeight += charHeight;
        }
        
        // Pattern Tree (APT)
        if ((op.getApt() != null) && (op.getApt().getAptRootNode() != null)){
        	ReturnDimensionApt d = this.drawAptTreeNode(g, op.getApt().getAptRootNode(), x - rectWidth/2 + 5, y + rectHeight);
        	if (d.dimension.getWidth() > rectWidth){
        		rectWidth = (int)d.dimension.getWidth();
        	}
        	rectHeight += (int)d.dimension.getHeight();
        }
        
        
        // Contorno do retângulo do operador
        Area p = new Area(new RoundRectangle2D.Double(x - rectWidth/2, y, rectWidth, rectHeight, 10, 10));
        g.draw(p);
                
        // Operadores filho
        if (op.hasChild()){
        	if (op.getChildOperators().size() == 1){
        		this.drawOperator(g, op.getChildAt(0), x, y + rectHeight + gap);
        		g.drawLine(x, y + rectHeight, x, y + rectHeight + gap);
        	}
        	else if (op.getChildOperators().size() == 2){
        		int space = 0;
        		if ((!op.getChildAt(1).hasChild()) || 
        			(op.getChildAt(1).hasChild() && op.getChildAt(0).hasChild() && 
        				(op.getChildAt(1).getChildOperators().size() == 1) && 
        				(op.getChildAt(0).getChildOperators().size() == 1))){
	        		space = rectWidth/2;
        		}
        		else{
        			space = rectWidth;
        		}
        		this.drawOperator(g, op.getChildAt(1), x - space - gap/2, y + rectHeight + gap);
        		this.drawOperator(g, op.getChildAt(0), x + space + gap/2, y + rectHeight + gap);
        		
        		g.drawLine(x - gap, y + rectHeight, x - space - gap/4, y + rectHeight + gap);
        		g.drawLine(x + gap, y + rectHeight, x + space + gap/4, y + rectHeight + gap);
        	}
        }
	}
	
	private ReturnDimensionApt drawAptTreeNode(Graphics2D g, TreeNode node, int topLeftX, int topLeftY){
		
		// Margem para desenho do "balão"
		int y = topLeftY + 5;
		
		// Label principal
		String label = node.getLabel() + " (" + node.getNodeId() + ")";
		
		if (node.getPredicate() != null){
			label += node.getPredicate().toString();
		}
		
		int labelWidth = label.length()*this.charWidth;
		
		int righty = y + this.charHeight;
		
		g.drawString(label, topLeftX + 7, y);
		
		
		// Elipse que contorna o label
		g.drawOval(topLeftX , y - this.charHeight, labelWidth, charHeight + 5);
		
		int x = labelWidth/2;
		
		// Nodos filhos
		if (node.hasChield()){
			
			int leftx;
			if (node.getChildren().size() == 1){
				leftx = topLeftX - 20 + labelWidth/2;
			}
			else{
				leftx = topLeftX + 20 + labelWidth/2;
			}
			int lefty = y + charHeight + 10;
			
			int lineX1 = topLeftX + labelWidth/2;
			int lineY1 = y + 5;
			
			int rigthx = topLeftX + labelWidth + 10;
						
			for (int i=0; i<node.getChildren().size(); i++){
				TreeNode childNode = node.getChild(i);
				ReturnDimensionApt d = this.drawAptTreeNode(g, childNode, leftx, lefty);

				boolean ancestor = childNode.getRelationType().equals(TreeNode.RelationTypeEnum.ANCESTOR_DESCENDENT);
				
				if (node.getChildren().size() == 1){
					//g.drawLine(lineX1, lineY1, (int)(leftx - 15 + d.dimension.getWidth()/2), lefty - 10);
					g.drawLine(lineX1, lineY1, leftx+d.x, lefty - 10);
					if (ancestor){
						//g.drawLine(lineX1+10, lineY1, (int)(leftx - 15 + d.dimension.getWidth()/2)+10, lefty - 10);
						g.drawLine(lineX1+7, lineY1, leftx+d.x+7, lefty - 10);
					}
				}
				else{
					g.drawLine(lineX1, lineY1, leftx, lefty);
					if (ancestor){
						g.drawLine(lineX1+10, lineY1, leftx+10, lefty);
					}
				}
				
				lefty += d.dimension.getHeight() + 5;
				
				int newRigthx = (int)(leftx + d.dimension.getWidth() + 10);
				if (newRigthx > rigthx){
					rigthx = newRigthx;
				}
				
			}
			
			return new ReturnDimensionApt(new Dimension(rigthx - topLeftX, lefty - topLeftY), x);
			
		}
		else{
			return new ReturnDimensionApt(new Dimension(labelWidth + 10, righty - topLeftY), x);
		}		
	}
	
	public class ReturnDimensionApt extends Object{
		public Dimension dimension;
		public int x;
		public ReturnDimensionApt(Dimension d, int i){
			dimension = d;
			x = i;
		}
	}
}

