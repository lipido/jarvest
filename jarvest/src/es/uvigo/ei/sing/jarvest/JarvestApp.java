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

import java.util.LinkedList;
import java.util.List;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.Command;

public class JarvestApp extends CLIApplication{

	@Override
	protected List<Command> buildCommands() {
		List<Command> toret = new LinkedList<Command>();
		toret.add(new RunCommand());
		toret.add(new XML2DSLCommand());
		return toret;
	}

	@Override
	protected String getApplicationCommand() {
		return "jarvest";
	}

	@Override
	protected String getApplicationName() {
		return "jarvest";
	}
	
	public static void main(String[] args){
		new JarvestApp().run(args);
	}

}
