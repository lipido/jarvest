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

import java.io.Serializable;
import java.util.List;

public interface Transformer extends Serializable,Cloneable{
	
	
	public static final BranchType DEFAULT_BRANCH_TYPE = BranchType.CASCADE;
	public static final MergeMode DEFAULT_BRANCH_MERGE_MODE = MergeMode.ORDERED;
	
	
	// Run Transformers
	public String[] apply(String[] source);
	
	public void pushString(String str);
	public void closeOneInput();
	public void closeAllInputs();	
	public void setOutputHandler(OutputHandler handler);
	
	
	public List<Transformer> getChilds();
	public void add(Transformer child);
	public void add(Transformer child, int index);
	
	public void remove(int index);

	public void setBranchMergeMode(MergeMode mode);
	public MergeMode getBranchMergeMode();
	
	public void setBranchType(BranchType mode);
	public BranchType getBranchType();
	
	public String getDescription();
	public void setDescription(String description);
	
	public void setInputFilter(String filter);
	public String getInputFilter();
	public boolean isLoop();
	public void setLoop(boolean loop);
	
	
	
	public void stop();
	public boolean isStopped();
	
	public Object clone();
}
