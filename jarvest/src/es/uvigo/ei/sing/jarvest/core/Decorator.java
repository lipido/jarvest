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

public class Decorator extends AbstractTransformer{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	private String head, tail;
	public Decorator(String head, String tail){
		this.head = head;
		this.tail = tail;
	}
	public Decorator(){
		this.head = this.tail = "";
	}
	@Override
	protected String[] _apply(String[] source) {
		String[] toret = new String[source.length];
		
		int counter = 0;
		for (String string : source){
			toret[counter++] = head+string+tail;
		}
		
		return toret;
	}
	
	/*public String toString(){
		String toret = "Decorator Transformer\n" +
				"Head: "+head+"\n" +
				"Tail: "+tail;
		return toret;
	}*/
	
	public String getHead() {
		return head;
	}
	
	public void setHead(String head) {
		this.head = head;
	}
	
	public String getTail() {
		return tail;
	}
	
	
	public void setTail(String tail) {
		this.tail = tail;
	}
	
	// NEW MODEL
	boolean firstCall = true;
	@Override
	protected void _pushString(String str){
		closedInput=false;
		if (firstCall){
			firstCall = false;
			super.getOutputHandler().pushOutput(super.restoreScapes(this.getHead())+str);
		}else{
			super.getOutputHandler().pushOutput(str);
		}
	}
	boolean closedInput = false;
	@Override
	protected void _closeOneInput() {
		if (closedInput) throw new RuntimeException("closing too much!");
		closedInput =true;
		firstCall=true;
		super.getOutputHandler().pushOutput(super.restoreScapes(this.getTail()));
		super.getOutputHandler().outputFinished();
		
	}
	@Override
	protected void _closeAllInputs() {
		// TODO Auto-generated method stub
		firstCall=true;
		super._closeAllInputs();
		
	}
	@Override
	public Object clone() {
		
		// create a clone with the status vars resetted. I don't know if this is really necessary
		Decorator _clone = (Decorator) super.clone();
		_clone.closedInput = false;
		_clone.firstCall = true;
		
		return _clone;
	}
	
}
