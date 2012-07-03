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

import org.junit.Test;

import es.uvigo.ei.sing.jarvest.dsl.Jarvest;

public class URL {

	@Test
	public void test() {
		Jarvest jarvest = new Jarvest();
		String[] results = jarvest.exec(
				"url | xpath('//a/@href')", //robot! 
				"http://www.google.com" //inputs
				);
		
		
		for (String s : results){
			System.out.println(s);
		}
	}

}
