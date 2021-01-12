/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting.gwendolencrclclient;

import crcl.base.*;
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

import org.apache.commons.math3.util.Pair;

/**
 * Shapes is a container for the shapes in the kitting scene,
 * the definitions defining the parts in the scene (gears, kits,
 * supply vessel, as well as the slots in a tray).
 * @author michaloski
 */
public class CShapes {

    private Vector<CShape> shapes = new Vector<CShape>();
    public static Vector<CShape> definitions = new Vector<CShape>();
    public static Vector<CShape> instances = new Vector<CShape>();

    /**
     * initialize all the shape attributes, and if a tray describes
     * all the slots contained.
     */
    public static void initDefinitions() {
        PmPose dimensions;

        try {
            definitions.add(new CShape("definition", "sku_part_small_gear", new PmPose()));
            definitions.add(new CShape("definition", "sku_part_medium_gear", new PmPose()));
            definitions.add(new CShape("definition", "sku_part_large_gear", new PmPose()));

            /// Vessels
            definitions.add(new CShape("definition", "sku_small_gear_vessel", new PmPose()));
            CShape vessel = definitions.lastElement();

            // Need dimensions to understand if within container
            dimensions = new PmPose(new PmCartesian(.22, 0.165, 0.), new PmQuaternion(1., 0., 0., 0.));
            vessel._dimensions = dimensions;

            // The defines the slots and offsets from the centroid
            vessel._contains.add(new CShape("slot1",
                    "sku_part_small_gear",
                    new PmPose(new PmCartesian(0.028575, 0.028575, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            vessel._contains.add(new CShape("slot2",
                    "sku_part_small_gear",
                    new PmPose(new PmCartesian(0.028575, -0.028575, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));
            vessel._contains.add(new CShape("slot3",
                    "sku_part_small_gear",
                    new PmPose(new PmCartesian(-0.028575, 0.028575, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));
            vessel._contains.add(new CShape("slot4",
                    "sku_part_small_gear",
                    new PmPose(new PmCartesian(-0.028575, -0.028575, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            definitions.add(new CShape("definition", "sku_large_gear_vessel", new PmPose()));
            CShape vessel1 = definitions.lastElement();

            // Need dimensions to understand if within container
            dimensions = new PmPose(new PmCartesian(.22, 0.165, 0),
                    new PmQuaternion(1., 0., 0., 0.));
            vessel1._dimensions = dimensions;

            // The defines the slots and offsets from the centroid
            vessel1._contains.add(new CShape("slot1",
                    "sku_part_large_gear",
                    new PmPose(new PmCartesian(-.055, 0.0, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            vessel1._contains.add(new CShape("slot2",
                    "sku_part_large_gear",
                    new PmPose(new PmCartesian(.055, 0.0, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            definitions.add(new CShape("definition", "sku_medium_gear_vessel", new PmPose()));
            CShape vessel2 = definitions.lastElement();

            // Need dimensions to understand if within container
            dimensions = new PmPose(new PmCartesian(.22, 0.165, 0),
                    new PmQuaternion(1., 0., 0., 0.));
            vessel2._dimensions = dimensions;

            // The defines the slots and offsets from the centroid
            vessel2._contains.add(new CShape("slot1",
                    "sku_part_medium_gear",
                    new PmPose(new PmCartesian(0.0396, 0.0396, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            vessel2._contains.add(new CShape("slot2",
                    "sku_part_medium_gear",
                    new PmPose(new PmCartesian(-0.0396, 0.0396, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));
            vessel2._contains.add(new CShape("slot3",
                    "sku_part_medium_gear",
                    new PmPose(new PmCartesian(0.0396, -0.0396, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));
            vessel2._contains.add(new CShape("slot4",
                    "sku_part_medium_gear",
                    new PmPose(new PmCartesian(-0.0396, -0.0396, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            /// Kits
            definitions.add(new CShape("definition", "sku_kit_s2l2_vessel", new PmPose()));
            CShape vessel3 = definitions.lastElement();

            // Need dimensions to understand if within container
            dimensions = new PmPose(new PmCartesian(.22, 0.165, 0),
                    new PmQuaternion(1., 0., 0., 0.));
            vessel3._dimensions = dimensions;

            // The defines the slots and offsets from the centroid
            vessel3._contains.add(new CShape("slot1",
                    "sku_part_small_gear",
                    new PmPose(new PmCartesian(0.03, -0.054, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            vessel3._contains.add(new CShape("slot2",
                    "sku_part_small_gear",
                    new PmPose(new PmCartesian(-0.03, -0.054, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));
            vessel3._contains.add(new CShape("slot3",
                    "sku_part_large_gear",
                    new PmPose(new PmCartesian(-.055, 0.0375, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));
            vessel3._contains.add(new CShape("slot4",
                    "sku_part_large_gear",
                    new PmPose(new PmCartesian(.055, 0.0375, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            definitions.add(new CShape("definition", "sku_kit_m2l1_vessel", new PmPose()));
            CShape vessel4 = definitions.lastElement();
            // Need dimensions to understand if within container
            dimensions = new PmPose(new PmCartesian(0.16, 0.19, 0),
                    new PmQuaternion(1., 0., 0., 0.));
            vessel4._dimensions = dimensions;

            // The defines the slots and offsets from the centroid
            vessel4._contains.add(new CShape("slot1",
                    "sku_part_medium_gear",
                    new PmPose(new PmCartesian(-0.04, 0.055, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

            vessel4._contains.add(new CShape("slot2",
                    "sku_part_medium_gear",
                    new PmPose(new PmCartesian(0.04, 0.055, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));
            vessel4._contains.add(new CShape("slot3",
                    "sku_part_large_gear",
                    new PmPose(new PmCartesian(0.0, -0.04, 0.0),
                            new PmQuaternion(1., 0., 0., 0.)),
                    vessel));

        } catch (Exception ex) {
            Logger.getLogger(CRCLClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * access method to find the associated definition for a type.
     * @param atype sku name of type
     * @return shape definition describing the shapetype.
     */
    static public CShape findDefinition(String atype) {
        for (int i = 0; i < definitions.size(); i++) {
            if (definitions.get(i).type().equalsIgnoreCase( atype)) {
                return definitions.get(i);
            }
        }
        return null;
    }

    /**
     * accessor function to find a matching name in the instances
     * vector supplied as a calling argument.
     * @param shapes pointer to vector containg shapes
     * @param aname name of the shape (sku with numeric trailing id).
     * @return shape definition of the instance.
     */
    static public CShape findInstance(Vector<CShape> shapes, String aname) {
        // @todo this finds global instance name, not class instance name
        for (int i = 0; i < shapes.size(); i++) {
            if (shapes.get(i).name().equalsIgnoreCase(aname)) {
                return shapes.get(i);
            }
        }
        return null;
    }

    /**
     * mutex clone of current instance.
     * @return clone of the intances.
     */
    static public Vector<CShape> snapshotInstances() {
        Vector<CShape> now_instances = new Vector<CShape>();
        try {
            Globals.mutex.lock();
            now_instances.clear();
            for (CShape instance : instances) {
                now_instances.add(instance);
            }
        } finally {
            Globals.mutex.unlock();
        }
        return now_instances;
    }

    /**
     * store the instance properties for given model.
     * Will parse the crcl xml models in ther status
     * report for inferences also.
     * @param name sku type plus numeric trailing id
     * @param model name and pose of model.
     */
    public static void storeInstanceProperties(String name,
            ModelsStatusType model) {

        if (!Globals.bReadAllInstances) {
            return;
        }

        // Test me if this copies may not be valid.
        //Vector<CShape> now_instances = snapshotInstances();
        Vector<CShape> now_instances = instances;
        CShape shape = findInstance(now_instances, name);
        if (shape == null) {
            return;
        }

        Vector<CShape.inference_type> inferences = shape.inferences;
        // this clears ONE CSHape inferences, gear, tray: kit, vessesl
        inferences.clear();

        MapType map = model.getProperties();
        crcl.base.Map props = map.getMap();
        // Gears are different than kit/vessels
        List<MapItemType> maplist = props.getItem();
        if (shape.isGear()) {
            CShape.inference_type inference = new CShape.inference_type();
            inference.name = "parent";
            for (MapItemType m : maplist) {
                if (m.getKey().equalsIgnoreCase("parent")) {
                    inference.parent = m.getValue();
                }
                if (m.getKey().equalsIgnoreCase("slot")) {
                    inference.slot = m.getValue();
                }
            }
            inferences.add(inference);
        } else {
            CShape.inference_type inference = new CShape.inference_type();
            for (MapItemType m : maplist) {
                if (m.getKey().equalsIgnoreCase("name")) {
                    inference.name = m.getValue();
                }
                if (m.getKey().equalsIgnoreCase("type")) {
                    inference.type = m.getValue();
                }
                if (m.getKey().equalsIgnoreCase("location")) {
                    inference.location = m.getValue();
                }
                if (m.getKey().equalsIgnoreCase("state")) {
                    inference.state = m.getValue();
                }
            }
            inferences.add(inference);
        }

    }

    /**
     * simple store of a model instance given name and pose
     * @param name sku name of the shape
     * @param centroid location in world coordinates of the shape 
     */
    public static void storeInstance(String name,
            PmPose centroid) {

        // make sure instance is sku item
        if (name.indexOf("sku") == -1) {
            return;
        }

        try {
            Globals.mutex.lock();

            // lock thread as we are updating
            //std::unique_lock<std::mutex> lock(WorldModel::shapemutex);
            // Find out if instance has already been defined
            for (int i = 0; i < instances.size(); i++) {
                if (instances.get(i).name().equals(name)) {
                    instances.get(i)._location = centroid;
                    Globals.bReadAllInstances = true;
                    return;
                }
            }

            CShape definition;
            String type = name;
            type = type.replaceAll("\\d*$", "");
            instances.add(new CShape(name, type, centroid));
            if ((definition = findDefinition(type)) != null) {
                instances.lastElement()._contains = definition._contains;
            }
        } finally {
            Globals.mutex.unlock();
        }

    }

    /**
     * given a tray with slot, find the shape definition for the
     * given slot name (e.g., slot1, slot2, ...
     * @param tray tray either kit or supply vessel that containes slots.
     * @param slotname name of the slot
     * @return shape definition of the slot.
     */
    public static CShape findSlot(CShape tray, String slotname) {
        // either kit or vessel - now do slots
        CShape slotz = findDefinition(tray.type());

        if (slotz == null) {
            return null;
        }

        for (int j = 0; j < slotz._contains.size(); j++) {
            CShape slot = slotz._contains.get(j);
            if (slot.name().equalsIgnoreCase(slotname)) {
                return slotz._contains.get(j);
            }
        }
        return null;
    }

    /**
     * use the model inferences to find a free gear of type geartyp
     * in the supply vessels.
     * @param geartype type of gear, either sm, med, lg.
     * @return shape definition of gear.
     */
    public static CShape findFreeGear(String geartype) {
        Vector<CShape> now_instances = snapshotInstances();

        for (int i = 0; i < now_instances.size(); i++) {
            CShape instance = now_instances.get(i);

            if (!instance.isVessel()) {
                continue;
            }

            // kit - now do slots
            CShape slotz = findDefinition(instance.type());

            if (slotz == null) {
                continue;
            }

            for (int j = 0; j < slotz._contains.size(); j++) {
                CShape slot = slotz._contains.get(j);
               CShape.inference_type inference = instance.findInference(slot.name());
                if (inference.type.equalsIgnoreCase(geartype) && 
                        !inference.state.equalsIgnoreCase("open")) {
                    // look up gear by name
                    return findInstance(now_instances, inference.state);
                }
            }

        }
        return null;
    }

    /**
     * find an open slot that in one of the kits. 
     * @return a pair: shape defoining the kit and open slot.
     */
    public static Pair<CShape, CShape> findOpenKittingGearSlot()
            //CShape kit,   CShape openslot) 
    {

        // freeze current instance locations
        Vector<CShape> now_instances = snapshotInstances();

        for (int i = 0; i < now_instances.size(); i++) {
            CShape instance = now_instances.get(i);

            // assume all instances are part of this robot workspace
            if (!instance.isKit()) {
                continue;
            }

            // kit - now do slots
            CShape slotz = findDefinition(instance.type());

            // no matching type containing slot for kit - bad bad bad 
            if (slotz == null) {
                // fixme: log once error not every time, too messy in Java
                continue;
            }

            // at this point instance is a kitting tray
            for (int j = 0; j < slotz._contains.size(); j++) {
                CShape slot = slotz._contains.get(j);
                CShape.inference_type inference = instance.findInference(slot.name());
                if(inference==null)
                {
                    System.out.println("findOpenKittingGearSlot null reference - shouldn't be");
                    continue;
                }
                if (inference.state.equalsIgnoreCase( "open")) {
                    //kit = instances.get(i);
                    //openslot=slot;
                    return new Pair<CShape, CShape>(instances.get(i),slot);
                }
            }

        }
        return new Pair<CShape, CShape>(null,null);
    }


};
