package sk.fiit.dp.refactor.model;

import java.util.List;

/**
 * Vstup pre expertny system
 * 
 * @author Lukas
 *
 */
public class JessInput {
	private String code;

	private String refCode;

	private List<String> parents;

	private int size;

	private String position;
	
	private String xpatPosition;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRefCode() {
		return refCode;
	}

	public void setRefCode(String name) {
		this.refCode = name;
	}

	public List<String> getParents() {
		return parents;
	}

	public void setParents(List<String> parents) {
		this.parents = parents;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getXpatPosition() {
		return xpatPosition;
	}

	public void setXpathPosition(String xpatPosition) {
		this.xpatPosition = xpatPosition;
	}
}
