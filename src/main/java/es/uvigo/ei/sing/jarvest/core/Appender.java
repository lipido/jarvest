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

public class Appender extends AbstractTransformer {
	private static final long serialVersionUID = 1L;
	
	private String append;
	public String getAppend() {
		return append;
	}
	public void setAppend(String append) {
		this.append = append;
	}
	public Appender(String append){
		
		this.append = append;
		
	}
	public Appender(){
		this.append="";
	}
	
	
	@Override
	protected void _closeAllInputs(){		
		super.getOutputHandler().pushOutput(super.restoreScapes(append));
		
		super.getOutputHandler().outputFinished();
		super.getOutputHandler().allFinished();
	}
	@Override
	protected String[] _apply(String[] source) {
		String[] res = new String[source.length+1];
		System.arraycopy(source, 0, res, 0, source.length);
		res[res.length-1]=append;
		return res;
		
	}
	@Override
	protected void _closeOneInput() {		
		super.getOutputHandler().outputFinished();
	}
	@Override
	protected void _pushString(String str){
		super.getOutputHandler().pushOutput(str);
	}
}
