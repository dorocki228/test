package  l2s.Phantoms.objects;

import  l2s.Phantoms.enums.TaskValidation;

public class RouteTargetCheck
{
	public TaskValidation tvalid; 
	public int id;
	
	public RouteTargetCheck(TaskValidation _tvalid, int _id)
	{
		tvalid = _tvalid;
		id= _id;
	}
	
}
