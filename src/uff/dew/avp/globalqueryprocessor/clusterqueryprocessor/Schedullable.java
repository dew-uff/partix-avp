/*
 * Created on 27/04/2005
 *
 */
package uff.dew.avp.globalqueryprocessor.clusterqueryprocessor;

 
/**
 * @author bmiranda
 *
 */
public abstract class Schedullable {
	protected QueryScheduler scheduller;
	protected int queryNumber; 
	public abstract void go();
	/**
	 * @param scheduller The scheduller to set.
	 */
	public void setScheduller(QueryScheduler scheduller) {
		this.scheduller = scheduller;
	}
	public int getQueryNumber() {
		return queryNumber;
	}
}
