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
package es.uvigo.ei.sing.jarvest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.uvigo.ei.sing.jarvest.core.OutputHandler;
import es.uvigo.ei.sing.jarvest.core.Transformer;
import es.uvigo.ei.sing.jarvest.dsl.Jarvest;
import es.uvigo.ei.sing.yacli.AbstractCommand;
import es.uvigo.ei.sing.yacli.Option;

public class RunCommand extends AbstractCommand{

	@Override
	public void execute(Map<Option, Object> parameters) throws Exception {
		
		Transformer trans = null;
		String code = this.getSingleValue(parameters, "p");
		if (code!=null){			
			final Jarvest lang = new Jarvest();
			
			trans = lang.eval(code);
		}else{
			String fileName = this.getSingleValue(parameters, "f");
			if (fileName!=null){
				
				File file = new File(fileName);
				if (file.exists()){
					final Jarvest lang = new Jarvest();
					
					trans = lang.eval(new File(fileName));
					//XMLInputOutput.writeTransformer(trans, System.out);
					
				}else{
					throw new IllegalArgumentException("Robot file not found: "+file);
				}
			}else{
				
				System.err.println("You must provide the robot as a file or as an inline code.");
				
				System.exit(1);
			}
		}
		class MyOutputHandler implements OutputHandler{


			public void allFinished() {
				
			
			}

			public void outputFinished() {
				System.out.println();				
			}

			public void pushOutput(String arg0) {
				System.out.print(arg0);
				
			}
						
		};
		trans.setOutputHandler(new MyOutputHandler());
		
		if (!super.hasFlag(parameters, "n")){
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));		
			for (String line = input.readLine(); line!=null; line=input.readLine()){
				trans.pushString(line);
				trans.closeOneInput();
				
			}
		}
		trans.closeAllInputs();
		
	}

	@Override
	public String getDescription() {
		return "Runs a robot";
	}

	@Override
	public String getName() {
		return "run";
	}

	
	@Override
	protected List<Option> createOptions() {
		List<Option> toret = new LinkedList<Option>();
		toret.add(new Option("robot-program", "p", "Program of the robot", true, true));
		toret.add(new Option("robot-file", "f", "File with a robot code", true, true));
		toret.add(new Option("no-input", "n", "Do not read any input", true, false));
		return toret;
	}

}
