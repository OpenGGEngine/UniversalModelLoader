/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opengg.external.modelloader;

import com.opengg.core.math.Vector3f;
import com.opengg.core.model.Build;
import com.opengg.core.model.Face;
import com.opengg.core.model.Material;
import com.opengg.core.model.Mesh;
import com.opengg.core.model.Model;
import static com.opengg.core.util.FileUtil.getFileName;
import com.opengg.external.modelloader.loaders.obj.OBJParser;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_AMBIENT;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_DIFFUSE;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_SPECULAR;
import static org.lwjgl.assimp.Assimp.aiGetMaterialColor;
import static org.lwjgl.assimp.Assimp.aiGetMaterialTextureCount;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_FindInvalidData;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_ImproveCacheLocality;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_OptimizeMeshes;
import static org.lwjgl.assimp.Assimp.aiProcess_PreTransformVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_RemoveRedundantMaterials;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiReturn_SUCCESS;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiTextureType_HEIGHT;
import static org.lwjgl.assimp.Assimp.aiTextureType_NONE;
import static org.lwjgl.assimp.Assimp.aiTextureType_SHININESS;
import static org.lwjgl.assimp.Assimp.aiTextureType_SPECULAR;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Warren
 */
public class UniversalModelConverter extends Application {

    public final ProgressBar nodelist = new ProgressBar();
    final Button openButton = new Button("Choose model...");
    public Label progress = new Label("Temp", nodelist);
    public volatile double percent;
    public static int wow;

    public class MyRunnable implements Runnable {

        private File file;

        public MyRunnable(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            // try {
            openButton.setVisible(false);
            nodelist.setVisible(true);
            progress.setVisible(true);
            try {
                assimpOpen2(file);
            } catch (IOException ex) {
                Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
            nodelist.setVisible(false);
            progress.setVisible(false);
            openButton.setVisible(true);
            // code in the other thread, can reference "var" variable
            //  } catch (IOException ex) {
            //     Logger.getLogger(UniversalModelConverter.class.getName()).log(Level.SEVERE, null, ex);
            //  }
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
        text3.setText("Alpha Release: 1.3. Diffuse, specular and normal map textures supported. Expect Bugs \n \n "
                + "NOTE: When loading your OBJs with MTLs, the MTL must be in the same directory as the OBJ file. "
                + "Otherwise the OBJ will not load correctly. Sometimes the program will appear to stall. "
                + "This is assimp and this behavior is perfectly normal.");

        Tab assimptab = new Tab("Assimp Loader");
        VBox box1 = new VBox(30);
        nodelist.prefWidthProperty().bind(stage.widthProperty().subtract(200));
        box1.setAlignment(Pos.TOP_CENTER);
        assimptab.setContent(box1);
        progress.setVisible(false);
        box1.getChildren().addAll(text3, openButton, progress, nodelist);

        Text text = new Text("DO NOT USE.\n"
                + "This is the legacy OBJ loader. "
                + "It doesn't load other types of models. "
                + "If you want that, you should use the new Assimp Loader."
                + " Loading Non-OBJ files will result in disaster.");
        text.setFont(new Font(15));
        text.setWrappingWidth(350);
        text.setTextAlignment(TextAlignment.JUSTIFY);

        Text text2 = new Text("Legacy OBJ Loader");
        text2.setFont(new Font(20));
        text2.setWrappingWidth(200);
        text2.setTextAlignment(TextAlignment.CENTER);

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

    public void assimpOpen2(File file) throws IOException {
        List<Mesh> meshes = new ArrayList<>();
        AIScene scene = aiImportFile(file.getAbsolutePath(), aiProcess_ImproveCacheLocality | aiProcess_FindInvalidData | aiProcess_JoinIdenticalVertices | aiProcess_RemoveRedundantMaterials | aiProcess_OptimizeMeshes | aiProcess_Triangulate
                | aiProcess_PreTransformVertices | aiProcess_GenSmoothNormals);
        //AIScene scene = aiImportFile(file.getAbsolutePath(), aiProcess_PreTransformVertices);
        processNode(scene.mRootNode(), scene, meshes);
        System.out.println("weedidit");
        Model model = new Model("", meshes);
        String endloc = file.getAbsolutePath().replace("shit", "");

        model.putData(endloc);

    }

    public void processNode(AINode node, AIScene scene, List<Mesh> meshes) {

        percent = (wow + 1) / (double) scene.mRootNode().mNumChildren();
        final int yeah = wow + 1;
        final int yeah2 = scene.mRootNode().mNumChildren();
        Platform.runLater(
                () -> {
                    nodelist.setProgress(percent);
                    progress.setText("Node: " + yeah + "/" + yeah2);
                }
        );
        // Process all the node's meshes (if any)
        for (int i = 0; i < node.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(node.mMeshes().get(i)));
            meshes.add(processMesh(mesh, scene));
        }
        // Then do the same for each of its children
        for (int i = 0; i < node.mNumChildren(); i++) {
            processNode(AINode.create(node.mChildren().get(i)), scene, meshes);

        }
        wow++;
    }

    public Mesh processMesh(AIMesh mesh, AIScene scene) {
        FloatBuffer vertices = MemoryUtil.memAllocFloat(mesh.mNumVertices() * 12);
        IntBuffer indices = MemoryUtil.memAllocInt(mesh.mNumFaces() * 3);
        // vector<GLuint> indices;

        //Process Vertices
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            //Vertex Position
            AIVector3D vertex = mesh.mVertices().get(i);
            vertices.put(vertex.x()).put(vertex.y()).put(vertex.z());

            //Vertex Colors currntly disabled
            if (false) {
                AIColor4D colors = AIColor4D.create(mesh.mColors().get(i));

                vertices.put(colors.r()).put(colors.g()).put(colors.b()).put(colors.a());
            } else {
                vertices.put(1f).put(1f).put(1f).put(1f);
            }

            //Vertex Normal
            AIVector3D normal = mesh.mNormals().get(i);
            vertices.put(normal.x()).put(normal.y()).put(normal.z());

            //Texture Coordinates
            if (mesh.mNumUVComponents().get(0) != 0) {
                AIVector3D texture = mesh.mTextureCoords(0).get(i);
                vertices.put(texture.x()).put(texture.y());
            } else {
                vertices.put(0).put(0);
            }
        }
        // Process indices
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace face = mesh.mFaces().get(i);
            indices.put(face.mIndices());

        }
        // Process material
        Material mat = (mesh.mMaterialIndex() >= 0) ? processMaterial(AIMaterial.create(scene.mMaterials().get(mesh.mMaterialIndex()))) : Material.defaultmaterial;
        vertices.flip();
        indices.flip();
        return new Mesh(vertices, indices, mat);
    }
   
    public Material processMaterial(AIMaterial material) {
        Material mat = new Material(material.toString());
        //The worst function parameters known to mankind
        AIColor4D colour = AIColor4D.create();

        AIString path = AIString.calloc();
        int result = Assimp.aiGetMaterialTexture(material, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        
        if (result == aiReturn_SUCCESS && (aiGetMaterialTextureCount(material, aiTextureType_DIFFUSE) >0)) {
            Path p = Paths.get(textPath);
            mat.mapKdFilename = p.getFileName().toString();
            mat.hascolmap = true;
        }

        AIString path2 = AIString.calloc();
        result = Assimp.aiGetMaterialTexture(material, aiTextureType_SPECULAR, 0, path2, (IntBuffer) null, null, null, null, null, null);
        String textPath2 = path2.dataString();
        if (result == aiReturn_SUCCESS && (aiGetMaterialTextureCount(material, aiTextureType_SPECULAR) >0)) {
            Path p = Paths.get(textPath2);
            mat.mapKsFilename = p.getFileName().toString();
            mat.hasspecmap = true;
        }

        AIString path3 = AIString.calloc();
        result = Assimp.aiGetMaterialTexture(material, aiTextureType_HEIGHT, 0, path3, (IntBuffer) null, null, null, null, null, null);
        String textPath3 = path3.dataString();
        if (result == aiReturn_SUCCESS && (aiGetMaterialTextureCount(material,  aiTextureType_HEIGHT) >0)) {
            Path p = Paths.get(textPath3);
            mat.bumpFilename = p.getFileName().toString();
            mat.hasnormmap = true;
        }

        AIString path4 = AIString.calloc();
        result = Assimp.aiGetMaterialTexture(material, aiTextureType_SHININESS, 0, path4, (IntBuffer) null, null, null, null, null, null);
        String textPath4 = path4.dataString();
        if (result == aiReturn_SUCCESS && (aiGetMaterialTextureCount(material, aiTextureType_SHININESS) >0)) {
            Path p = Paths.get(textPath4);
            mat.mapNsFilename = p.getFileName().toString();
            mat.hasspecpow = true;
        }

        Vector3f ambient = new Vector3f(1, 1, 1);
        result = aiGetMaterialColor(material, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            ambient = new Vector3f(colour.r(), colour.g(), colour.b());
        }
        mat.ka = ambient;

        Vector3f diffuse = new Vector3f(1, 1, 1);
        result = aiGetMaterialColor(material, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            diffuse = new Vector3f(colour.r(), colour.g(), colour.b());
        }
        mat.kd = diffuse;

        Vector3f specular = new Vector3f(1, 1, 1);
        result = aiGetMaterialColor(material, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            specular = new Vector3f(colour.r(), colour.g(), colour.b());
        }
        mat.ks = specular;

        return mat;
    }

    private void openFile(File file) throws IOException {
        Build b = new Build();
        try {
            OBJParser p = new OBJParser(b, file.getAbsolutePath());

        } catch (IOException ex) {
            Logger.getLogger(UniversalModelConverter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        Model m = new Model(b);
        try {
            String endloc = file.getAbsolutePath().replace(b.getObjectFileName(), "");
            System.out.println(endloc);
            m.putData(endloc);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniversalModelConverter.class
                    .getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UniversalModelConverter.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UniversalModelConverter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

}
