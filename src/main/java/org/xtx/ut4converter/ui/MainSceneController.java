/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xtx.ut4converter.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.xml.bind.JAXBException;
import org.xtx.ut4converter.MainApp;
import org.xtx.ut4converter.MainApp.FXMLoc;
import org.xtx.ut4converter.UTGames.UTGame;
import org.xtx.ut4converter.config.UserConfig;

/**
 * FXML Controller class
 * TODO i18n
 * @author XtremeXp
 */
public class MainSceneController implements Initializable {
    
    /**
     * Link to UT3 Converter topic 
     * Until we create topic for UT4 converter
     */
    final String URL_UTCONV_FORUM = "http://utforums.epicgames.com/showthread.php?p=25131566";
    
    /**
     * Url to git hub for source code
     */
    final String URL_UTCONV_GITHUB = "https://github.com/xtremexp/UT4Converter";
    
    @FXML
    private MenuItem menuExit;
    @FXML
    private MenuItem menuItemAbout;
    @FXML
    private Menu menuOptions;
    @FXML
    private MenuItem menuSettings;

    @FXML
    private MenuItem menuCheckForUpdates;

    
    public MainApp mainApp;
    public Stage mainStage;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.mainStage = mainApp.getPrimaryStage();
    }
    
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }   

    /**
     * Exit program
     * @param event 
     */
    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }
    
    /**
     * Opens file browser for UT99 .t3d map,
     * then convert it.
     * @param event 
     */
    @FXML
    private void handleConvert(ActionEvent event) {
        convertUtxMap(UTGame.UT99);
    }


    
    /**
     * Show credits about program
     * TODO history, library used, licence
     * @param event 
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About "+MainApp.PROGRAM_NAME+": ");
        alert.setContentText("Version: "+MainApp.VERSION+"\nAuthor: "+MainApp.AUTHOR+"\nPowered by Java "+System.getProperty("java.version"));

        alert.showAndWait();
    }

    /**
     * Display Settings panel
     * @param event 
     */
    @FXML
    private void handleSettings(ActionEvent event) {
        
        showSettings();
    }
    
    private void showSettings(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(FXMLoc.SETTINGS.getPath()));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Settings");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);


            SettingsSceneController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    
    /**
     * Opens url in web browser
     * @param if <code>true</code> then display a confirmation dialog before opening directly web browser.
     * @param url Url to open with web browser 
     */
    private void openUrl(String url, boolean confirmBeforeOpen) {
        
        if(url == null){
            return;
        }
        
        if(confirmBeforeOpen){
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Web browser access");
            alert.setContentText("Do you want to open web browser to this url ?\n"+url);

            Optional<ButtonType> result = alert.showAndWait();
            
            if (result.get() != ButtonType.OK){
                return;
            }
        }
        
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                
                desktop.browse(new URI(url));
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }   
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Impossible to open web browser");
            alert.setContentText("Your system is not or does not support desktop. \n Manually go to:"+url);

            alert.showAndWait();
        }
    }

    @FXML
    private void openUtTopicUrl(ActionEvent event) {
        openUrl(URL_UTCONV_FORUM, true);
    }

    private void openGitHubUrl(ActionEvent event) {
        openUrl(URL_UTCONV_GITHUB, true);
    }

    @FXML
    private void handleConvertU1Map(ActionEvent event) {
        convertUtxMap(UTGame.U1);
    }
    
    private void convertUtxMap(UTGame inputGame){
        
        convertUtxMap(inputGame, UTGame.UT4);
    }
    
    /**
     * 
     * @param inputGame Input UT Game
     * @param outputGame Output UT Game
     */
    private void convertUtxMap(UTGame inputGame, UTGame outputGame) {

        try {
            if (checkGamePathSet(inputGame, inputGame)) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainApp.class.getResource(FXMLoc.CONV_SETTINGS.getPath()));
                AnchorPane page = (AnchorPane) loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Conversion Settings");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(mainStage);
                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                ConversionSettingsController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setInputGame(inputGame);
                controller.setOutputGame(outputGame);
                controller.setMainApp(mainApp);
                controller.load();

                // Show the dialog and wait until the user closes it
                dialogStage.showAndWait();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error Game paths not set");
                alert.setHeaderText("Need to set game path");
                alert.setContentText(inputGame.name + " and/or " + outputGame.name + " game path not set.");

                alert.showAndWait();
                showSettings();
            }
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
            // TODO alert
        }

    }
    
    private boolean checkGamePathSet(UTGame inputGame, UTGame outputGame) throws JAXBException {
        UserConfig userConfig = UserConfig.load();
        
        return userConfig != null && userConfig.hasGamePathSet(inputGame) && userConfig.hasGamePathSet(outputGame);
    }

    @FXML
    private void convertUt2004Map(ActionEvent event) {
        convertUtxMap(UTGame.UT2004);
    }

    @FXML
    private void convertUt3Map(ActionEvent event) {
        convertUtxMap(UTGame.UT3);
    }

    @FXML
    private void convertUt2003Map(ActionEvent event) {
        convertUtxMap(UTGame.UT2003);
    }

    @FXML
    private void convertU2Map(ActionEvent event) {
        convertUtxMap(UTGame.U2);
    }

}
