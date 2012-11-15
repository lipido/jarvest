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
package es.uvigo.ei.sing.jarvest.test;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class SimpleServer {

	private static final int HTTP_PORT = 50001;
	private Server jettyserver;
	private HashMap<String, String> URLtoContent = new HashMap<String, String>();
	
	public SimpleServer() throws Exception{
		jettyserver = new Server(HTTP_PORT);
		
		jettyserver.setHandler(new AbstractHandler(){

			@Override
			public void handle(String arg0, Request arg1,
					HttpServletRequest arg2, HttpServletResponse arg3)
					throws IOException, ServletException {
				String result = URLtoContent.get(arg0);
				if (result == null){
					arg3.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}else{
					arg3.getOutputStream().write(result.getBytes());
				}
				arg1.setHandled(true);
				
			}

		
			
		});
		jettyserver.start();
	}
	
	public int getPort(){
		return HTTP_PORT;
	}
	//public void setRoot()
	public void mapURL(String url, String content){
		this.URLtoContent.put(url, content);		
	}
}
