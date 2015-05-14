package mediadorxml.tests;

import java.io.IOException;
import java.util.ArrayList;

import mediadorxml.catalog.CatalogManager;
import mediadorxml.catalog.util.Catalog;
import mediadorxml.catalog.util.GlobalView;
import mediadorxml.catalog.util.LocalView;
import mediadorxml.catalog.util.WrapperLocation;

public class CatalogTest {
	
	public static void main(String[] args) throws Exception {
		
		// Cria��o do cat�logo (catalog.xml)
		CatalogTest.createCatalog();
		
		CatalogManager cm = new CatalogManager();		
	}
	
	public static void createCatalog() throws IOException{
		//		 Global View
		GlobalView gvBib = new GlobalView("student.xml");
		gvBib.setXsdFile("student.xsl");
		
		// Local View 1
		LocalView lv1 = new LocalView();
		lv1.setReferenceCollection("student.xml");
		lv1.setSelectionPredicates("/student/person/age >= 30");
		WrapperLocation wl1 = new WrapperLocation("http://localhost/abc");
		wl1.setCommunicationWeight(10);
		lv1.setWrapperLocation(wl1);
		lv1.setViewName("F1");
		lv1.setTotalNodes(500);
		gvBib.addLocalView(lv1);
		
		// Local View 2
		LocalView lv2 = new LocalView();
		lv2.setReferenceCollection("student.xml");
		lv2.setSelectionPredicates("/student/person/age < 30");
		WrapperLocation wl2 = new WrapperLocation("http://localhost/xyz");
		wl2.setCommunicationWeight(5);
		ArrayList l = new ArrayList();
		l.add(wl1);
		l.add(wl2);
		lv2.setWrapperLocation(l);
		lv2.setViewName("F2");
		lv2.setTotalNodes(100);
		gvBib.addLocalView(lv2);
				
		Catalog c = new Catalog("CatalogoStudents");
		
		CatalogManager cm = new CatalogManager(c);
		cm.save();
		cm = null;
	}

}
