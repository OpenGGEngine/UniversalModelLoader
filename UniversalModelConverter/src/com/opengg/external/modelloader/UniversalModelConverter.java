/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opengg.external.modelloader;

import com.opengg.core.math.Vector2f;
import com.opengg.core.math.Vector3f;
import com.opengg.core.model.Build;
import com.opengg.core.model.Face;
import com.opengg.core.model.FaceVertex;
import com.opengg.core.model.Material;
import com.opengg.core.model.Mesh;
import com.opengg.core.model.Model;
import static com.opengg.core.util.FileUtil.getFileName;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.opengg.external.modelloader.loaders.obj.OBJParser;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiGetMaterialColor;
import static org.lwjgl.assimp.Assimp.aiGetMaterialTexture;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_GenNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_PreTransformVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiTextureType_NONE;

/**
 *
 * @author Warren
 */
public class UniversalModelConverter extends Application {

    public final ProgressBar nodelist = new ProgressBar();
    final Button openButton = new Button("Choose model...");
    public Label progress = new Label("Temp", nodelist);
    public volatile double percent;

    public class MyRunnable implements Runnable {

        private File file;

        public MyRunnable(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                openButton.setVisible(false);
                nodelist.setVisible(true);
                progress.setVisible(true);
                assimpOpen(file);
                nodelist.setVisible(false);
                progress.setVisible(false);
                openButton.setVisible(true);
                // code in the other thread, can reference "var" variable
            } catch (IOException ex) {
                Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void start(final Stage stage) {
        stage.setTitle("Universal Model Converter. Now Fully Universal.");
        Group root = new Group();
        Scene scene = new Scene(root, 600, 450, Color.WHITE);
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        BorderPane borderPane = new BorderPane();

        final FileChooser fileChooser = new FileChooser();

        openButton.setOnAction((final ActionEvent e) -> {
            File fileobj = fileChooser.showOpenDialog(stage);
            if (fileobj != null) {
                MyRunnable myRunnable = new MyRunnable(fileobj);
                Thread t = new Thread(myRunnable);
                t.start();
            }

        });

        final FileChooser fileChooser2 = new FileChooser();
        final Button openButton2 = new Button("Load OBJ");
        nodelist.setVisible(false);
        openButton2.setOnAction((final ActionEvent e) -> {
            File fileobj = fileChooser2.showOpenDialog(stage);
            if (fileobj != null) {
                try {
                    openFile(fileobj);
                } catch (IOException ex) {
                    Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        System.out.println(UniversalModelConverter.class.getResource("derpy.png").getPath().substring(1));
        Image image = new Image(UniversalModelConverter.class.getResource("derpy.png").toExternalForm());

        // simple displays ImageView the image as is
        ImageView iv1 = new ImageView();
        iv1.setImage(image);
        iv1.setFitWidth(450);
        iv1.setPreserveRatio(true);
        iv1.setSmooth(true);
        iv1.setCache(true);
        Text text3 = new Text();
        text3.setFont(new Font(15));
        text3.setWrappingWidth(450);
        text3.setTextAlignment(TextAlignment.JUSTIFY);
        text3.setText("Alpha Release: 1.1. Only diffuse textures supported. Expect Bugs \n \n NOTE: When loading your OBJs with MTLs, the MTL must be in the same directory as the OBJ file. Otherwise the OBJ will not load correctly. Sometimes the program will appear to stall. This is assimp and this behavior is perfectly normal.");

        Tab assimptab = new Tab("Assimp Loader");
        VBox box1 = new VBox(30);
        nodelist.prefWidthProperty().bind(stage.widthProperty().subtract(200));
        box1.setAlignment(Pos.TOP_CENTER);
        assimptab.setContent(box1);
        progress.setVisible(false);
        box1.getChildren().addAll(text3, openButton, progress, nodelist);

        Text text = new Text();
        text.setFont(new Font(15));
        text.setWrappingWidth(350);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        text.setText("This is the legacy OBJ loader. "
                + "It doesn't load other types of models. "
                + "If you want that, you should use the new Assimp Loader."
                + " Loading Non-OBJ files will result in disaster.");

        Text text2 = new Text();
        text2.setFont(new Font(20));
        text2.setWrappingWidth(200);
        text2.setTextAlignment(TextAlignment.CENTER);
        text2.setText("Legacy OBJ Loader");

        Tab legacytab = new Tab("Legacy OBJ Loader");
        VBox box2 = new VBox(25);

        //hbox.getChildren().add(new Label("Tab" + i));
        box2.setAlignment(Pos.TOP_CENTER);
        legacytab.setContent(box2);
        box2.getChildren().addAll(iv1, text2, openButton2, text);

        tabPane.getTabs().addAll(assimptab, legacytab);

        // bind to take available space
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());

        borderPane.setCenter(tabPane);
        root.getChildren().add(borderPane);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {

        Application.launch(args);
    }

    public void assimpOpen(File file) throws IOException {

        AIScene scene = aiImportFile(file.getAbsolutePath(), aiProcess_Triangulate | aiProcess_PreTransformVertices | aiProcess_GenNormals);
        System.out.println(scene.mNumTextures());
        AINode root = scene.mRootNode();
        System.out.println(root.mName().dataString());
        System.out.println(root.mNumChildren());

        List<Mesh> meshes = new ArrayList<>();
        for (int root2 = 0; root2 < root.mChildren().remaining(); root2++) {
            System.out.println("Root " + root2);
            long address = root.mChildren().get(root2);
            AINode node = AINode.create(address);
            percent = (root2 + 1) / (double) root.mNumChildren();
            final int yeah = root2 + 1;
            final int yeah2 = root.mNumChildren();
            Platform.runLater(
                    () -> {
                        nodelist.setProgress(percent);
                        progress.setText("Node: " + yeah + "/" + yeah2);
                    }
            );
            List<Face> facelist = new ArrayList<>();
           //note that due to the current setup, there is only one mesh per node.

            for (int root3 = 0; root3 < 1; root3++) {

                int adress2 = node.mMeshes().get(0);
                AIMesh mesh = AIMesh.create(scene.mMeshes().get(adress2));
                int materialindex = mesh.mMaterialIndex();
                AIMaterial dumb = AIMaterial.create(scene.mMaterials().get(materialindex));

                Material mat = new Material(dumb.toString());
                AIString path = AIString.create();
                //The worst function parameters known to mankind
                aiGetMaterialTexture(dumb, aiTextureType_DIFFUSE, 0, path, new int[100], new int[100], new float[100], new int[100], new int[100], new int[100]);

                System.out.println(path.dataString());

                AIColor4D mDiffuseColor = AIColor4D.create();
                if (aiGetMaterialColor(dumb, AI_MATKEY_COLOR_DIFFUSE,
                        aiTextureType_NONE, 0, mDiffuseColor) != 0) {
                    throw new IllegalStateException(aiGetErrorString());
                }
                int faceCount = mesh.mNumFaces();
                int elementCount = faceCount * 3;
                AIFace.Buffer facesBuffer = mesh.mFaces();
                for (int i = 0; i < faceCount; ++i) {
                    AIFace face = facesBuffer.get(i);
                    System.out.println(i + "/" + faceCount);

                    if (face.mNumIndices() != 3) {
                        throw new IllegalStateException("Uh Oh, Triangulation Messed Up. AIFace.mNumIndices() != 3");
                    }
                    Face faces = new Face();

                    for (int i2 = 0; i2 < 3; i2++) {
                        FaceVertex fv = new FaceVertex();
                        int index = face.mIndices().get(i2);
                        //mesh.mNormals()
                        AIVector3D vertex = mesh.mVertices().get(index);
                        Vector3f vertex1 = new Vector3f(vertex.x(), vertex.y(), vertex.z());

                        AIVector3D normal = mesh.mNormals().get(index);
                        Vector3f normal1 = new Vector3f(normal.x(), normal.y(), normal.z());

                        AIVector3D texture = mesh.mTextureCoords(0).get(index);
                        Vector2f texturecoord = new Vector2f(texture.x(), texture.y());
                        //   System.out.println(texturecoord.toString());
                        fv.t = texturecoord;
                        fv.v = vertex1;
                        fv.n = normal1;
                        // System.out.println(fv.toString());
                        switch (i2) {
                            case 0:
                                faces.v1 = fv;
                                break;
                            case 1:
                                faces.v2 = fv;
                                break;

                            case 2:

                                faces.v3 = fv;
                                break;
                        }

                        // mesh.mTextureCoords()
                    }
                    facelist.add(faces);

                }
                
                mat.mapKdFilename = path.dataString();
                Mesh mesh1 = new Mesh(facelist, mat);
                meshes.add(mesh1);
            }

        }
        Model model = new Model("shit", meshes);
        String endloc = file.getAbsolutePath().replace("shit", "");
        model.putData(endloc);

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
            String endloc = file.getAbsolutePath().replace(b.getObjectFileName(), "");
            System.out.println(endloc);
            m.putData(endloc);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadFile(String path) {
        String pathname = getFileName(path);
        try {
            DataInputStream in = new DataInputStream(new FileInputStream("C:/res/" + pathname + "/" + pathname + ".bmf"));
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
