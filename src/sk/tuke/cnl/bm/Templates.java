/* 
* Copyright (C) 2010 Lubos Kosco, Pavol Benko
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
 * Trieda obsahujuca popisy jednotlivych atributov protokolu NetFlow9 a ich ID.
 */
public class Templates {

  public static final int IPV4_SRC_ADDR   = 8;     //zdrojova IPv4 adresa
  public static final int IPV4_DST_ADDR   = 12;    //cielova IPv4 adresa
  public static final int L4_SRC_PORT     = 7;     //zdrojovy port
  public static final int L4_DST_PORT     = 11;    //cielovy port
  public static final int FIRST_SWITCHED  = 22;    //cas prveho paketu zaradeneho do toku
  public static final int LAST_SWITCHED   = 21;    //cas posledneho paketu zaradeneho do toku
  public static final int IN_BYTES        = 1;     //pocet bytov prenesenych smerom dnu
  public static final int IN_PKTS         = 2;     //pocet paketov prenesenych smerom dnu
  public static final int OUT_BYTES       = 23;    //pocet bytov prenesenych smerom von
  public static final int OUT_PKTS        = 24;    //pocet paketov prenesenych smerom von
  public static final int TOTAL_BYTES_EXP = 40;    //celkovy pocet bytov
  public static final int TOTAL_PKTS_EXP  = 41;    //celkovy pocet paketov
  public static final int PROTOCOL        = 4;     //protokol

  public static final int IPV4_NEXT_HOP   = 15;
  public static final int INPUT_SNMP      = 10;
  public static final int OUTPUT_SNMP    = 14;
  public static final int TCP_FLAGS       = 6;
  public static final int SRC_TOS         = 5;
  public static final int SRC_AS          = 16;
  public static final int DST_AS          = 17;
  public static final int SRC_MASK        = 9;
  public static final int DST_MASK        = 13;

  public static final int IP_MP           = 0;     //IP adresa meracieho bodu (*)
  //(*) - nevyskytuje sa v netflow specifikacii => proprietarna hod. pre nasu aplikaciu

  /**
   * Pole s napevno nadefinovanymi sablonami. Index pola reprezentuje ID sablony.
   */
  public static final int[][] TEMPLATE = {
    { //sablona ID=0
      IP_MP, IPV4_SRC_ADDR, L4_SRC_PORT, IPV4_DST_ADDR, L4_DST_PORT, PROTOCOL, FIRST_SWITCHED, LAST_SWITCHED, IN_BYTES
    },
    { //sablona ID=1
      IP_MP, IPV4_SRC_ADDR, L4_SRC_PORT, IPV4_DST_ADDR, L4_DST_PORT, PROTOCOL, FIRST_SWITCHED, LAST_SWITCHED, IN_BYTES
    },
    { //sablona ID=2
      IP_MP, IPV4_SRC_ADDR, L4_SRC_PORT, IPV4_DST_ADDR, L4_DST_PORT, PROTOCOL, FIRST_SWITCHED, LAST_SWITCHED, IN_BYTES, IN_PKTS
    }
  };

  /**
   * Vrati index daneho atributu v danej sablone.
   * @param templateID ID sablony
   * @param fieldID ID atributu
   * @return index daneho atributu v sablone, ak dany atribut v sablone neexistuje vrati <CODE>-1</CODE>
   */
  public static int getFieldIndex(int templateID, int fieldID) {

    for (int i = 0; i < TEMPLATE[templateID].length; i++)
      if (TEMPLATE[templateID][i] == fieldID)
        return i;
    return -1;

  } //getFieldIndex()
  
  public static String getFiledName(int field){
    String fieldName;
    switch(field){  
        case 8: fieldName="IPV4_SRC_ADDR";break;     
        case 12: fieldName="IPV4_DST_ADDR";break;   
        case 7: fieldName="L4_SRC_PORT";break;   
        case 11: fieldName="L4_DST_PORT";break;    
        case 22: fieldName="FIRST_SWITCHED";break;    
        case 21: fieldName="LAST_SWITCHED";break;   
        case 1: fieldName="IN_BYTES";break;   
        case 2: fieldName="IN_PKTS";break;     
        case 23: fieldName="OUT_BYTES";break;    
        case 24: fieldName="OUT_PKTS";break;    
        case 40: fieldName="TOTAL_BYTES_EXP";break;    
        case 41: fieldName="TOTAL_PKTS_EXP";break;    
        case 4: fieldName="PROTOCOL";break;     
        case 15: fieldName="IPV4_NEXT_HOP";break;
        case 10: fieldName="INPUT_SNMP";break;
        case 14: fieldName="OUTPUT_SNMP";break;
        case 6: fieldName="TCP_FLAGS";break;
        case 5: fieldName="SRC_TOS";break;
        case 16: fieldName="SRC_AS";break;
        case 17: fieldName="DST_AS";break;
        case 9: fieldName="SRC_MASK";break;
        case 13: fieldName="DST_MASK";break;
        default: fieldName="Nepoznam(ID"+field+")"; break;
    }
    
    return fieldName;
  }
} //Templates