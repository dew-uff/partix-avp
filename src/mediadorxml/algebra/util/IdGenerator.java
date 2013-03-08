package mediadorxml.algebra.util;

public class IdGenerator {
	
	private int genId;
	private static IdGenerator myInstance;
	
	public IdGenerator(){
		this.genId = 1;
	}
	
	private int getNextIdInternal(){
		return this.genId++;
	}
	
	private static IdGenerator getInstance(){
		if (myInstance == null){
			myInstance = new IdGenerator();
		}
		return myInstance;
	}
	
	/**
	 * Busca o pr�ximo Id (m�todo est�tico)
	 * @return Id
	 */
	public static int getNextId(){
		return IdGenerator.getInstance().getNextIdInternal();		
	}
	
	public static void reset(){
		myInstance = new IdGenerator();
	}

}
