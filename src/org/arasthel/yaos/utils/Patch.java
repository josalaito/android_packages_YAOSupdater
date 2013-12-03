package org.arasthel.yaos.utils;

import java.util.ArrayList;

import org.arasthel.yaos.online.OnlineJSON;

public class Patch extends Update {
	
	private ArrayList<Integer> maxVersion;

	public Patch(String name, String filename, String descUrl, String maxVersion, String minVersion) {
		super(name, filename, descUrl, minVersion);
		this.maxVersion = OnlineJSON.getVersionFromString(maxVersion, ".");
	}

	public ArrayList<Integer> getMaxVersion() {
		return maxVersion;
	}

	public void setMaxVersion(ArrayList<Integer> maxVersion) {
		this.maxVersion = maxVersion;
	}

	public boolean aplicable(Update update){
		//El parche es aplicable siempre que la versión actual se encuentre entre el máximo y el mínimo incluídos ambos
		if((minEsMenor(0, update))&&(maxEsMayor(0, update))){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean minEsMenor(int i, Update update){
		if(i >= version.size() || i >= update.getVersion().size()){
			return true;
		}
		if(version.get(i) < update.getVersion().get(i)){
			return true;
		}else if(version.get(i) == update.getVersion().get(i)){
			if(i == version.size()-1){
				return true;
			}else{
				return minEsMenor(i+1, update);
			}
		}
		return false;
	}
	
	private boolean maxEsMayor(int i, Update update){
		if(maxVersion.get(i) > update.getVersion().get(i)){
			return true;
		}else if(maxVersion.get(i) == update.getVersion().get(i)){
			if(i == maxVersion.size()-1){
				return true;
			}else{
				return maxEsMayor(i+1, update);
			}
		}
		return false;
	}
	
	@Override
	public String getVersionStr(){
		String versionStr = "";
		for (int i = 0; i < this.maxVersion.size(); i++) {
			if(i == this.maxVersion.size()-1)
				versionStr += maxVersion.get(i);
			else
				versionStr += maxVersion.get(i)+".";
		}
		return versionStr;
	}
}
