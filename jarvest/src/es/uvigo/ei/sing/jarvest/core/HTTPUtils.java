/*

Copyright 2012 Daniel Gonzalez Peña


This file is part of the jARVEST Project. 

jARVEST Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

jARVEST Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with jARVEST Project.  If not, see <http://www.gnu.org/licenses/>.
*/
package es.uvigo.ei.sing.jarvest.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HTTPUtils {
	
	private static HashMap<Thread, HttpClient> clients = new HashMap<Thread, HttpClient>();
	
	public static String DEFAULT_USER_AGENT = "Mozilla/5.0 (compatible; jARVEST; +http://sing.ei.uvigo.es/jarvest)";
	
	private static HttpClient getClient(){
		
		// one client per thread
		if (clients.get(Thread.currentThread())== null){
			
			Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
			Protocol.registerProtocol("https", easyhttps);
			if (System.getProperty("httpclient.useragent")==null){
				System.getProperties().setProperty("httpclient.useragent", DEFAULT_USER_AGENT);
			}
			HttpClient client = new HttpClient();
			if (System.getProperty("http.proxyHost")!=null && System.getProperty("http.proxyPort")!=null){ 
				client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
			}
			clients.put(Thread.currentThread(), client);
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			
			client.getParams().setBooleanParameter("http.protocol.single-cookie-header",true);
			
			
			
		}
		return clients.get(Thread.currentThread());
	}
	private static int reading = 0;
	private static String gettingContent = "";
	public static InputStream getURLBody(final String url, StringBuffer charset) throws IOException{
		return getURLBody(url, charset, new HashMap<String, String>());
	}
	
	public static InputStream getURLBody(final String url, StringBuffer charset, Map<String, String> additionalHeaders) throws IOException{
		HttpClient client = getClient();
		System.err.println("Connecting to: "+url);
        final GetMethod get = new GetMethod(url);
        
        addHeaders(additionalHeaders, get);
        
        
       
	       /* synchronized(HTTPUtils.class){
	        	System.out.println("Trying to get "+url);
	        	while (reading>0){
	        		try {
	        			
	        			System.out.println("getting "+url+". Somebody using the http client, waiting. Currently reading: "+reading+" url reading: "+gettingContent);
	        			HTTPUtils.class.wait();
	        			System.out.println("getting "+url+". Stop waiting for http client");
	        		} catch (InterruptedException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	        	}
	        	reading++;
	        	gettingContent = url.toString();
	        }*/
	    try{
	    //	System.out.println("Executing method get for url "+url);
	    	
	        client.executeMethod(get);
	       // System.out.println("Executed method! "+url);
	        charset.append(get.getResponseCharSet());
	        //System.out.println("charset response is: "+charset);
	        final InputStream in = get.getResponseBodyAsStream();
	        return new InputStream(){
	        	
	        	@Override
	        	public int read() throws IOException {
	        		return in.read();
	        	}
	        	
	        	@Override
	        	public void close(){				
	        		get.releaseConnection();
	        		/*synchronized(HTTPUtils.class){
	        			if (reading>0) reading--;
	        			System.out.println("Getted content of "+url+" notifying others waiting. Currently reading: "+reading);
	        			HTTPUtils.class.notify();
	        		}*/
	        	}
	        	
	        };
        }
        catch(Exception e){
        	/*synchronized(HTTPUtils.class){
    			if (reading>0) reading--;
    			System.out.println("Getting an exception getting "+url+" notifying others waiting. Currently reading: "+reading);
    			HTTPUtils.class.notify();
    		}*/
        	throw new RuntimeException(e);
        }
	}

	private static void addHeaders(Map<String, String> additionalHeaders,
			final HttpMethod method) {
		for (String header : additionalHeaders.keySet()){
			System.err.println("Adding header. "+header+": "+additionalHeaders.get(header) );
        	method.addRequestHeader(header, additionalHeaders.get(header));
        }
	}
	private static String validCharset(String string) {
		
		String input = string.toUpperCase();
		if (Charset.availableCharsets().containsKey(input)){ return input;
		}else return Charset.defaultCharset().name();
	}
	public static String getURLBodyAsString(String url) throws IOException{
		return getURLBodyAsString(url, new HashMap<String, String>());
	}
	public static String getURLBodyAsString(String url, Map<String, String> additionalHeaders) throws IOException{
	
		InputStream in =null;
		try{
			StringBuffer charsetb = new StringBuffer();
			in= getURLBody(url,charsetb, additionalHeaders);
			
			String charset = validCharset(charsetb.toString());
			Reader reader = new InputStreamReader(in, Charset.forName(charset));
			
			StringBuffer sbuf = new StringBuffer();
			
			char[] characters = new char[1024];
			int readed=0;
			while( (readed =reader.read(characters))!=-1){
				sbuf.append(characters, 0, readed);
			}
			
			/*
			
			StringBuffer buffer = new StringBuffer(1024);
			
			
			byte[] bytes = new byte[1024];
			int readed=0;
			while ((readed=in.read(bytes))!=-1){
				
				buffer.append(new String(bytes, 0, readed, charset));
			}
			return new String(buffer);*/
			//System.out.println("Content getted from: "+url);
			return sbuf.toString();
		}finally{
			
			if(in!=null) in.close();
			
		}
	}
	public synchronized static String doPost(String urlstring, String queryString, String separator) throws HttpException, IOException{
		return doPost(urlstring, queryString, separator, new HashMap<String, String>());
	}
	public synchronized static String doPost(String urlstring, String queryString, String separator, Map<String, String> additionalHeaders) throws HttpException, IOException{
		
		
		HashMap<String, String> query = parseQueryString(queryString, separator);
		HttpClient client = getClient();
		
		URL url = new URL(urlstring);
		int port = url.getPort();
		if (port == -1){
			if (url.getProtocol().equalsIgnoreCase("http")){
				port = 80;
			}
			if (url.getProtocol().equalsIgnoreCase("https")){
				port = 443;
			}
		}
		
		
        client.getHostConfiguration().setHost(url.getHost(), port, url.getProtocol());
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        PostMethod post = new PostMethod(url.getFile());
        addHeaders(additionalHeaders, post);
        post.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
        post.setRequestHeader("Accept","*/*");
        // Prepare login parameters
        NameValuePair[] valuePairs = new NameValuePair[query.size()];
        
        int counter = 0;
        
        for (String key: query.keySet()){
        	//System.out.println("Adding pair: "+key+": "+query.get(key));
        	valuePairs[counter++] = new NameValuePair(key, query.get(key));
        
        }
        
        
        post.setRequestBody(valuePairs);
        //authpost.setRequestEntity(new StringRequestEntity(requestEntity));
       
        client.executeMethod(post);
         
      
        
        // See if we got any cookies
        // The only way of telling whether logon succeeded is 
        // by finding a session cookie
        CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
        Cookie[] logoncookies = cookiespec.match(
            url.getHost(), port, "/", false, client.getState().getCookies());
        
        /*
        System.out.println("Logon cookies:");    
        if (logoncookies.length == 0) {
            System.out.println("None");    
        } else {
            for (int i = 0; i < logoncookies.length; i++) {
                System.out.println("- " + logoncookies[i].toString());    
            }
        }
        */
        // Usually a successful form-based login results in a redicrect to 
        // another url
        int statuscode = post.getStatusCode();
        String toret = "";
        if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
            (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
            (statuscode == HttpStatus.SC_SEE_OTHER) ||
            (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
            Header header = post.getResponseHeader("location");
            if (header != null) {
                String newuri = header.getValue();
                if ((newuri == null) || (newuri.equals(""))) {
                    newuri = "/";
                }
                
             //   System.out.println("Redirect target: " + newuri); 
        /*        GetMethod get2 = new GetMethod(url.getFile()+"/"+newuri);
                
                client.executeMethod(get2);
                toret = get2.getResponseBodyAsString();
                get2.releaseConnection();*/
                
            } else {
                System.out.println("Invalid redirect");
                System.exit(1);
            }
        }else{
        	
        	InputStream stream =  post.getResponseBodyAsStream();
        	byte[] bytes = new byte[1024];
        	int read = -1;
        	while((read=stream.read(bytes))!=-1){
        		toret+=new String(bytes,0,read);
        	}
        	
        }
        // release any connection resources used by the method
        post.releaseConnection();
        return toret;
        
        
	}
	private static HashMap<String, String> parseQueryString(String query, String separator){
		HashMap<String, String> toret = new HashMap<String, String>();
		
		
		for(String pair : query.split(separator)){
			int index = pair.indexOf("=");
			if (index!=-1 && index<pair.length()){
				String key = pair.substring(0, index);
				String value = pair.substring(index+1);
			
				// some replacements
				value = value.replaceAll("%3D", "=").replaceAll("%3D", "=").replaceAll("%20", " ");
				
				toret.put(key, value);
			}
			
			else{
				System.err.println("ignoring string as a valid key-value pair: "+pair);
				continue;
			}
			
		}
		
		
		return toret;
		
	}
	
	
}
class EasySSLProtocolSocketFactory implements SecureProtocolSocketFactory {

    /** Log object for this class. */
    

    private SSLContext sslcontext = null;

    /**
     * Constructor for EasySSLProtocolSocketFactory.
     */
    public EasySSLProtocolSocketFactory() {
        super();
    }

    private static SSLContext createEasySSLContext() {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(
              null, 
              new TrustManager[] {new EasyX509TrustManager(null)}, 
              null);
            return context;
        } catch (Exception e) {
            
            throw new HttpClientError(e.toString());
        }
    }

    private SSLContext getSSLContext() {
        if (this.sslcontext == null) {
            this.sslcontext = createEasySSLContext();
        }
        return this.sslcontext;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
        String host,
        int port,
        InetAddress clientHost,
        int clientPort)
        throws IOException, UnknownHostException {

        return getSSLContext().getSocketFactory().createSocket(
            host,
            port,
            clientHost,
            clientPort
        );
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect timeout a 
     * controller thread is executed. The controller thread attempts to create a new socket 
     * within the given limit of time. If socket constructor does not return until the 
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *  
     * @param host the host name/IP
     * @param port the port on the host
     * @param clientHost the local host name/IP to bind the socket to
     * @param clientPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     * 
     * @return Socket a new socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    public Socket createSocket(
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        final HttpConnectionParams params
    ) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketfactory = getSSLContext().getSocketFactory();
        if (timeout == 0) {
            return socketfactory.createSocket(host, port, localAddress, localPort);
        } else {
            Socket socket = socketfactory.createSocket();
            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(
            host,
            port
        );
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(
        Socket socket,
        String host,
        int port,
        boolean autoClose)
        throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(
            socket,
            host,
            port,
            autoClose
        );
    }

    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(EasySSLProtocolSocketFactory.class));
    }

    public int hashCode() {
        return EasySSLProtocolSocketFactory.class.hashCode();
    }

}

class EasyX509TrustManager implements X509TrustManager
{
    private X509TrustManager standardTrustManager = null;

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(EasyX509TrustManager.class);

    /**
     * Constructor for EasyX509TrustManager.
     */
    public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        this.standardTrustManager = (X509TrustManager)trustmanagers[0];
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
        standardTrustManager.checkClientTrusted(certificates,authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
     */
    public void checkServerTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
        if ((certificates != null) && LOG.isDebugEnabled()) {
            LOG.debug("Server certificate chain:");
            for (int i = 0; i < certificates.length; i++) {
                LOG.debug("X509Certificate[" + i + "]=" + certificates[i]);
            }
        }
        if ((certificates != null) && (certificates.length == 1)) {
            //certificates[0].checkValidity();
        } else {
            //standardTrustManager.checkServerTrusted(certificates,authType);
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }
}