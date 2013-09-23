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

import java.util.LinkedList;

import es.uvigo.ei.sing.jarvest.core.OutputHandler;
import es.uvigo.ei.sing.jarvest.core.Transformer;
import es.uvigo.ei.sing.jarvest.core.XMLInputOutput;

public class Util {
	public static String[] runRobot(String resourcePath, String[] input) {
		Transformer robot = XMLInputOutput.loadTransformer(Util.class.getResourceAsStream(resourcePath));
		return runRobot(robot, input);
	}

	public static String[] runRobot(Transformer robot, String[] input) {
		return runRobot(robot, input, null);
	}
	
	public static String[] runRobot(Transformer robot, String[] input, OutputHandler outputHandler) {
		HTTPUtils.clearCookies();
		
		final LinkedList<String> output = new LinkedList<String>();

		if (outputHandler == null){
			class MyOutputHandler implements OutputHandler{
				
				private String currentResult = "";
				
				public void allFinished() {
					
					
				}
				
				public void outputFinished() {
					output.add(this.currentResult);
					this.currentResult = "";
					
					
				}
				
				public void pushOutput(String arg0) {
					currentResult+=arg0;
					
				}
				
			};
			outputHandler = new MyOutputHandler();
		
		}
		robot.setOutputHandler(outputHandler);
		
		for (String string : input){
			robot.pushString(string);
			robot.closeOneInput();
		}
		
		robot.closeAllInputs();
		
		String[] toret = new String[output.size()];
		output.toArray(toret);
		
		
		return toret;

	}

}
