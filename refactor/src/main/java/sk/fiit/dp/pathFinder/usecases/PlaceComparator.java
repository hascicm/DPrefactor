package sk.fiit.dp.pathFinder.usecases;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;

public class PlaceComparator {
	
	public static List<LocationPart> findCommonDestinationPath(List<LocationPart> listA, List<LocationPart> listB){
		
		List<LocationPart> commonPath = new ArrayList<LocationPart>();
		
		if(listA == null || listB == null){
			return commonPath;
		}
		
		List<LocationPart> shorterList = listA.size() > listB.size() ? listB : listA;
		List<LocationPart> otherList = shorterList == listA ? listB : listA; 
		
		int count = 0;
		for(LocationPart locationPart : shorterList){
			if(locationPart.getLocationPartType() != otherList.get(count).getLocationPartType() 
					|| !(locationPart.getId().equals(otherList.get(count).getId()))){
				break;
			}
			
			commonPath.add(locationPart);
			count++;
		}
	
		return commonPath; 
	}
}
