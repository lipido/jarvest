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

public class OneShotTransformer extends AbstractTransformer{

	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 1L;
	

	@Override
	protected String[] _apply(String[] source) {
		return source;
	}


	/* the one_to_one makes an allFinished call to its childs in order 
	 * to make childs isolated for each input (ex: make possible mergers 
	 * to send outputs). However the AbstractTransformer will propagate
	 * the allFinsihed outside this one_to_one. With this wrapper, we
	 * avoid this.
	 */
	private class OutputWrapper implements OutputHandler{

		OutputHandler internal;
		public OutputWrapper(OutputHandler internal) {
			this.internal = internal;
		}
		@Override
		public void pushOutput(String string) {
			internal.pushOutput(string);
			
		}

		@Override
		public void outputFinished() {
			internal.outputFinished();
			
		}

		@Override
		public void allFinished() {
			//internal.allFinished(); //do not make this one_to_one allFinished
			
		}
		public void sendOneShotAllFinished(){
			internal.allFinished();
		}
	};
	private boolean initiated;
	private OutputWrapper outputWrapper;
	
	
	public OneShotTransformer() {
	//	this.setBranchType(BranchType.CASCADE);
		this.add(new Merger());
		
	}
	public void add(Transformer child) {		
		//childs are added before the merger
		this.add(child, this.getChilds().size()==0?0:this.getChilds().size()-1);
	}
	
	
	private void init(){
		this.outputWrapper = new OutputWrapper(this.getNonLocalOutputHandler()); 
		this.setOutputHandler(outputWrapper);
	}
	// NEW MODEL
	@Override
	protected void _pushString(String str){
		if (!initiated){
			init();
			initiated = true;
		}
		this.getOutputHandler().pushOutput(str);
	}
	@Override
	protected void _closeOneInput() {
		this.getOutputHandler().outputFinished();
		
		//make childs believe that the output is finished (ex: internal mergers should output!)
		//however, in order to avoid an allFinsihed call from the one_to_one to next transformers,
		//we have implemented an OutputWrapper
		this.getOutputHandler().allFinished(); 
		
	}
	
	
	@Override
	protected void _closeAllInputs() {
		this.outputWrapper.sendOneShotAllFinished();
	}

}
