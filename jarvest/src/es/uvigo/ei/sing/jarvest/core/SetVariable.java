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

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class SetVariable extends AbstractTransformer {

	private String name;
	private String value;
	
	private static ThreadLocal<Map<String, String>> variables = new ThreadLocal<Map<String, String>>();
	boolean variableWasDefined = false;
	@Override
	protected void _closeOneInput() {
		if (!variableWasDefined) defineVariable();
		this.getOutputHandler().outputFinished();
		
	}

	@Override
	protected void _pushString(String str) {
		
		if (!variableWasDefined) defineVariable();
		this.getOutputHandler().pushOutput(str);
	}
	
	@Override
	protected void _closeAllInputs() {
		if (!variableWasDefined) defineVariable();
		super._closeAllInputs();
	}
	
	private void defineVariable() {
		System.err.println("defining variable "+this.getName()+": "+this.getValue());
		Map<String, String> variablesMap = variables.get();
		if (variablesMap == null){
			variablesMap = new HashMap<String, String>();
			variables.set(variablesMap);
		}
		variablesMap.put(this.getName(), this.getValue());
		variableWasDefined = true;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	
	public static String getVariableValue(String name){
		Map<String, String> variablesMap = variables.get();
		if (variablesMap == null){
			throw new IllegalArgumentException("no variables was defined. Asking for variable: "+name);
		}
		
		String value = variablesMap.get(name);
		if (value == null){
			throw new IllegalArgumentException("undefined variable: "+name);
		}
		
		return value;
	}
	

}
