package tfc.smallerunits.utils.accessor;

import tfc.smallerunits.utils.tracking.data.SUDataTracker;

public interface SUTracked {
	SUDataTracker SmallerUnits_getTracker();
	
	boolean SmallerUnits_hasFinished();
	
	void SmallerUnits_setHasFinished(boolean b);
}
