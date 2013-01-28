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

public class VariablesTest {
	@Test
	public void makeInputsAsVariables() throws Exception {
		
		String input1 = "foo";
		String input2 = "bar";
		Jarvest jarvest = new Jarvest();
		
		String[] results = jarvest.exec(
			"setvar(:name=>'myvar1', :value=>'%%0%%')\n"+
			"setvar(:name=>'myvar2', :value=>'%%1%%')\n"+
			"append(:append=>'first input was %%myvar1%%',:inputFilter=>'0-1')\n"+
			"append(:append=>'second input was %%myvar2%%')\n"			
		, new String[]{input1, input2});
		
		assertEquals("first input was "+input1, results[0]);
		assertEquals("second input was "+input2, results[1]);
	}
	
	@Test
	public void testIsolation() throws Exception {
		
		Jarvest jarvest = new Jarvest();
		
		
		String[] results = jarvest.exec(
				"append('a')\n"+
				"append('b')\n"+								
				"one_to_one{\n"+
				"	setvar(:name=>'%%0%%', :value=>'defined %%0%%')\n" +
				"}\n" +
				"append(:append=>'%%a%%', :inputFilter=>'0-1')\n" +
				"append('%%b%%')\n"+
				
				"");

		System.out.println(Arrays.toString(results));
		assertEquals(Arrays.asList(new String[]{"defined a", "defined b"}), Arrays.asList(results));
		
		
	}	
		
}
