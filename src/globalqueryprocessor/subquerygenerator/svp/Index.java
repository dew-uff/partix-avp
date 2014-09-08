package globalqueryprocessor.subquerygenerator.svp;

public class Index {

	protected String name; // nome do índice.	
	protected String objectType; // indica se o índice está aplicado sobre uma coleção ou sobre um documento.
	protected String objectName; // nome da coleção ou do documento sobre o qual o índice foi definido.
	protected String onPath; // caminho xpath até o nodo pai do nodo indexado.
	protected String byPath; // nome do nodo indexado.
	protected String asType; // indica o tipo do elemento. Pode assumir os valores xs:string,xs:double,xs:integer,...
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getOnPath() {
		return onPath;
	}
	public void setOnPath(String onPath) {
		this.onPath = onPath;
	}
	public String getByPath() {
		return byPath;
	}
	public void setByPath(String byPath) {
		this.byPath = byPath;
	}
	public String getAsType() {
		return asType;
	}
	public void setAsType(String asType) {
		this.asType = asType;
	}
}
