/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.cnl.bm;

/**
 * Base exception within JXColl. All other JXColl specific exceptions should extend
 * this class. This class can be used in catch block, when we want to catch all 
 * JXColl specific exceptions.
 * @author Tomas Verescak
 */
public class JXCollException extends Exception {

    public JXCollException(String message) {
        super(message);
    }
}
