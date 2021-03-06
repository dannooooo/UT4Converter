/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xtx.ut4converter.ucore;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.xtx.ut4converter.UTGames;
import org.xtx.ut4converter.UTGames.UnrealEngine;
import org.xtx.ut4converter.t3d.T3DRessource.Type;

/**
 * Very basic implementation of unreal package
 * @author XtremeXp
 */
public class UPackage {
    
    
    /**
     * UT game this package comes from
     */
    private UTGames.UTGame game;
    
    /**
     * Name of package
     */
    String name;
    
    /**
     * File of package
     */
    File file;
    
    /**
     * Package ressources (textures, staticmeshes, ...)
     */
    Set<UPackageRessource> ressources = new HashSet<>();
    
    boolean exported;
    
    /**
     * Type of package (level, sound, textures, ...)
     * TODO remove some package may not contain only one type of ressource
     * (e.g: map packages)
     */
    public Type type;
    
    /**
     * 
     * @param name Package Name
     * @param type Type of package (sounds, textures, ...)
     * @param game UT game this package belong to
     * @param uRessource
     */
    public UPackage(String name, Type type, UTGames.UTGame game, UPackageRessource uRessource){
        this.name = name;
        this.type = type;
        this.game = game;
        ressources.add(uRessource);
    }



    public String getName() {
        return name;
    }

    /**
     * Gets the associated file with this package.
     * @param gamePath Base path of the ut game this unreal package comes from
     * @return 
     */
    public File getFileContainer(File gamePath){
        
        if(this.file != null){
            return this.file;
        }
        
        // refactor this
        if(type == Type.LEVEL){
            this.file = new File(name);
        }
        else {
            this.file = new File(gamePath.getAbsolutePath() + File.separator + getFileFolder() + File.separator + getName() + getFileExtension());
            
            // Temp hack sometimes textures are embedded not only in .utx files but .u files
            if(!this.file.exists()){
                this.file = new File(gamePath.getAbsolutePath() + File.separator + "System" + File.separator + getName() + ".u");
            }
            
            // might be map itself
            if(!this.file.exists()){
                this.file = new File(gamePath.getAbsolutePath() + File.separator + "Maps" + File.separator + getName() + "." + game.mapExtension);
            }
        }
        
        return this.file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    
    public void addRessource(UPackageRessource ressource){
        ressources.add(ressource);
    }

    /**
     * List all ressources of packages that have been exported
     * @return List of exported ressources
     */
    public Set<File> getExportedFiles() {
        
        Set<File> exportedFiles = new HashSet<>();
        
        for(UPackageRessource upr : ressources){
            if(upr.getExportedFile() != null){
                exportedFiles.add(upr.getExportedFile());
            }
        }
        
        return exportedFiles;
    }
    
    /**
     * Returns ressource package by full name
     * @param fullName Full ressource name (e.g: "AmbAncient.Looping.Stower51")
     * @return ressource with same full name
     */
    public UPackageRessource findRessource(String fullName){
        
        String s[] = fullName.split("\\.");
        String fullNameWithoutGroup = null;
        String group = null;
        
        if(s.length == 3){
            fullNameWithoutGroup = s[0] + "." + s[2];
            group = s[1];
        }
        
        for(UPackageRessource packageRessource : ressources){

            if(fullName.equals(packageRessource.getFullName()) 
                    || fullName.equals(packageRessource.getFullNameWithoutGroup())){
                return packageRessource;
            } 
            
            // Package ressource was created without group info
            // since we have this info now, update the ressource and return it
            else if(packageRessource.getFullNameWithoutGroup().equals(fullNameWithoutGroup)){
                
                if(group != null){
                    packageRessource.group = s[2];
                }
                
                return packageRessource;
            }
        }
        
        return null;
    }
    

    /**
     * Get ressources used by the package.
     * The ressource list is built on extracting ressource packages
     * with unreal package extractor
     * @return List of ressources of the package
     */
    public Set<UPackageRessource> getRessources() {
        return ressources;
    }
    
    /**
     * Return path where unreal packages are stored depending
     * on type of ressource
     * @return Relative folder from UT path where the unreal package file should be
     */
    private String getFileFolder(){
        
        if(null != type) switch (type) {
            case MUSIC:
                return "Music";
            case SOUND:
                return "Sounds";
            case TEXTURE:
                return "Textures";
            case STATICMESH:
                return "StaticMeshes";
            case LEVEL:
                return "Maps";
            case SCRIPT:
                return "System";
            default:
        }
        
        return null;
    }
    
    /**
     * Return relative path
     * @return 
     */
    private String getFileExtension(){
        
        if(null != type) switch (type) {
            case MUSIC:
                if(game.engine == UnrealEngine.UE1){
                    return ".umx";
                } else if(game.engine == UnrealEngine.UE2){
                    return ".ogg";
                }
            case SOUND:
                return ".uax";
            case TEXTURE:
                return ".utx";
            case STATICMESH:
                return ".usx";
            case SCRIPT:
                return ".u";
            case LEVEL:
                return ".unr";
            default:
        }
        
        return null;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }
    
    
    
}
