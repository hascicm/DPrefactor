package sk.fiit.dp.pathFinder.configuration;

import java.util.ArrayList;
import java.util.List;

import sk.fiit.dp.pathFinder.entities.Location;
import sk.fiit.dp.pathFinder.entities.LocationPart;
import sk.fiit.dp.pathFinder.entities.LocationPartType;
import sk.fiit.dp.refactor.model.JessInput;

public class LocationProcessor {
	
	public static List<Location> processLocationString(String locationString) {
		List<Location> locationList = new ArrayList<Location>();
		List<LocationPart> locationParts = new ArrayList<LocationPart>();
		String[] strLocations = locationString.split("::");
		for (String str : strLocations) {
			if (!str.trim().isEmpty()) {
				locationParts.addAll(processStringToLocationPart(str));
			}
		}
		locationList.add(new Location(locationParts));
		return locationList;
	}

	private static List<LocationPart> processStringToLocationPart(String s) {
		String[] strParts = s.split(":");
		String type = strParts[0];
		String id = strParts[1];
		LocationPartType locationPartType = null;
		if (type.equals("CC")) {
			return ProcessPackageString(id);
		} else if (type.equals("C")) {
			locationPartType = LocationPartType.CLASS;
		} else if (type.equals("M")) {
			locationPartType = LocationPartType.METHOD;
		} else if (type.equals("NODE")) {
			locationPartType = LocationPartType.NODE;
		} else if (type.equals("A")) {
			locationPartType = LocationPartType.ATTRIBUTE;
		} else if (type.equals("P")) {
			locationPartType = LocationPartType.PARAMETER;
		} else if (type.equals("POS")) {
			locationPartType = LocationPartType.POSITION;
		}

		List<LocationPart> list = new ArrayList<LocationPart>();
		list.add(new LocationPart(locationPartType, id));
		return list;
	}

	private static List<LocationPart> ProcessPackageString(String packageStr) {
		List<LocationPart> list = new ArrayList<LocationPart>();
		String[] packages = packageStr.split("\\.");
		for (String s : packages) {
			list.add(new LocationPart(LocationPartType.PACKAGE, s));
		}
		return list;
	}
}
