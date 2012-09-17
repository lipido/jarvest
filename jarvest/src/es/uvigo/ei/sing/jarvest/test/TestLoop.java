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

import static org.junit.Assert.*;

import org.junit.Test;

import es.uvigo.ei.sing.jarvest.dsl.Jarvest;

public class TestLoop {
	
	
	@Test
	public void testLoop() throws Exception {
		Jarvest jarvest = new Jarvest();

		SimpleServer server = new SimpleServer();
		server.mapURL("/page1.html",
				"<b>Page 1 results</b><br>" +
				"<a id=\"next\" href=\"/page2.html\">Next</a>"
		);
		server.mapURL("/page2.html", 
				"<b>Page 2 results</b><br>");
		
		String[] results = jarvest.exec("wget{ xpath('//b') }.repeat?{xpath('//a[@id=\"next\"]/@href') | decorate(:head=>'http://localhost:"+server.getPort()+"')}", "http://localhost:"+server.getPort()+"/page1.html");
	
		assertEquals(2, results.length);
		assertEquals("Page 1 results", results[0]);
		assertEquals("Page 2 results", results[1]);
		
		
	}

}
