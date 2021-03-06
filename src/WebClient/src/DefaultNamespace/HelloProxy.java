package DefaultNamespace;

public class HelloProxy implements DefaultNamespace.Hello {
  private String _endpoint = null;
  private DefaultNamespace.Hello hello = null;
  
  public HelloProxy() {
    _initHelloProxy();
  }
  
  public HelloProxy(String endpoint) {
    _endpoint = endpoint;
    _initHelloProxy();
  }
  
  private void _initHelloProxy() {
    try {
      hello = (new DefaultNamespace.HelloServiceLocator()).getHello();
      if (hello != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)hello)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)hello)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (hello != null)
      ((javax.xml.rpc.Stub)hello)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public DefaultNamespace.Hello getHello() {
    if (hello == null)
      _initHelloProxy();
    return hello;
  }
  
  public void addUser(java.lang.String id, java.lang.String email, java.lang.String password, java.lang.String fullname, java.lang.String alamat, java.lang.String provinsi, java.lang.String kota, int kodepos, int hp) throws java.rmi.RemoteException{
    if (hello == null)
      _initHelloProxy();
    hello.addUser(id, email, password, fullname, alamat, provinsi, kota, kodepos, hp);
  }
  
  public java.lang.String sayHello(java.lang.String name) throws java.rmi.RemoteException{
    if (hello == null)
      _initHelloProxy();
    return hello.sayHello(name);
  }
  
  public void insertBarang(java.lang.String query) throws java.rmi.RemoteException{
    if (hello == null)
      _initHelloProxy();
    hello.insertBarang(query);
  }
  
  
}