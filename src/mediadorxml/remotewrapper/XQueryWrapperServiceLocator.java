/**
 * XQueryWrapperServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package mediadorxml.remotewrapper;

public class XQueryWrapperServiceLocator extends org.apache.axis.client.Service implements XQueryWrapperService {

    public XQueryWrapperServiceLocator() {
    }


    public XQueryWrapperServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public XQueryWrapperServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for XQueryWrapper
    private java.lang.String XQueryWrapper_address = "http://localhost:8080/WrapperSaxon/services/XQueryWrapper";

    public java.lang.String getXQueryWrapperAddress() {
        return XQueryWrapper_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String XQueryWrapperWSDDServiceName = "XQueryWrapper";

    public java.lang.String getXQueryWrapperWSDDServiceName() {
        return XQueryWrapperWSDDServiceName;
    }

    public void setXQueryWrapperWSDDServiceName(java.lang.String name) {
        XQueryWrapperWSDDServiceName = name;
    }

    public XQueryWrapper getXQueryWrapper() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(XQueryWrapper_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getXQueryWrapper(endpoint);
    }

    public XQueryWrapper getXQueryWrapper(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            XQueryWrapperSoapBindingStub _stub = new XQueryWrapperSoapBindingStub(portAddress, this);
            _stub.setPortName(getXQueryWrapperWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setXQueryWrapperEndpointAddress(java.lang.String address) {
        XQueryWrapper_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (XQueryWrapper.class.isAssignableFrom(serviceEndpointInterface)) {
                XQueryWrapperSoapBindingStub _stub = new XQueryWrapperSoapBindingStub(new java.net.URL(XQueryWrapper_address), this);
                _stub.setPortName(getXQueryWrapperWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("XQueryWrapper".equals(inputPortName)) {
            return getXQueryWrapper();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wrapper", "XQueryWrapperService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wrapper", "XQueryWrapper"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("XQueryWrapper".equals(portName)) {
            setXQueryWrapperEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
