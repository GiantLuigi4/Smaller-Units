package tfc.smallerunits.utils.accessor;

import tfc.smallerunits.utils.tracking.data.SUDataTracker;

import java.util.UUID;

public interface SUTracked {
	SUDataTracker SmallerUnits_getTracker();
	
	boolean SmallerUnits_hasFinished();
	
	boolean SmallerUnits_setTracking(UUID entity);
	
	void SmallerUnits_setHasFinished(boolean b);
}
