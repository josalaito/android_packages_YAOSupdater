package org.arasthel.yaos.utils;

import java.util.ArrayList;
import java.util.Date;

import org.arasthel.yaos.online.OnlineJSON;

public class Update implements Comparable<Update> {

	protected String name;
	protected String filename;
	protected Date date;
	protected String descUrl;
	protected ArrayList<Integer> version;
	
	public Update(String name, String filename, String descUrl,String versionStr){
		this.name = name;
		this.filename = filename;
		this.descUrl = descUrl;
		this.version = OnlineJSON.getVersionFromString(versionStr, ".");
	}
	
	public boolean isURL(){
		return descUrl.substring(0, 4).contains("url:");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescUrl() {
		return descUrl;
	}

	public void setDescUrl(String descUrl) {
		this.descUrl = descUrl;
	}

	public ArrayList<Integer> getVersion() {
		return version;
	}

	public void setVersion(ArrayList<Integer> version) {
		this.version = version;
	}
	
	@Override
	public int compareTo(Update another) {
		if(this.version.size() == another.getVersion().size()){
			return compareVersions(0, this.version, another);
		}else{
			if(version.size() > another.getVersion().size())
				return compareVersions(0, version.size()-1, this.version, another);
			else
				return compareVersions(0, another.getVersion().size()-1, this.version, another);
		}
	}
	
	protected int compareVersions(int i, ArrayList<Integer> version, Update another){
		if(version.get(i) > another.getVersion().get(i))
			return 1;
		else if(version.get(i) < another.getVersion().get(i))
			return -1;
		else	
			if(i == version.size()-1)
			    return 0;
			return compareVersions(i+1, version, another);
	}
	
	protected int compareVersions(int i, int max, ArrayList<Integer> version, Update another){
		if(version.get(i) > another.getVersion().get(i))
			return 1;
		else if(version.get(i) < another.getVersion().get(i))
			return -1;
		else{
			if(i == max-1){
				if(version.size() > another.getVersion().size()){
					return 1;
				}
				return 0;
			}else
				return compareVersions(i+1, max, version, another);
		}
	}
	
	public String getVersionStr(){
		String versionStr = "";
		for (int i = 0; i < this.version.size(); i++) {
			if(i == this.version.size()-1)
				versionStr += version.get(i);
			else
				versionStr += version.get(i)+".";
		}
		return versionStr;
	}
	
}
