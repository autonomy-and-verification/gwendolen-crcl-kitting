package gwendolen.crcl.kitting;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import rcs.posemath.PmCartesian;
import rcs.posemath.PmEulerZyx;
import rcs.posemath.PmException;
import rcs.posemath.PmHomogeneous;
import rcs.posemath.PmPose;
import rcs.posemath.PmRotationMatrix;
import rcs.posemath.PmRotationVector;
import rcs.posemath.PmRpy;
import rcs.posemath.Posemath;
import rcs.posemath.PmQuaternion;

/**
 * Shape describes the basic attributes of the world model objects. Each object
 * has a name, type and location. Slots can also be shapes. Trays also have a
 * contains "slot" items. In addition, each shape has inferences about its state - for
 * example if a slot is open, or contains a gear. Depending on the parent (kit
 * or supply tray) this can be a free gear or a filled slot.
 *
 * @author michaloski
 */
public class CShape implements Cloneable {

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * inference structure for describing a shape inferences. note for a tray,
     * each slot has its own inference, under the tray shape. For gears, the
     * inference describes the tray and slot the in which the gear is located.
     */
    public static class inference_type {

        /** name of the inference object - sku +type + numeric id*/ 
        public String name;
        /** name of the inference object type - sku + type*/ 
        public String type;
       /** state of the inference object occupied by name of gear or empty*/  
        public String state;
       /** pose of the inference object*/  
        public String location;

       /** name of the inference object parent if slot*/  
        public String parent;
       /** name of the inference object slot if gear*/  
        public String slot;
    };
    /**
     * container for all inferences of a shape.
     */
    public Vector<inference_type> inferences = new Vector<inference_type>();

    /**
     * constructor for spaee, intiializes the contains container for slots.
     * Slots are immutable (each tray has a fixed number) but the inferences
     * define the slot state and can dynamically change.
     */
    public CShape() {
        _contains = new ArrayList< CShape>();
    }
    /**
     * constructor for spaee, intiializes the contains container for slots.
     * Slots are immutable (each tray has a fixed number) but the inferences
     * define the slot state and can dynamically change.
     * @param name sku name of the shape
     * @param type sku name minus numeric trailing idetnfier
     * @param pose PmPose of the shape
     * @param parent parent of the spape
     */

    public CShape(String name,
            String type,
            PmPose pose,
            CShape parent) {
        _name = name;
        _type = type;
        _location = pose;
        _parent = parent;
        _contains = new ArrayList< CShape>();
    }
  /**
     * constructor for spaee, intiializes the contains container for slots.
     * Slots are immutable (each tray has a fixed number) but the inferences
     * define the slot state and can dynamically change.
     * @param name sku name of the shape
     * @param type sku name minus numeric trailing idetnfier
     * @param pose PmPose of the shape
      */
    public CShape(String name,
            String type,
            PmPose pose) {
        _name = name;
        _type = type;
        _location = pose;
        _updated = "";
        _contains = new ArrayList< CShape>();
    }

    /**
     * accessoro name
     *
     * @return string containing shape name.
     */
    public String name() {
        return _name;
    }

    /**
     * accessor to set the shape name
     *
     * @param name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * accessor method that returns the type of shape: sku identifier e.g., sm,
     * med, lg gear of type of kit, or type of supply vessel.
     *
     * @return
     */
    public String type() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    /**
     * accessor method to retrieve the shape location as a pose.
     *
     * @return
     */
    public PmPose centroid() {
        return _location;
    }

    public void setCentroid(PmPose centroid) {
        _location = centroid;
    }

    /** flag to determine if the field has been updated */
    public String _updated;
    
    /** sku name of the shape*/
    private String _name;
    
    /** sku type - just removal of number identifier at end of name*/
    private String _type;  
    
    /** parent of shpae, e.g., for slot would be tray.
     */
    private CShape _parent;
    /** size of xyz in meters*/
    public PmPose _dimensions;  
    /** location of xyz bottom of object.*/
    public PmPose _location;  
    
    /** slots in tray*/
    public List< CShape> _contains;  

    
    /**
     * find the inference with the given name in this shape.
     * @param name is the name of the inference e.g., slot1, slot2,...
     * @return pointer to the inference_type
     */
    public inference_type findInference(String name) {
        for (inference_type inference : inferences) {
            if (inference.name.equalsIgnoreCase(name)) {
                return inference;
            }
        }
        return null;

    }
    /**
     * method to determine if shape is kit. Looks
     * at name to see if it contains the string "kit'.
     * @return true if shape is kit.
     */
    public boolean isKit() {
        if (_name.indexOf("kit") != -1) {
            return true;
        }
        return false;
    }

   /**
    * method to determine if shape is supply vessel. Looks
     * at name to see if it contains the string "gear_vessel."
     * @return true if shape is supply vessel.
     * */
    public boolean isVessel() // tray
    {
        // kit also has vessel in its name
        if (_name.indexOf("gear_vessel") != -1) {
            return true;
        }
        return false;
    }

    /**
     * method to determine if shape is gear. Looks
     * at name to see if it contains the string gear.
     * @return true if shape is gear.
     */
    public boolean isGear() {
        if (_name.indexOf("part") != -1) {
            return true;
        }
        return false;
    }

    /**
     * does the model name contain sku which signals
     * a kitting scene object.
     * @return 
     */
    public boolean isSkuPart() {
        // If not a sku skip
        if (_name.indexOf("sku") != -1) {
            return true;
        }
        return false;
    }

    /**
     * diagnostic to dump the shape attributes as a string.
     * @return 
     */
    public String dumpShape() {
        String s;
        s = name() + ":" + type() + ":" + KittingInterface.dumpPmPose(_location) + "\n";
        for (CShape slot : this._contains) {
            s += "\t" + slot.name() + ":" + slot.type() + ":" + KittingInterface.dumpPmPose(slot._location) + "\n";
        }
        return s;
    }

};
