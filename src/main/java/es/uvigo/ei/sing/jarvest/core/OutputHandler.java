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

public interface OutputHandler {
	
	/**
	 * Pushes in the current output a string
	 * @param string the string to push
	 */
	public void pushOutput(String string);
	/**
	 * Called when an output of the transformer has finished
	 */
	public void outputFinished();
	
	/**
	 * Called when all outputs has finished, subsequent calls to <code>pushOutput</code> and <code>outputFinished</code> will fail
	 */
	public void allFinished();
}
