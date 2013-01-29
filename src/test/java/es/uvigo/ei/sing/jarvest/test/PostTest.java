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

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import es.uvigo.ei.sing.jarvest.dsl.Jarvest;

public class PostTest {
	
	private static SimpleServer server;

	@BeforeClass
	public static void setupServer(){
		
		try {
			server = new SimpleServer();
			server.mapURL("/login.php",
					"Login successful",
					"logged=true", //cookiedefs
					null, //requires cookie
					"password" //requires this post parameter
			);
			
			server.mapURL("/inside.php", 
					"This is your private page",
					null, //cookiedefs
					"logged", //requires this cookie
					null
					);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@AfterClass
	public static void destroyServer(){
		server.shutDown();
	}
	@Test
	public void testLogin() throws Exception {
		Jarvest jarvest = new Jarvest();

		String[] results = jarvest.exec("post(:URL=>'http://localhost:"+server.getPort()+"/login.php', :queryString=>'password=foo')" +
				"| append('http://localhost:"+server.getPort()+"/inside.php') | wget");
	
		assertEquals(1, results.length);
		assertEquals("This is your private page", results[0]);
		
		
	}
	
	@Test
	public void testHTTPOutputs() throws Exception {
		Jarvest jarvest = new Jarvest();

		String[] results = jarvest.exec("post(:URL=>'http://localhost:"+server.getPort()+"/login.php', :queryString=>'password=foo', :outputHTTPOutputs=>'true') |" +
		
						"branch(:BRANCH_SCATTERED, :ORDERED) { " +
							"decorate(:head=>'', :tail=>'') \n " +
							"pipe{ append('http://localhost:"+server.getPort()+"/inside.php') | wget() }" +
						"}");
	
		assertEquals(2, results.length);
		assertEquals("Login successful", results[0]);
		assertEquals("This is your private page", results[1]);
	}

}
