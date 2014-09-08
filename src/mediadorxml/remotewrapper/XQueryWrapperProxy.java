package mediadorxml.remotewrapper;

public class XQueryWrapperProxy implements XQueryWrapper {
  private String _endpoint = null;
  private XQueryWrapper xQueryWrapper = null;
  
  public XQueryWrapperProxy() {
    _initXQueryWrapperProxy();
  }
  
  private void _initXQueryWrapperProxy() {
    try {
      xQueryWrapper = (new XQueryWrapperServiceLocator()).getXQueryWrapper();
      if (xQueryWrapper != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)xQueryWrapper)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)xQueryWrapper)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (xQueryWrapper != null)
      ((javax.xml.rpc.Stub)xQueryWrapper)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public XQueryWrapper getXQueryWrapper() {
    if (xQueryWrapper == null)
      _initXQueryWrapperProxy();
    return xQueryWrapper;
  }
  
  public XQueryResult executeXQuery(java.lang.String query) throws java.rmi.RemoteException{
    if (xQueryWrapper == null)
      _initXQueryWrapperProxy();
    return xQueryWrapper.executeXQuery(query);
  }
  
  
}