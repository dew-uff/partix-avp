/**
 * XQueryResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package mediadorxml.remotewrapper;

import java.io.*;

public class XQueryResult_original  implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 759535829075193252L;
	private String result;
    private boolean success;
    private long timeMsCompile;
    private long timeMsLocal;
	private long timeMsCommunicRemote;
    private long timeMsRemote;
    private long totalBytes;
    private int numberQueriesExecuted; 

    public XQueryResult_original() {
    }

    public XQueryResult_original(
           java.lang.String result,
           boolean success,
           long timeMsCompile,
           long timeMsLocal,
           long timeMsCommunicRemote,
           long timeMsRemote,
           long totalBytes,
           int numberQueriesExecuted) {
           this.result = result;
           this.success = success;
           this.timeMsCompile = timeMsCompile;
           this.timeMsLocal = timeMsLocal;
           this.timeMsCommunicRemote = timeMsCommunicRemote;
           this.timeMsRemote = timeMsRemote;
           this.totalBytes = totalBytes;
           this.numberQueriesExecuted = numberQueriesExecuted;
    }


    /**
     * Gets the result value for this XQueryResult.
     * 
     * @return result
     */
    public java.lang.String getResult() {
        return result;
    }


    /**
     * Sets the result value for this XQueryResult.
     * 
     * @param result
     */
    public void setResult(java.lang.String result) {
        this.result = result;
    }


    /**
     * Gets the success value for this XQueryResult.
     * 
     * @return success
     */
    public boolean isSuccess() {
        return success;
    }


    /**
     * Sets the success value for this XQueryResult.
     * 
     * @param success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }


    /**
     * Gets the timeMsLocal value for this XQueryResult.
     * 
     * @return timeMsLocal
     */
    public long getTimeMsLocal() {
        return timeMsLocal;
    }


    /**
     * Sets the timeMsLocal value for this XQueryResult.
     * 
     * @param timeMsLocal
     */
    public void setTimeMsLocal(long timeMsLocal) {
        this.timeMsLocal = timeMsLocal;
    }


    /**
     * Gets the timeMsRemote value for this XQueryResult.
     * 
     * @return timeMsRemote
     */
    public long getTimeMsRemote() {
        return timeMsRemote;
    }


    /**
     * Sets the timeMsRemote value for this XQueryResult.
     * 
     * @param timeMsRemote
     */
    public void setTimeMsRemote(long timeMsRemote) {
        this.timeMsRemote = timeMsRemote;
    }


    /**
     * Gets the totalBytes value for this XQueryResult.
     * 
     * @return totalBytes
     */
    public long getTotalBytes() {
        return totalBytes;
    }


    /**
     * Sets the totalBytes value for this XQueryResult.
     * 
     * @param totalBytes
     */
    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }
    
    public int getNumberQueriesExecuted() {
		return numberQueriesExecuted;
	}

	public void setNumberQueriesExecuted(int numberQueriesExecuted) {
		this.numberQueriesExecuted = numberQueriesExecuted;
	}

	public long getTimeMsCompile() {
		return timeMsCompile;
	}

	public void setTimeMsCompile(long timeMsCompile) {
		this.timeMsCompile = timeMsCompile;
	}

	public long getTimeMsCommunicRemote() {
		return timeMsCommunicRemote;
	}

	public void setTimeMsCommunicRemote(long timeMsCommunicRemote) {
		this.timeMsCommunicRemote = timeMsCommunicRemote;
	}

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof XQueryResult)) return false;
        XQueryResult other = (XQueryResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              this.result.equals(other.getResult()))) &&
            this.success == other.isSuccess() &&
            this.timeMsLocal == other.getTimeMsLocal() &&
            this.timeMsRemote == other.getTimeMsRemote() &&
            this.totalBytes == other.getTotalBytes() &&
            this.timeMsCompile == other.getTimeMsCompile() &&
            this.numberQueriesExecuted == other.getNumberQueriesExecuted();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getResult() != null) {
            _hashCode += getResult().hashCode();
        }
        _hashCode += (isSuccess() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += new Long(getTimeMsLocal()).hashCode();
        _hashCode += new Long(getTimeMsRemote()).hashCode();
        _hashCode += new Long(getTotalBytes()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(XQueryResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://wrapper", "XQueryResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("result");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "result"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("success");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "success"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeMsCompile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "timeMsCompile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeMsLocal");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "timeMsLocal"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeMsCommunicRemote");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "timeMsCommunicRemote"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeMsRemote");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "timeMsRemote"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalBytes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "totalBytes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numberQueriesExecuted");
        elemField.setXmlName(new javax.xml.namespace.QName("http://wrapper", "numberQueriesExecuted"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }


}
