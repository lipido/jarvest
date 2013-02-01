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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import es.uvigo.ei.sing.jarvest.core.OutputHandler;
import es.uvigo.ei.sing.jarvest.dsl.Jarvest;


public class URLTest {

private static SimpleServer server;
	
	@BeforeClass
	public static void setupServer() throws Exception{
		server = new SimpleServer();
		server.mapURL("/index.html",
				"<html><body>" +
				"<a href='link1'>link1</a>" +
				"<a href='link2'>link2</a>" +
				"</body></html>"
		);
		server.mapURL("/page2.html", 
				"<b>Page 2 results</b><br>");
	}
	@AfterClass
	public static void shutDownServer(){
		server.shutDown();
	}
	
	@Test
	public void test() {
		Jarvest jarvest = new Jarvest();
		String[] results = jarvest.exec(
				"wget | xpath('//a/@href')", //robot!			
				"http://localhost:"+server.getPort()+"/index.html" //inputs
				
		);
		
		assertEquals(Arrays.asList(new String[]{"link1", "link2"}), Arrays.asList(results));
		
	}
	@Test
	public void testOutputHandler() {
		Jarvest jarvest = new Jarvest();
		
		class Conditions{
			int outputFinished = 0;
			boolean allFinished = false;
		}
		final Conditions conditions = new Conditions();
		jarvest.exec(
				"wget | xpath('//a/@href')", //robot! 
				new OutputHandler(){

					@Override
					public void pushOutput(String string) {
						assertTrue(string.startsWith("link"));
						
					}

					@Override
					public void outputFinished() {
						conditions.outputFinished ++;
						
					}

					@Override
					public void allFinished() {
						conditions.allFinished = true;
						
					}
					
				},
				"http://localhost:"+server.getPort()+"/index.html" //inputs
				
		);
		assertTrue(conditions.allFinished);
		assertEquals(2, conditions.outputFinished);
	}

}
