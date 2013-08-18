package org.zephyrsoft.ibancalc.model;

import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.Record;

/**
 * A bank's basic data.
 * 
 * @author Mathis Dirksen-Thedens
 */
@Record
public class Bank {
	
	private String name;
	private String blz;
	private String bic;
	
	@Field(offset = 10, length = 58)
	public String getName() {
		return name;
	}
	
	@Field(offset = 1, length = 8)
	public String getBlz() {
		return blz;
	}
	
	@Field(offset = 140, length = 11)
	public String getBic() {
		return bic;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setBlz(String blz) {
		this.blz = blz;
	}
	
	public void setBic(String bic) {
		this.bic = bic;
	}
	
	public boolean isFilled() {
		return name != null && !name.isEmpty() && blz != null && !blz.isEmpty() && bic != null && !bic.isEmpty();
	}
	
}
