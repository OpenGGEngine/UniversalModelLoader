/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universalmodelconverter;

import java.awt.Desktop;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import universalmodelconverter.loader.Model;
import universalmodelconverter.loader.obj.Build;
import universalmodelconverter.loader.obj.OBJParser;

/**
 *
 * @author Warren
 */
public class UniversalModelConverter extends Application {

    private Desktop desktop = Desktop.getDesktop();

    @Override
    public void start(final Stage stage) {
        stage.setTitle("This sucks");

        final FileChooser fileChooser = new FileChooser();

        final Button openButton = new Button("Choose model...");

        openButton.setOnAction((final ActionEvent e) -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {

               try {
                openFile(file);
                loadFile("sd");
               } catch (IOException ex) {
                   Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
                 }
            }
        });

        final GridPane inputGridPane = new GridPane();

        GridPane.setConstraints(openButton, 0, 0);
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(openButton);

        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(inputGridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

        stage.setScene(new Scene(rootGroup));
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void openFile(File file) throws IOException {
        Build b = new Build();
        try {
            OBJParser p = new OBJParser(b, file.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        Model m = new Model(b);
        try {
            m.putData();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println();
    }

    public void loadFile(String path) {

        try {
            DataInputStream in = new DataInputStream(new FileInputStream("C:/res/textbin.txt"));
            int s = in.readInt();
            int fbcap = in.readInt();
            for (int i = 0; i < fbcap; i++) {
                System.out.println(in.readFloat());
            }
            int ibcap = in.readInt();
            for (int i = 0; i < ibcap; i++) {
                System.out.println(in.readInt());
            }
            int len = in.readInt();
            String name = "";
            for (int i = 0; i < len; i++) {
                name += in.readChar();
            }
            System.out.println(name);

            System.out.println(in.readDouble());
            System.out.println(in.readDouble());
            System.out.println(in.readDouble());

            System.out.println(in.readDouble());
            System.out.println(in.readDouble());
            System.out.println(in.readDouble());

            System.out.println(in.readDouble());
            System.out.println(in.readDouble());
            System.out.println(in.readDouble());

            System.out.println(in.readDouble());
            System.out.println(in.readDouble());
            System.out.println(in.readDouble());

            System.out.println(in.readInt());

            System.out.println(in.readBoolean());

            System.out.println(in.readDouble());

            System.out.println(in.readDouble());

            System.out.println(in.readDouble());

            System.out.println(in.readDouble());

            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
            System.out.println(in.readInt());
            
            len = in.readInt();
            if (len != 0) {
                name = "";
                for (int i = 0; i < len; i++) {
                    name += in.readChar();
                }
                System.out.println(name);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
