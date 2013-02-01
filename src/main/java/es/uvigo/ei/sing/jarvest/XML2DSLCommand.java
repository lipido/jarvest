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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import es.uvigo.ei.sing.jarvest.dsl.Jarvest;
import es.uvigo.ei.sing.yacli.AbstractCommand;
import es.uvigo.ei.sing.yacli.Option;
import es.uvigo.ei.sing.yacli.Parameters;

public class XML2DSLCommand extends AbstractCommand{

	@Override
	public void execute(Parameters parameters) throws Exception {
		final Jarvest lang = new Jarvest();
		
		
		String fileName = parameters.getSingleValue(findOption("f"));
		if (fileName!=null){
			File file = new File(fileName);
			if (file.exists()){
				String s = lang.xmlToLanguage(file);
				System.out.println(s);
			
			}else{
				throw new IllegalArgumentException("Robot file not found: "+file);
			}
		}else{
			throw new IllegalArgumentException("You must provide the robot as a file or as an inline code");
		}
		
		
		
	}

	@Override
	public String getDescription() {
		return "Transforms a robot in XML to the DSL language";
	}

	@Override
	public String getName() {
		return "xml2dsl";
	}

	
	@Override
	protected List<Option> createOptions() {
		List<Option> toret = new LinkedList<Option>();
		toret.add(new Option("xml-file", "f", "File with a robot code in XML", false, true));
		return toret;
	}

}
