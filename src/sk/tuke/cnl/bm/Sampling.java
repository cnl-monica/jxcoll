/* 
* Copyright (C) 2010 Michal Kascak
*
* This file is part of JXColl.
*
* JXColl is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.

* JXColl is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with JXColl; If not, see <http://www.gnu.org/licenses/>.
*/

package sk.tuke.cnl.bm;

/**
 * Nepouziva sa momentalne
 * @deprecated
 */
@Deprecated
public class Sampling {
	
	private int algorithm = 0;
	private int param1 = 0;
	private int param2 = 0;
	
        @Deprecated
	public Sampling(int alg, int param1, int param2){
		this.algorithm = alg;
		this.param1 = param1;
		this.param2 = param2;
	}
        
        @Deprecated
        public Sampling(){
        }
	
	public int getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}
	public int getParam1() {
		return param1;
	}
	public void setParam1(int param1) {
		this.param1 = param1;
	}
	public int getParam2() {
		return param2;
	}
	public void setParam2(int param2) {
		this.param2 = param2;
	}
	
	public String toString(){
          return Integer.toString(algorithm) + " " + Integer.toString(param1) + " " + Integer.toString(param2);
        }
}
