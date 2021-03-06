/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xtx.ut4converter.ucore;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xtx.ut4converter.MapConverter;
import org.xtx.ut4converter.UTGames.UTGame;
import org.xtx.ut4converter.export.UTPackageExtractor;
import org.xtx.ut4converter.t3d.T3DRessource.Type;
import org.xtx.ut4converter.tools.ImageUtils;
import org.xtx.ut4converter.tools.SoundConverter;

/**
 * Some ressource such as texture, sound, ... 
 * embedded into some unreal package
 * @author XtremeXp
 */
public class UPackageRessource {

    /**
     * Unreal Package this ressource belongs to
     */
    UPackage unrealPackage;
    
    /**
     * Where this ressource have been exported.
     * TODO handle multi export file
     * (for textures we might need export as .bmp and .tga as well)
     * If it's null and exportFailed is false the we should try to export it
     */
    File exportedFile;
    
    /**
     * If true means export of this ressource failed
     * and we must not try to export it again
     */
    boolean exportFailed;
    

    
    boolean isUsedInMap;

    /**
     * Group of ressource (optional)
     */
    public String group;

    /**
     * Name of ressource
     */
    public String name;
    
    /**
     * Type of ressource
     * (texture, sounds, staticmesh, mesh, ...)
     */
    Type type;
    
    /**
     * Texture dimension if type of ressource is texture
     */
    Dimension textureDimensions;

    /**
     * Creates an unreal package ressource information object.
     * @param fullName Full package ressource name (e.g:
     * "AmbAncient.Looping.Stower51")
     * @param type Type of ressource (texture, sound, staticmesh, mesh, ...)
     * @param game UT game this ressource belongs to
     * @param isUsedInMap if <code>true</code> means ressource is being used 
     */
    public UPackageRessource(String fullName, Type type, UTGame game, boolean isUsedInMap) {

        String s[] = fullName.split("\\.");

        // TODO handle brush polygon texture info
        // which only have "name" info
        // TODO move out creating upackageressource from level
        // which should be directly an unreal package
        String packageName = type != Type.LEVEL ? s[0] : fullName;
        
        parseNameAndGroup(fullName);
        
        this.type = type;
        unrealPackage = new UPackage(packageName, type, game, this);
        this.isUsedInMap = isUsedInMap;
    }
    
    public void setPackageFile(File f){
        unrealPackage.setFile(f);
    }
    
    /**
     * Creates a package ressource
     * @param fullName Full name of ressource
     * @param uPackage Package this ressource belongs to
     * @param game 
     * @param ressourceType Type of ressource (texture, sound, ...)
     * @param isUsedInMap <code>true<code> if this ressource is used in map that is being converted
     */
    public UPackageRessource(String fullName, Type ressourceType, UTGame game, UPackage uPackage, boolean isUsedInMap) {

        parseNameAndGroup(fullName);
        
        this.type = ressourceType;
        unrealPackage = uPackage;
        unrealPackage.addRessource(this);
        this.isUsedInMap = isUsedInMap;
    }
    
    /**
     * 
     * @param fullName Full ressource name (e.g: "AmbAncient.Looping.Stower51")
     * @param uPackage 
     * @param exportedFile 
     */
    public UPackageRessource(String fullName, UPackage uPackage, File exportedFile) {

        parseNameAndGroup(fullName);
        
        this.unrealPackage = uPackage;
        this.exportedFile = exportedFile;
        this.type = uPackage.type;
        uPackage.ressources.add(this);
    }
    
    /**
     * From exported texture file get the dimension of the texture
     * Should be only called when converting polygon with the texture associated
     */
    public void readTextureDimensions(){
        
        if(type != Type.TEXTURE || exportedFile == null || this.textureDimensions != null){
            return;
        }
        
        try {
            this.textureDimensions = ImageUtils.getTextureDimensions(exportedFile);
        } catch (Exception e){
            Logger.getLogger(UPackageRessource.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }
    
    /**
     * Parse group and name of ressource from full name.
     * e.g: Full ressource name (e.g: "AmbAncient.Looping.Stower51")
     * x) group = "Looping"
     * x) name = "Stower51"
     * @param fullName Full ressource name (e.g: "AmbAncient.Looping.Stower51")
     */
    public void parseNameAndGroup(String fullName){
        String s[] = fullName.split("\\.");

        // TODO handle brush polygon texture info
        // which only have "name" info
        name = s[s.length - 1];

        if (s.length == 2) {
            group = null;
        } 
        else if (s.length == 3) {
            group = s[1];
        } 
        // UT3 does not handle groups like previous unreal engines
        // e.g: "A_Gameplay.JumpPad.Cue.A_Gameplay_JumpPad_Activate_Cue"
        else if (s.length > 3){
            group = "";
            
            for(int i = 1; i <= s.length - 2; i++){
                group += s[i];
                
                if(i < s.length - 2 ){
                    group += ".";
                }
            }
        }
    }
    
    /**
     * Tells if this ressource can be exported.
     * If it has never been exported or export ever failed,
     * it cannot be exported again
     * @return <code>true</code> if the file need to be exported
     */
    public boolean needExport(){
        return !exportFailed && exportedFile == null;
    }
    
    /**
     * Export the ressource from unreal package to file
     * @param packageExtractor
     * @param forceExport Force export of package even if it has ever been extracted
     */
    public void export(UTPackageExtractor packageExtractor, boolean forceExport) {
        
        if((needExport() || forceExport) && packageExtractor != null){
            try {
                packageExtractor.extract(this, forceExport);
            } catch (Exception ex) {
                packageExtractor.logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Export the ressource from unreal package to file
     * @param packageExtractor
     */
    public void export(UTPackageExtractor packageExtractor) {
        export(packageExtractor, false);
    }
    
    /**
     * Guess the converted name used in converted t3d unreal map
     * E.G:
     * UT99:
     * @param mapConverter Map Converter
     * @return 
     */
    public String getConvertedName(MapConverter mapConverter){
        
        // TODO move out this param to some core/config class
        final String UE4_BASEPATH = "/Game/RestrictedAssets/Maps/WIP";
        
        String suffix = "";
            
        if(type == Type.SOUND){
            if(mapConverter.toUE3()){
                // beware UT3 needs cue for AmbientSound but not for AmbientSoundSimple actor
               suffix = "_Cue";
            }
            // UE4 handles both wave and cue sounds for AmbientSound actor
        } 

        else if(type == Type.TEXTURE){
            suffix = "_Mat";
        }
        
        // /Game/RestrictedAssets/Maps/WIP/<convertedmapname>/<pkgName>_<group>_<name>_<suffix>.<pkgName>_<group>_<name>_<suffix>
        return UE4_BASEPATH + "/" + mapConverter.getOutMapName() + "/"  + getFullNameWithoutDots() + suffix + "." + getFullNameWithoutDots() + suffix;
    }
    
    /**
     * Sometimes we need to change name of exported file
     * to have info about from which package this file comes from
     * @return 
     */
    public String getConvertedFileName(){
        String s[] = exportedFile.getName().split("\\.");
        String currentFileExt = s[s.length -1];
            
        return getFullNameWithoutDots() + "." +currentFileExt;
    }
    
    /**
     * Return the full name of package ressource
     * @return <packagename>.<group>.<name>
     */
    public String getFullName(){
        
        
        if(unrealPackage.name != null && group != null && name != null){
            return unrealPackage.name + "." + group + "." + name;
        }
        
        if(unrealPackage.name != null && group == null && name != null){
            return unrealPackage.name + "." + name;
        }
        
        if(unrealPackage.name == null && group == null && name != null){
            return name;
        }
        
        // other cases should not happen normally ...
        
        return "";
    }
    
    public String getFullNameWithoutGroup(){
        if(unrealPackage.name != null && name != null){
            return unrealPackage.name + "." + name;
        } else {
            return "";
        }
    }
    
    /**
     * Replace all dots in full name with underscores.
     * This is used to get converted name or filename
     * @return 
     */
    public String getFullNameWithoutDots(){
        return getFullName().replaceAll("\\.", "_");
    }

    /**
     * 
     * @return Unreal Package this ressource belongs to
     */
    public UPackage getUnrealPackage() {
        return unrealPackage;
    }
    
    /**
     * 
     * @return true if this ressource has been exported to a file
     */
    public boolean isExported(){
        return exportedFile != null;
    }

    public File getExportedFile() {
        return exportedFile;
    }

    public void setExportedFile(File exportedFile) {
        this.exportedFile = exportedFile;
    }

    /**
     * Set this ressource as "used in map"
     * This helps deleting unused ressources after extracting
     * multiple ressources from single unreal package
     * @param isUsedInMap true if this ressource is used in map
     */
    public void setIsUsedInMap(boolean isUsedInMap){
        this.isUsedInMap = isUsedInMap;
    }
    
    /**
     * 
     * @return true if this ressource is used in the map being converted
     */
    public boolean isUsedInMap(){
        return this.isUsedInMap;
    }
    
    /**
     * Says if this ressource needs to be converted
     * to be correctly imported in unreal engine 4.
     * For example so old sounds from unreal 1 / ut99 are not correctly imported
     * we need to convert them to 44k frequency
     * @param mc Map Converter
     * @return <code>true</code> true if needs conversion
     */
    public boolean needsConversion(MapConverter mc){
        return mc.isFromUE1UE2ToUE3UE4() && type == Type.SOUND;
    }
    
    
    /**
     * Convert ressource to good format if needed
     * @param logger 
     * @return  
     */
    public File convert(Logger logger){
        
       // TODO convert sound ressources to wav 44k  using sox (like the good old UT3 converter)
        if(type == Type.SOUND){
            SoundConverter scs = new SoundConverter(logger);
            
            try {
                File tempFile = File.createTempFile(getFullNameWithoutDots(), ".wav");
                scs.convertTo16BitSampleSize(exportedFile, tempFile);
                return tempFile;
            } catch (IOException ex) {
                Logger.getLogger(UPackageRessource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }

    public Type getType() {
        return type;
    }

    /**
     * Return the dimension of the texture
     * if ressource is a texture.
     * Might be null if this ressource is unused in map (optimisation)
     * @return Dimension of texture
     */
    public Dimension getTextureDimensions() {
        return textureDimensions;
    }

    
}
