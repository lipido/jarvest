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

import es.uvigo.ei.sing.jarvest.core.SimpleTransformer;
import es.uvigo.ei.sing.jarvest.core.Transformer;
import es.uvigo.ei.sing.jarvest.core.Util;
import es.uvigo.ei.sing.jarvest.dsl.Jarvest;

public class OneToOne {
	@Test
	public void testOneInputToOneOutput() throws Exception {
		Jarvest jarvest = new Jarvest();	
		String[] results = jarvest.exec(
			"append('aa')\n"+
			"append('aaa')\n"+
			"one_to_one{\n"+
			"	match('(a)') \n"+
			"}"+
		"");
		
		assertEquals("aa", results[0]);
		assertEquals("aaa", results[1]);
	}
	
	@SuppressWarnings("serial")
	@Test
	public void closeAllOnce() throws Exception {
		Jarvest jarvest = new Jarvest();

		Transformer t = jarvest.eval(
				"append('aa')\n"+
				"append('aaa')\n"+
								
				"one_to_one{\n"+
				"	match('(a)') \n"+
				//"	\n"+
				"}"+
				"");
		
		t.add(new SimpleTransformer(){
			private boolean allClosed = false;
			@Override
			protected void _closeAllInputs() {
				assertEquals(false, allClosed);
				allClosed = true;
				super._closeAllInputs();
				
			}
		});
		
		Util.runRobot(t, new String[0]);
	}
	@Test
	public void testInternalMergers() throws Exception {
		Jarvest jarvest = new Jarvest();
		
		String[] results = jarvest.exec(
				"append('aa')\n"+
				"append('aaa')\n"+
								
				"one_to_one{\n"+
				"	match('(a)') \n merge \n decorate(:head=>'<found>',:tail=>'</found>') \n"+
				//"	\n"+
				"}"+
				"");

		assertEquals("<found>aa</found>", results[0]);
		assertEquals("<found>aaa</found>", results[1]);
	}
	@Test
	public void testIsolation() throws Exception {
		Jarvest jarvest = new Jarvest();
		
		String[] results = jarvest.exec(
				"append('a')\n"+
				"append('b')\n"+
								
				"one_to_one{\n"+
				"	decorate(:tail=>' is %%0%%')"+
				"}");

		assertEquals(Arrays.asList(new String[]{"a is a", "b is b"}), Arrays.asList(results));
	}
		
}
