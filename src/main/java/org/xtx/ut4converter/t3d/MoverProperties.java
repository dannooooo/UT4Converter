/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xtx.ut4converter.t3d;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3d;
import org.xtx.ut4converter.export.UTPackageExtractor;
import static org.xtx.ut4converter.t3d.T3DActor.IDT;
import org.xtx.ut4converter.t3d.iface.T3D;
import org.xtx.ut4converter.ucore.UPackageRessource;

/**
 * Since T3DMover extends T3DBrush for Unreal Engine 1
 * and T3DMover extends T3DStaticMesh for Unreal Engine > 1
 * Need to have a common "class" for both actors
 * for sharing same properties
 * @author XtremeXp
 */
public class MoverProperties implements T3D {
    
    /**
     * Sounds used by movers when it started moving, is moving ...
     * TODO make a list of UPackageRessource binded with some property name
     */
    UPackageRessource closedSound, closingSound, openedSound, openingSound, moveAmbientSound;

    /**
     * CHECK usage?
     */
    List<Vector3d> savedPositions = new ArrayList<>();
    
    
    /**
     * List of positions where mover moves
     * UTUE4: Lift Destination=(X=0.000000,Y=0.000000,Z=730.000000) (no support for several nav localisations unlike UE1/2)
     * UT99: KeyPos(1)=(Y=72.000000)
     */
    List<Vector3d> positions = new ArrayList<>();
    
    /**
     * CHECK usage?
     * U1: BaseRot=(Yaw=-49152)
     */
    List<Vector3d> savedRotations = new ArrayList<>();;
    
    /**
     * How long it takes for the mover to get to next position
     */
    Double moveTime;
    
    /**
     * How long the mover stay static in his final position before returning to home
     */
    Double stayOpenTime;
    
    
    /**
     * How long time the mover is available again after getting back to home position
     */
    Double delayTime;
    
    T3DActor mover;
    
    public MoverProperties(T3DActor mover){
        this.mover = mover;
    }
    
    @Override
    public boolean analyseT3DData(String line) {
        // UE1 -> 'Wait at top time' (UE4)
        if(line.contains("StayOpenTime")){
            stayOpenTime = T3DUtils.getDouble(line);
        } 
        
        // UE1 -> 'Lift Time' (UE4)
        else if(line.contains("MoveTime")){
            moveTime = T3DUtils.getDouble(line);
        } 
        
        // UE1 -> 'Retrigger Delay' (UE4)
        else if(line.contains("DelayTime")){
            delayTime = T3DUtils.getDouble(line);
        }
        
        // UE1 -> 'CloseStartSound' ? (UE4)
        else if(line.contains("ClosedSound=")){
            closedSound = mover.mapConverter.getUPackageRessource(line.split("\\'")[1], T3DRessource.Type.SOUND);;
        }
        
        // UE1 -> 'CloseStopSound' ? (UE4)
        else if(line.contains("ClosingSound=")){
            closingSound = mover.mapConverter.getUPackageRessource(line.split("\\'")[1], T3DRessource.Type.SOUND);
        }
        
        // UE1 -> 'OpenStartSound' ? (UE4)
        else if(line.contains("OpeningSound=")){
            openingSound = mover.mapConverter.getUPackageRessource(line.split("\\'")[1], T3DRessource.Type.SOUND);
        }
        
        // UE1 -> 'OpenStopSound' ? (UE4)
        else if(line.contains("OpenedSound=")){
            openedSound = mover.mapConverter.getUPackageRessource(line.split("\\'")[1], T3DRessource.Type.SOUND);
        }
        
        // UE1 -> 'Closed Sound' (UE4)
        else if(line.contains("MoveAmbientSound=")){
            moveAmbientSound = mover.mapConverter.getUPackageRessource(line.split("\\'")[1], T3DRessource.Type.SOUND);
        }
        
        // UE1 -> 'Lift Destination' (UE12)
        else if(line.contains("SavedPos=")){
            savedPositions.add(T3DUtils.getVector3d(line.split("SavedPos")[1], 0D));
        }
        
        // UE1 -> 'Saved Positions' (UE12)
        else if(line.contains("SavedRot=")){
            savedRotations.add(T3DUtils.getVector3dRot(line.split("SavedRot")[1]));
        }
        
        // UE1 -> 'Saved Positions' (UE12)
        else if(line.contains("KeyPos")){
            positions.add(T3DUtils.getVector3d(line.split("\\)=")[1], 0D));
        } else {
            return false;
        }
        
        return true;
    }
    
    public String toString(StringBuilder sbf){
        // Write the mover as Destination Lift
        sbf.append(IDT).append("Begin Actor Class=Generic_Lift_C Name=").append(mover.name).append("\n");
        sbf.append(IDT).append("\tBegin Object Name=\"Scene1\"\n");
        mover.writeLocRotAndScale();
        sbf.append(IDT).append("\tEnd Object\n");
        sbf.append(IDT).append("\tScene1=Scene\n");
        sbf.append(IDT).append("\tRootComponent=Scene1\n");

        if(moveTime != null){
            sbf.append(IDT).append("\tLift Time=").append(moveTime).append("\n");
        }


        if(!positions.isEmpty()){
            Vector3d v = positions.get(0);

            sbf.append(IDT).append("\tLift Destination=(X=").append(T3DActor.fmt(v.x)).append(",Y=").append(T3DActor.fmt(v.y)).append(",Z=").append(T3DActor.fmt(v.z)).append(")\n");
        }

        if(openingSound != null){
            sbf.append(IDT).append("\tOpenStartSound=SoundCue'").append(openingSound.getConvertedName(mover.mapConverter)).append("'\n");
        }

        if(openedSound != null){
            sbf.append(IDT).append("\tOpenStopSound=SoundCue'").append(openedSound.getConvertedName(mover.mapConverter)).append("'\n");
        }

        if(closingSound != null){
            sbf.append(IDT).append("\tCloseStartSound=SoundCue'").append(closingSound.getConvertedName(mover.mapConverter)).append("'\n");
        }

        if(closedSound != null){
            sbf.append(IDT).append("\tCloseStopSound=SoundCue'").append(closedSound.getConvertedName(mover.mapConverter)).append("'\n");
        }

        if(moveAmbientSound != null){
            // no property for sound when moving in UT4
        }

        if(stayOpenTime != null){
            sbf.append(IDT).append("\tWait at top time=").append(stayOpenTime).append("\n");
        }

        if(delayTime != null){
            sbf.append(IDT).append("\tRetrigger Delay=").append(delayTime).append("\n");
        }

        mover.writeEndActor();
        
        return sbf.toString();
    }
    
    @Override
    public void convert(){
        
        if(openingSound != null){
            openingSound.export(UTPackageExtractor.getExtractor(mover.mapConverter, openingSound));
        }

        if(openedSound != null){
            openedSound.export(UTPackageExtractor.getExtractor(mover.mapConverter, openedSound));
        }

        if(closingSound != null){
            closingSound.export(UTPackageExtractor.getExtractor(mover.mapConverter, closingSound));
        }

        if(closedSound != null){
            closedSound.export(UTPackageExtractor.getExtractor(mover.mapConverter, closedSound));
        }
    }
    
    @Override
    public void scale(Double newScale){
        
        for(Vector3d position : positions){
            position.scale(newScale);
        }
    }

    @Override
    public String toT3d(StringBuilder sb) {
        return toString(sb);
    }

    @Override
    public String getName() {
        return mover.name;
    }
}
