package mediadorxml.tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class TesteGraphicPlan extends JFrame  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8829826232315796259L;
	private JPanel jContentPane = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TesteGraphicPlan application = new TesteGraphicPlan();
		application.show();
	}
	
	/**
	 * This is the default constructor
	 */
	public TesteGraphicPlan() {
		super();
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400, 400);
		this.setContentPane(getJContentPane());
		this.setTitle("XQuery Plan Graphic");
	}
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			GraphPanel gp = new GraphPanel();
			gp.setPreferredSize(new Dimension(400,400));
			jContentPane.add(new JScrollPane(gp));
//			jContentPane.add(getJButtonRun(), java.awt.BorderLayout.SOUTH);
//			jContentPane.add(getRadioButtonGroup(), java.awt.BorderLayout.NORTH);
//			jContentPane.add(getJScrollPaneInput(), java.awt.BorderLayout.WEST);
//			jContentPane.add(getJScrollPaneOutput(), java.awt.BorderLayout.EAST);
		}
		return jContentPane;
	}
	
}

class GraphPanel extends JPanel{
	
	public GraphPanel(){
		//this.setSize(700, 700);
	}
	
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.white);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Dimension d = getSize();
        
        int rectWidth = 100;
        int rectHeight = 100;
        
        int x = d.width/2 - rectWidth/2;
        int y = d.height/2 - rectHeight/2;

        Area p = new Area(new RoundRectangle2D.Double(x, y, rectWidth, rectHeight, 10, 10));
        
        g2.draw(p);
        g2.drawString("Construct", x + 3, y + 14);
        
    }
}
