package globalqueryprocessor.subquerygenerator.svp;

import java.util.ArrayList;

public class Collection {

	protected ArrayList<Document> documents; // documentos pertencentes a esta coleção
	protected String name; // nome da coleção

	public ArrayList<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(ArrayList<Document> documents) {
		this.documents = documents;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
