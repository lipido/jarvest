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

import java.util.Arrays;

import org.junit.Test;

import es.uvigo.ei.sing.jarvest.dsl.Jarvest;

public class TestBranches {
	
	
	@Test
	public void testScatteredScattered() throws Exception {
		Jarvest jarvest = new Jarvest();

		
		
		String[] results = jarvest.exec(
						"append('a')\n"+
						"append('b')\n"+
						"append('c')\n"+
						"append('d')\n"+						
						"branch(:BRANCH_SCATTERED, :SCATTERED) {\n"+
						"	decorate(:head=>'', :tail=>'')\n"+
						"	decorate(:head=>'', :tail=>'')\n"+
						"}");
	
		System.out.println(Arrays.toString(results));
		assertEquals("a", results[0]);
		assertEquals("b", results[1]);
		assertEquals("c", results[2]);
		assertEquals("d", results[3]);
		
	}
	
	@Test
	public void testScatteredOrdered() throws Exception {
		Jarvest jarvest = new Jarvest();

		
		
		String[] results = jarvest.exec(
				"append('a')\n"+
				"append('b')\n"+
				"append('c')\n"+
				"append('d')\n"+						
				"branch(:BRANCH_SCATTERED, :ORDERED) {\n"+
				"	decorate(:head=>'', :tail=>'')\n"+
				"	decorate(:head=>'', :tail=>'')\n"+
				"}");

		System.out.println(Arrays.toString(results));
		assertEquals("a", results[0]);
		assertEquals("c", results[1]);
		assertEquals("b", results[2]);
		assertEquals("d", results[3]);
		
		}
	@Test
	public void testOne2One() throws Exception {
		Jarvest jarvest = new Jarvest();

		
		
		String[] results = jarvest.exec(
				"append('aa')\n"+
				"append('aaa')\n"+
								
				"one_to_one{\n"+
				"	match('(a)') \n merge \n decorate(:head=>'<found>',:tail=>'</found>') \n"+
				//"	\n"+
				"}"+
				"");

		System.out.println(Arrays.toString(results));
		assertEquals("<found>aa</found>", results[0]);
		assertEquals("<found>aaa</found>", results[1]);
		
		}



}

