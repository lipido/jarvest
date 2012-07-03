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

import static es.uvigo.ei.sing.jarvest.core.BranchType.BRANCH_DUPLICATED;
import static es.uvigo.ei.sing.jarvest.core.BranchType.BRANCH_SCATTERED;
import static es.uvigo.ei.sing.jarvest.core.BranchType.CASCADE;
import static es.uvigo.ei.sing.jarvest.core.MergeMode.COLLAPSED;
import static es.uvigo.ei.sing.jarvest.core.MergeMode.ORDERED;
import static es.uvigo.ei.sing.jarvest.core.MergeMode.SCATTERED;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public abstract class AbstractTransformer extends Observable implements Transformer{

	
	
	
	private Vector<Transformer> childs = new Vector<Transformer>();
	private MergeMode branchMergeMode = ORDERED;
	private BranchType branchType = CASCADE;
	private boolean loop=false;
	private String description = this.getClass().getSimpleName();
	private String inputFilter ="";
	
	private Vector<Filter> inputFilters = new Vector<Filter>();
	/**
	 *	Serial Version UID 
	 */
	private static final long serialVersionUID = 1L;

	public void add(Transformer child) {
		this.add(child, this.childs.size());
	}
	
	public void add(Transformer child, int index){
		childs.insertElementAt(child, index);
		this.setChanged();
		this.notifyObservers();
	}
	
	public List<Transformer> getChilds(){
		return this.childs;
	}
	
	@Override
	public Object clone(){
		
		
		try {
//			 field-by-field assignment (the specific transformers have only fields that can be cloned by simple assignment, because
			// they are String, or primitive types)
			AbstractTransformer _clone = (AbstractTransformer) super.clone();
			// clone the vectors
			_clone.childs = new Vector<Transformer>();
			for (Transformer t : this.childs){
				_clone.childs.add((Transformer)t.clone());
			}		
			_clone.inputFilters = new Vector<Filter>();
			for (Filter f : this.inputFilters){
				_clone.inputFilters.add((Filter)f.clone());
			}
			
			_clone.stopped = false;
			_clone.initiated = false;
			//System.out.println("Cloning "+this+" clone: "+_clone);
			return _clone;
			
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		
		
	}

//=================================
//	NEW EXECUTION MODEL
//=================================

	private HashMap<Transformer, LinkedList<String>> childQueues;
	private HashSet<Transformer> finishedChilds;
	private void initChildOutputQueues(){
		childQueues = new HashMap<Transformer, LinkedList<String>>();
		finishedChilds = new HashSet<Transformer>();
		for (Transformer child: childs){
			childQueues.put(child, new LinkedList<String>());
		}
	}
	// an output handler that redirects to other transformer
	private class ConnectedOutputHandler implements OutputHandler{
		
		private Transformer nextTransformer;
		public ConnectedOutputHandler(Transformer next){
			this.nextTransformer = next;
		}
		public void allFinished() {
			nextTransformer.closeAllInputs();
		}
		
		public void outputFinished() {
			nextTransformer.closeOneInput();
		}
		
		public void pushOutput(String string) {
			//System.err.println("connected transformer: pushing "+string+" to "+nextTransformer);
			nextTransformer.pushString(string);	
		}	
	}
	private boolean initiated = false;
	private boolean loopFinished = false;
	private void init(){
		//System.out.println("initiating "+this);
	/*	try{
			throw new RuntimeException();
		}catch(Exception e){
			e.printStackTrace();
		}*/
		currentString = "";
		this.localOutputHandler = new LocalOutputHandler();
		generalOutputHandler = new NumberedOutputHandler();
		currentInput = 0;
		stoppedInputs.clear(); stoppedInputs.add(null);
		resolvedParameters = false;
		initChildOutputQueues(); //only useful in BRANCHED modes
		if (isLoop()){
			childs.get(0).setOutputHandler(new ConnectedOutputHandler(this){
				boolean anyOutput = false;
				
				@Override
				public void pushOutput(String str){
					anyOutput = true;
				
					super.pushOutput(str);
				}
				@Override
				public void outputFinished() {
				
					super.outputFinished();
				}
				@Override
				public void allFinished(){
				
					if (!anyOutput){ 
						loopFinished=true;
					}
					anyOutput=false;
					super.allFinished();
				}
			}); //redirect the loop control to me
			
		}
		int firstChild = 0; //handle loops
		if (isLoop()){
			firstChild = 1;
		}
		if (childs.size()>firstChild){			
			for (int i = firstChild; i< childs.size(); i++){
				final int j = i;
				
				//t.setOutputHandler(new ConnectedOutputHandler(childs.get(i+1)));
				childs.get(i).setOutputHandler(new OutputHandler(){
					
					public void allFinished() {
						generalOutputHandler.allFinished(j);		
					}
					
					public void outputFinished() {
						generalOutputHandler.outputFinished(j);	
					}
					
					public void pushOutput(String string) {
						generalOutputHandler.pushOutput(j, string);
					}
					
				});
			}
			//last child if it exists, forwards outputs to the general output transformer					
		}
	}
	
	// deals with the different merge modes creating the output in the correct order and compactness
	private class NumberedOutputHandler{
		
		private void enQueue(int childNumber, String string){
			LinkedList<String> queue = childQueues.get(childs.get(childNumber)); 
			
			if (queue.size()==0){
//				System.err.println("Queuing in IN EMPTY QUEUE in child "+childNumber+" string "+string);
				queue.offer(string);
			}else{
				
				String currentString = queue.get(queue.size()-1); //the string in the last position
				String toAdd=null;
				if (currentString == null){ 
					toAdd = string;
//					System.err.println("Queuing in ADDING (Null) in child "+childNumber+" string "+string);
				}
				else{
//					System.err.println("Queuing in ADDING in child "+childNumber+" string "+string);
					toAdd=currentString+string;
				}
				queue.remove(queue.size()-1);
				
				queue.add(toAdd);
				
			}

		}
		int currentOutput = 0; // the child we are expecting
		public NumberedOutputHandler(){
			resetCurrent();
		}
		
		private void resetCurrent(){
			if (isLoop()){
				currentOutput = 1;
			}else{
				currentOutput = 0;
			}
			
		}
		public void pushOutput(int childNumber, String string) {
			if (branchType == CASCADE){
				if (childNumber<childs.size()-1){
					childs.get(childNumber+1).pushString(string);
				}else{
					outputHandler.pushOutput(string);
				}
				
			}
			else if (branchMergeMode == ORDERED){
				if (childNumber == currentOutput){
					outputHandler.pushOutput(string);
				}else{
					// queue it!
					enQueue(childNumber, string);
				}
			}
			
			else if (branchMergeMode == SCATTERED){
				if (childNumber == currentOutput){
					outputHandler.pushOutput(string);
				}else{
					//queue it!
					
					enQueue(childNumber, string);
				}
			}
			else if (branchMergeMode == COLLAPSED){
				if (childNumber == currentOutput){
					outputHandler.pushOutput(string);
				}else{
					//queue it!
					
					enQueue(childNumber, string);
				}
			}
			
		}
		public void outputFinished(int childNumber) {
			if (branchType == CASCADE){
				if (childNumber<childs.size()-1){
					childs.get(childNumber+1).closeOneInput();
				}else {
					if (branchMergeMode != COLLAPSED){
						outputHandler.outputFinished();
					}
				}
			}
			else if (branchMergeMode == ORDERED){
				if (childNumber == currentOutput){
					outputHandler.outputFinished();
					while (childQueues.get(childs.get(childNumber)).size()>1){
						outputHandler.pushOutput(childQueues.get(childs.get(childNumber)).poll());
						outputHandler.outputFinished();
					
					}
				}else{
					childQueues.get(childs.get(childNumber)).offer(null);
				}
			}
			else if (branchMergeMode == SCATTERED){
				if (childNumber == currentOutput){
					outputHandler.outputFinished();
					childQueues.get(childs.get(childNumber));
					
					//get 1 per queue until a queue is empty
					int child=(childNumber+1)%(childs.size());
					if (child == 0 && isLoop()) child = 1;
					LinkedList<String> queue = childQueues.get(childs.get(child)); 
					while (queue.size()>0 ){
						if (finishedChilds.contains((childs.get(child)))){
							child = (child+1)%(childs.size()-1);
							if (isLoop() && child ==0) child = 1;
							queue = childQueues.get(childs.get(child));
							continue;
						}
						if (queue.size()==1){
							//the item has not finished, we will flush the current content, and wait until it finishes
							if (queue.peek()!=null) outputHandler.pushOutput(queue.peek());
							queue.remove(0);
							queue.add(0, "");
							currentOutput = child;
							break;
						}else{
							// the item has finished, because there is more than one in the queue
							outputHandler.pushOutput(queue.poll());
							outputHandler.outputFinished();
							
						}
						child = (child+1)%(childs.size());
						if (isLoop() && child ==0) child = 1;
						queue = childQueues.get(childs.get(child));
					}
					currentOutput = child;
				}else{
					childQueues.get(childs.get(childNumber)).offer(null);
				}
			}
			else if (branchMergeMode == COLLAPSED){
				if (childNumber == currentOutput){
					//outputHandler.outputFinished(); // SINCE WE ARE COLLAPSED
					childQueues.get(childs.get(childNumber));
					
					//get 1 per queue until a queue is empty
					int child=(childNumber+1)%(childs.size());
					if (child == 0 && isLoop()) child = 1;
					LinkedList<String> queue = childQueues.get(childs.get(child)); 
					while (queue.size()>0 ){
						if (finishedChilds.contains((childs.get(child)))){
							child = (child+1)%(childs.size()-1);
							if (isLoop() && child ==0) child = 1;
							queue = childQueues.get(childs.get(child));
							continue;
						}
						if (queue.size()==1){
							//the item has not finished, we will flush the current content, and wait until it finishes
							if (queue.peek()!=null) outputHandler.pushOutput(queue.peek());
							queue.remove(0);
							queue.add(0, "");
							currentOutput = child;
							break;
						}else{
							// the item has finished, because there is more than one in the queue
							outputHandler.pushOutput(queue.poll());
							//outputHandler.outputFinished();  //SINCE IT IS COLLAPSED
							
						}
						child = (child+1)%(childs.size());
						if (isLoop() && child ==0) child = 1;
						queue = childQueues.get(childs.get(child));
					}
					currentOutput = child;
				}else{
					childQueues.get(childs.get(childNumber)).offer(null);
				}
			}
			
		}
		public void allFinished(int childNumber) {
			if (branchType == CASCADE){
				if (childNumber<childs.size()-1){
					childs.get(childNumber+1).closeAllInputs();
				}else{
					if (branchMergeMode == COLLAPSED){ 
						outputHandler.outputFinished(); 
						outputHandler.allFinished(); 
					}
					else
						outputHandler.allFinished();
					
				}
			}
			else if (branchMergeMode == ORDERED){
				finishedChilds.add(childs.get(childNumber));
				if (childNumber == currentOutput){
					
					// go to next childs
					int child = childNumber;
					
					do{
						child = child+1;
						if (child == childs.size()){
							
							break;
						}
						
						LinkedList<String> queue = childQueues.get(childs.get(child));
						while (queue.size()>1){
							outputHandler.pushOutput(queue.poll());
							outputHandler.outputFinished();
						}
						if (queue.size()==1 && queue.peek()!=null){
							outputHandler.pushOutput(queue.poll());
							queue.offer(null);
						}
					}while (finishedChilds.contains(childs.get(child)));
					currentOutput = child;
					
					
					
				}
				if ((isLoop() && loopFinished==true && finishedChilds.size() == childs.size()-1) || (!isLoop() && finishedChilds.size() == childs.size())){
					currentOutput = 0; //added 14/5/08
					outputHandler.allFinished();
				}
				
			}
			else if (branchMergeMode == SCATTERED){
				
				finishedChilds.add(childs.get(childNumber));
				
				if ((isLoop() && loopFinished==true && finishedChilds.size() == childs.size()-1) || (!isLoop() && finishedChilds.size() == childs.size())){
					currentOutput = 0;//added 14/5/08
					outputHandler.allFinished();
				}
				
			}else if (branchMergeMode == MergeMode.COLLAPSED){
				finishedChilds.add(childs.get(childNumber));
				if (childNumber == currentOutput){
					
					// go to next childs
					int child = childNumber;
					
					do{
						child = child+1;
						if (child == childs.size()) break;
						
						LinkedList<String> queue = childQueues.get(childs.get(child));
						while (queue.size()>1){
							outputHandler.pushOutput(queue.poll());
							// outputHandler.outputFinished(); //SINCE IT IS COLLAPSED
						}
						if (queue.size()==1 && queue.peek()!=null){
							outputHandler.pushOutput(queue.poll());
							queue.offer(null);
						}
					}while (finishedChilds.contains(childs.get(child)));
					currentOutput = child;
					
					
				}
				if ((isLoop() && loopFinished==true && finishedChilds.size() == childs.size()-1) || (!isLoop() && finishedChilds.size() == childs.size())){
					currentOutput = 0;//added 14/5/08
					outputHandler.outputFinished();
					outputHandler.allFinished();
				}
			}
			
			
			
		}


		
	};
	private NumberedOutputHandler generalOutputHandler;
	
	
	private String currentString="";
	protected String getAndClearCurrentString(){
		String copy = currentString;
		currentString = "";
		return copy;
	}
	private int currentInput;
	
	ArrayList<String> stoppedInputs = new ArrayList<String>(); //stopped inputs because some parameters has an %%number%% specified and the input didn't arrived yet
	public final void pushString(String str){
		synchronized (this.stopped){
			if (this.stopped) return;
		}
//		System.err.println(this.description+" | pushed: "+str);
		
		
		//expand parameters
		//filter inputs
		if (!initiated){
			init();
			initiated = true;
		}
		
		
		if (tryResolveParameters()){
			if (isFiltered(currentInput)){
				return;
			}
			
			currentString+=str;
			this._pushString(str);
		}else{
			if (stoppedInputs.get(stoppedInputs.size()-1) == null){
				stoppedInputs.set(stoppedInputs.size()-1, str);
				
			}else{
				stoppedInputs.set(stoppedInputs.size()-1, stoppedInputs.get(stoppedInputs.size()-1)+str);
			}
		}
	}
	public final void closeOneInput(){
		synchronized (this.stopped){
			if (this.stopped) return;
		}
//		System.err.println(this.description+" | closed one input");
		
		boolean resolvedPrior = resolvedParameters;
		if (!tryResolveParameters()){
			stoppedInputs.add(null);
		}
		if (tryResolveParameters()){
			if (!resolvedPrior){
				//dump previous inputs
				for (int i = 0; i<stoppedInputs.size()-1; i++){
					if (!isFiltered(i)){
						this.pushString(stoppedInputs.get(i));
						this._closeOneInput();
					}
					currentInput++;
				}
			}
			else if (!isFiltered(currentInput)){
				this._closeOneInput();
				// currentString=""; //comentted  14/5/08
				
			}
			currentInput++;
		}
	}
	
	
	int closeAllCount = 0;
	public final void closeAllInputs(){
		synchronized (this.stopped){
			if (this.stopped) return;
		}
		
//		System.err.println(this.description+" | closed all");
		// IF I AM A LOOP, I FINISH ONLY WHEN THE FIRST CHILD DOESN'T GIVES ANYTHING, THERE WILL BE TWO CALLS TO CLOSE		
		if (!resolvedParameters && !tryResolveParameters()){
			throw new RuntimeException("input finished before resolved parameters");
		}else{
			try{
			closeAllCount++;
		
			/*if (isLoop() && closeAllCount == 2){
				
				this._closeAllInputs();
				this.initiated=false;
			}else if (isLoop()){
				this._closeAllInputs();
			}else if (!isLoop()){
				this._closeAllInputs();
				this.initiated=false;
			}*/
			this._closeAllInputs();
			}finally{
				this.resetParameters();
				this.initiated=false;
				
			}
		}
	}
	
	private boolean resolvedParameters = false;
	private boolean tryResolveParameters() {
		if (resolvedParameters) return true;
		Pattern pattern = Pattern.compile("%%[0-9]+?%%");
		boolean allResolved = true;
		try{
			BeanInfo info = java.beans.Introspector.getBeanInfo(this.getClass());
			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()){
				Method readM = descriptor.getReadMethod();
				Method writeM = descriptor.getWriteMethod();
				
				if (readM != null && writeM !=null && descriptor.getPropertyType().equals(String.class)){
					String lastValue = (String) readM.invoke(this, (Object[]) null);
					if(lastValues.get(descriptor)==null) lastValues.put(descriptor, lastValue);
					else continue;
					//expand expression
					boolean resolved=true;
					Matcher matcher = pattern.matcher(lastValue);
					while(matcher.find() && resolved){
						int start = matcher.start();
						int end = matcher.end();
						String number = lastValue.substring(start+2,end-2);
						System.err.println("Number: "+number);
						int position = Integer.parseInt(number);
						if (stoppedInputs.size()-1>position){
							lastValue = lastValue.substring(0,start) + stoppedInputs.get(position) + lastValue.substring(end);
						}else{				
							allResolved = false;
							resolved=false;
						}
						matcher = pattern.matcher(lastValue);
					}
					
					if (resolved) writeM.invoke(this, new Object[]{lastValue});
					else lastValues.remove(descriptor); 
				}
				
			}
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resolvedParameters = allResolved;
		return allResolved;
		
	}
	private OutputHandler outputHandler;
	public final void setOutputHandler(OutputHandler handler){
		this.outputHandler = handler;
		if (!this.initiated){
			this.init();
			this.initiated = true;
		}
	}
	
//	 gets the input from my subclass and deals with the branching modes and childs
	private class LocalOutputHandler implements OutputHandler {
		private int currentInput=0; //the output from my self
		public void pushOutput(String string) {
			// to childs...
			// handle loops...
			//
			int firstChild = 0; //when loops, it will be 1
			
			
			if (isLoop()){
				firstChild = 1;
			}
			
			if (childs.size()>0){
				if (branchType == CASCADE){
					childs.get(firstChild).pushString(string);
				}
				if (branchType == BRANCH_DUPLICATED){
					for (int i = firstChild; i<childs.size(); i++){
						childs.get(i).pushString(string);
					}
				}
				if (branchType == BRANCH_SCATTERED){
					finishedChilds.remove(childs.get((currentInput % childs.size())+firstChild));
					childs.get((currentInput % childs.size())+firstChild).pushString(string);
					
				}
			}
			else{
				
				outputHandler.pushOutput(string);				
			}
			if (isLoop()){
				childs.get(0).pushString(string);
				
			}
			
			
		}
		public void outputFinished() {
			
			
			int firstChild = 0; //when loops, it will be 1
			if (isLoop()){
				firstChild = 1;
			}
			if (childs.size()>0 ){ 
				if (branchType == CASCADE){
					childs.get(firstChild).closeOneInput();					
				}
				if (branchType == BRANCH_DUPLICATED){
					for (int i = firstChild; i<childs.size(); i++){
						childs.get(i).closeOneInput();
					}
				}
				if (branchType == BRANCH_SCATTERED){
					childs.get((currentInput % childs.size())+firstChild).closeOneInput();
					childs.get((currentInput % childs.size())+firstChild).closeAllInputs(); // CLOSING EACH CHILD IN EACH INPUT
				}
			}else{
				if (branchMergeMode != COLLAPSED)
					outputHandler.outputFinished();
			}
			if (isLoop()){
				childs.get(0).closeOneInput();
			}
			
			currentInput++;
		}
		public void allFinished() {
			int firstChild = 0; //when loops, it will be 1
			if (isLoop()){
				firstChild = 1;
			}
			if (childs.size()>0 ){
				if (branchType == CASCADE){
						childs.get(firstChild).closeAllInputs();
					}
				
				if (branchType == BRANCH_DUPLICATED){
					for (int i = firstChild; i<childs.size(); i++){
						childs.get(i).closeAllInputs();
					}
				}
				if (branchType == BRANCH_SCATTERED){
					for (int i = firstChild; i<childs.size(); i++){
						//do nothing because we close childs in each input
//						childs.get(i).closeAllInputs();
					}
					//childs.get((currentInput % childs.size())+firstChild).closeAllInputs();
				}
			}
			else{
				if (branchMergeMode == COLLAPSED){
					outputHandler.outputFinished();
				}
				outputHandler.allFinished();
			}
			if (isLoop() && loopFinished==false){
				childs.get(0).closeAllInputs(); //------------------------------------------------------------------------
			}
		}
	}
	
	private OutputHandler localOutputHandler = null;
	protected OutputHandler getOutputHandler(){
		return this.localOutputHandler;
	}
	
	
	protected void _pushString(String str){
		
	}
	protected abstract void _closeOneInput();
	protected void _closeAllInputs(){
		this.getOutputHandler().allFinished();
	}
//==================================
//	END NEW EXECUTION MODEL
//=================================	
	public final String[] apply(String[] source) {
		expandParameters(source);
		
		//System.out.println("Starting: "+this.getDescription());
		
		if (source==null){
			source=new String[]{""};
		}
		String[] toret;
		
		source = filterSource(source);
		
		String[] partialRes = this._apply(source);
		

		Transformer loopChild = null;
		
		int iteration = 0;
		if (this.isLoop()){
			if (this.childs.size()==0){
				throw new RuntimeException("A loop transformer must have a child");
			}
			loopChild = this.childs.get(0);
			this.childs.remove(0);
		}
		
		Vector<String> iterationsTotalRes = new Vector<String>();
		
		while (true){ //if the operator is not a loop, this loop makes only one iteration
			
			if (iteration !=0){
				String[] childLoopOpinion = loopChild.apply(partialRes);
				
				// must have a cancel......
				
				if (childLoopOpinion.length==0) break;
				
				partialRes = this._apply(childLoopOpinion);
			}
			
			
		
			//prepare iteration
			Vector<String[]> childRes = new Vector<String[]>();			
			Vector<String> totalResVector = new Vector<String>();
			if (!childs.isEmpty()){
				if (this.branchType == BRANCH_DUPLICATED){
					for(Transformer child : childs){
						childRes.add(child.apply(partialRes));
					}
				}else if (this.branchType == BRANCH_SCATTERED){
					int childCounter = 0;
					for (String partialString : partialRes){
						childRes.add(this.childs.get(childCounter%this.childs.size()).apply(new String[]{partialString}));
					}
					
					
				}else if (this.branchType == CASCADE){
					String[] cascadeStep = partialRes;
					for(Transformer child : childs){
					
						cascadeStep = child.apply(cascadeStep);
					}
					totalResVector = new Vector<String>();
					for (String child : cascadeStep){
							totalResVector.add(child);
					}
				}
		
				if (this.branchType != CASCADE){
					switch(this.branchMergeMode){
						case SCATTERED:
							int counter = 0;
							boolean finished=false;
							while (!finished){
								finished=true;
								
								
								for (String[] child : childRes){
									if (child.length>counter){
										finished=false;
										totalResVector.add(child[counter]);
									}
								}
								counter++;	
							}
							break;
						case ORDERED:
							for (String[] child : childRes){
								for (String childString : child){
									totalResVector.add(childString);
								}					
							}
							break;
						case COLLAPSED:
							String collapsedString = "";
							for (String[] child : childRes){
								for (String childString : child){
									collapsedString+=childString;
								}					
							}
							totalResVector.add(collapsedString);
							break;
							
					}
				}else{
					
				}
				
				iterationsTotalRes.addAll(totalResVector);
				
			}else{
				for (String partial: partialRes){
					iterationsTotalRes.add(partial);
				}
			}
			if (!this.isLoop()) break;
			else{
				iteration++;
			}
		}
//		 convert the vector to String[]
		
		toret = new String[iterationsTotalRes.size()];
		int counter = 0;
		for (String result: iterationsTotalRes){
			toret[counter++] = result;
		}
		
		if (this.isLoop()){
			this.childs.insertElementAt(loopChild,0);
		}
		
		//System.out.println("Finished: "+this.getDescription());
		
		resetParameters();
		return toret;
	}

	
	

	



	private HashMap<PropertyDescriptor, String> lastValues = new HashMap<PropertyDescriptor, String>();
	/**
	 * This method searches for all setter methods of this object and, if they are of type String,
	 * looks for %%<number>%% substrings, expanding them with the content of the source[<number>] string
	 * @param source
	 */
	private void expandParameters(String[] source) {
		Pattern pattern = Pattern.compile("%%[0-9]+?%%");
		try{
			BeanInfo info = java.beans.Introspector.getBeanInfo(this.getClass());
			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()){
				Method readM = descriptor.getReadMethod();
				Method writeM = descriptor.getWriteMethod();
				
				if (readM != null && writeM !=null && descriptor.getPropertyType().equals(String.class)){
					String lastValue = (String) readM.invoke(this, (Object[]) null);
					lastValues.put(descriptor, lastValue);
					
					//expand expression
					Matcher matcher = pattern.matcher(lastValue);
					while(matcher.find()){
						int start = matcher.start();
						int end = matcher.end();
						String number = lastValue.substring(start+2,end-2);
						System.err.println("Number: "+number);
						int position = Integer.parseInt(number);
						if (source.length>position){
							lastValue = lastValue.substring(0,start) + source[position] + lastValue.substring(end);
						}
						matcher = pattern.matcher(lastValue);
					}
					
					writeM.invoke(this, new Object[]{lastValue});
				}
				
			}
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void resetParameters() {
		for (PropertyDescriptor desc : lastValues.keySet()){
			try {
				desc.getWriteMethod().invoke(this, lastValues.get(desc));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		lastValues = new HashMap<PropertyDescriptor, String>();
	}


	public void remove(int index) {
		if (childs.size()>index){
			childs.remove(index);
		}
		this.setChanged();
		this.notifyObservers();
	}
	
	protected abstract String[] _apply(String [] source);

	public void setBranchMergeMode(MergeMode mergeMode) {
		this.branchMergeMode = mergeMode;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * @param branchType the branchType to set
	 */
	public void setBranchType(BranchType branchType) {
		this.branchType = branchType;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * @return the branchType
	 */
	public BranchType getBranchType() {
		return branchType;
	}

	/**
	 * @return the mergeMode
	 */
	public MergeMode getBranchMergeMode() {
		return branchMergeMode;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * @return the loop
	 */
	public boolean isLoop() {
		return loop;
	}

	/**
	 * @param loop the loop to set
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	/**
	 * @return the inputFilter
	 */
	public String getInputFilter() {
		return inputFilter;
	}

	/**
	 * @param inputFilter the inputFilter to set
	 */
	public void setInputFilter(String inputFilter) {
		this.inputFilter = inputFilter;
		createFilters();
	}
	
	private String[] filterSource(String[] source) {
		Vector<String> _toret = new Vector<String>();
		for (int i = 0; i<source.length; i++){
			if (!isFiltered(i)){
				_toret.add(source[i]);
			}
		}
		String[] toret = new String[_toret.size()];
		_toret.toArray(toret);
		return toret;
	}

	private void createFilters(){
		String[] filters = this.inputFilter.split(",");
		this.inputFilters = new Vector<Filter>();
		for (String filterString : filters){
			String[] startEnd = filterString.split("-");
			if (startEnd.length==0)continue;
			try{
				Filter filter = new Filter();
				if (filterString.indexOf("-")==-1){
					// a simple port filter
					filter.setStart(Integer.parseInt(startEnd[0]));
					filter.setEnd(Integer.parseInt(startEnd[0]));
				}else if (filterString.indexOf("-")==0){
					//no begin, only end
					filter.setEnd(Integer.parseInt(startEnd[1]));
				}else if (filterString.indexOf("-")==filterString.length()-1){
					//begin and no end
					filter.setStart(Integer.parseInt(startEnd[0]));
					
				}else{
					//begin and end
					filter.setStart(Integer.parseInt(startEnd[0]));
					filter.setEnd(Integer.parseInt(startEnd[1]));
				}
				this.inputFilters.add(filter);
				
			}catch(NumberFormatException e){
				//nothing, we ignore this no filter is created
			}
		}
	}
	
	private boolean isFiltered(int inputPosition){
		for (Filter f : this.inputFilters){
			if (f.isFiltered(inputPosition)) return true;
		}
		return false;
	}
	private class Filter implements Cloneable{
		private int start=-1, end=Integer.MAX_VALUE;
		
		/**
		 * @param start the start to set
		 */
		public void setStart(int start) {
			this.start = start;
		}

		/**
		 * @param end the end to set
		 */
		public void setEnd(int end) {
			this.end = end;
		}

		boolean isFiltered(int i){
			return i>=start && i<=end;
		}
		@Override
		public Object clone(){
			
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
			
			
		}
	}
	
	public static void main(String args[]){
		Transformer t1 = new SimpleTransformer();
		Decorator c = new Decorator();
		c.setHead("hello");
		c.setTail("bye");
		t1.add(c);
		
		Transformer t_clone = (Transformer) t1.clone();
		for (Transformer t : t_clone.getChilds()){
			if (t instanceof Decorator){
				((Decorator)t).setTail(((Decorator)t).getTail()+"-bye");
			}
		}
		
		try {
			XMLInputOutput.writeTransformer(t1, new File("/home/lipido/Desktop/t1.xml"));
			XMLInputOutput.writeTransformer(t_clone, new File("/home/lipido/Desktop/t_clone.xml"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private Boolean stopped=false;
	public void stop() {
		synchronized(this.stopped){
			this.stopped = true;
		
			// recurse
			for (Transformer t : this.childs){
				t.stop();
			}
		}
		this.outputHandler.allFinished();
	}
	public boolean isStopped() {		
		return this.stopped;
	}
	
	/*=================
	 * Utils
	 =================*/
	public String restoreScapes(String text){
		
		String toret = text;
		toret = toret.replaceAll("\\\\t", "\t");
		toret = toret.replaceAll("\\\\n", "\n");
		return toret;
	}
}
