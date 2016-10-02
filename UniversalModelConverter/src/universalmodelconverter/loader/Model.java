/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universalmodelconverter.loader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import universalmodelconverter.loader.obj.Build;
import universalmodelconverter.loader.obj.Face;
import universalmodelconverter.loader.obj.FaceVertex;

/**
 *
 * @author Warren
 */
public class Model {

    private List<Mesh> meshes = new ArrayList<>();

    public Model(Build b) {
        HashMap<String, ArrayList<Face>> facesByTextureList = new HashMap<>();

        for (Face face : b.faces) {

            if (face.material == null) {
                face.material = new Material("default");

            }

            if (facesByTextureList.containsKey(face.material.toString())) {

                ArrayList<Face> temp = facesByTextureList.get(face.material.toString());
                temp.add(face);
                facesByTextureList.replace(face.material.toString(), temp);
            } else {
                ArrayList<Face> temp = new ArrayList<>();
                temp.add(face);
                facesByTextureList.put(face.material.toString(), temp);
            }

        }
        for (String key : facesByTextureList.keySet()) {
            ArrayList<Face> currentFaceList = facesByTextureList.get(key);
            currentFaceList = splitQuads(currentFaceList);
            Mesh obj = makeadamnvbo(currentFaceList);
            Material material = currentFaceList.get(0).material;

            obj.m = material;
            meshes.add(obj);
        }
    }

    public static Mesh makeadamnvbo(ArrayList<Face> triangles) {
        HashMap<FaceVertex, Integer> indexMap = new HashMap<>();
        int nextVertexIndex = 0;
        ArrayList<FaceVertex> faceVertexList = new ArrayList<>();
        for (Face face : triangles) {
            for (FaceVertex vertex : face.vertices) {
                if (!indexMap.containsKey(vertex)) {
                    indexMap.put(vertex, nextVertexIndex++);
                    faceVertexList.add(vertex);
                }
            }
        }
        int verticeAttributesCount = nextVertexIndex;
        int indicesCount = triangles.size() * 3;

        int numMIssingNormals = 0;
        int numMissingUV = 0;
        FloatBuffer verticeAttributes = FloatBuffer.allocate(faceVertexList.size() * 12);

        for (FaceVertex vertex : faceVertexList) {
            verticeAttributes.put(vertex.v.x);
            verticeAttributes.put(vertex.v.y);
            verticeAttributes.put(vertex.v.z);
            verticeAttributes.put(1);
            verticeAttributes.put(0.4f);
            verticeAttributes.put(24);
            verticeAttributes.put(1);
            if (vertex.n == null) {
                // @TODO: What's a reasonable default normal?  Maybe add code later to calculate normals if not present in .obj file.
                verticeAttributes.put(1.0f);
                verticeAttributes.put(1.0f);
                verticeAttributes.put(1.0f);
                numMIssingNormals++;
            } else {
                verticeAttributes.put(vertex.n.x);
                verticeAttributes.put(vertex.n.y);
                verticeAttributes.put(vertex.n.z);
            }
            // @TODO: What's a reasonable default texture coord?  
            if (vertex.t == null) {
//                verticeAttributes.put(0.5f);
//                verticeAttributes.put(0.5f);
                verticeAttributes.put((float) Math.random());
                verticeAttributes.put((float) Math.random());
                numMissingUV++;
            } else {
                verticeAttributes.put(vertex.t.x);
                verticeAttributes.put(vertex.t.y);
            }

        }
        verticeAttributes.flip();

        IntBuffer indices = IntBuffer.allocate(indicesCount);    // indices into the vertices, to specify triangles.

        for (Face face : triangles) {
            for (FaceVertex vertex : face.vertices) {
                int index = indexMap.get(vertex);
                indices.put(index);
            }
        }
        indices.flip();
        return new Mesh(verticeAttributes, indices);
    }

    public static ArrayList<Face> splitQuads(ArrayList<Face> faceList) {
        ArrayList<Face> triangleList = new ArrayList<>();
        int countTriangles = 0;
        int countQuads = 0;
        int countNGons = 0;
        for (Face face : faceList) {
            if (face.vertices.size() == 3) {
                countTriangles++;
                triangleList.add(face);
            } else if (face.vertices.size() == 4) {
                countQuads++;
                FaceVertex v1 = face.vertices.get(0);
                FaceVertex v2 = face.vertices.get(1);
                FaceVertex v3 = face.vertices.get(2);
                FaceVertex v4 = face.vertices.get(3);
                Face f1 = new Face();
                f1.map = face.map;
                f1.material = face.material;
                f1.add(v1);
                f1.add(v2);
                f1.add(v3);
                triangleList.add(f1);
                Face f2 = new Face();
                f2.map = face.map;
                f2.material = face.material;
                f2.add(v1);
                f2.add(v3);
                f2.add(v4);
                triangleList.add(f2);
            } else {
                countNGons++;
            }
        }
        int texturedCount = 0;
        int normalCount = 0;
        for (Face face : triangleList) {
            if ((face.vertices.get(0).n != null)
                    && (face.vertices.get(1).n != null)
                    && (face.vertices.get(2).n != null)) {
                normalCount++;
            }
            if ((face.vertices.get(0).t != null)
                    && (face.vertices.get(1).t != null)
                    && (face.vertices.get(2).t != null)) {
                texturedCount++;
            }
        }

        return triangleList;
    }

    public void putData() throws FileNotFoundException, IOException {

        FileOutputStream ps;

        ps = new FileOutputStream("C:/res/textbin.txt");
         
         // create data output stream
         DataOutputStream dos = new DataOutputStream(ps);
         dos.writeInt(meshes.size());
        for (Mesh m : meshes) {
            FloatBuffer fs = m.vbodata;
            IntBuffer ib = m.inddata;
            
            dos.writeInt(fs.capacity());
            for (float f : fs.array()) {
               dos.writeFloat(f);
            }
            dos.writeInt(ib.capacity());
            for (int i : ib.array()) {
                dos.writeInt(i);
            }           
            m.m.toFileFormat(dos);
        }
         dos.close();
        ps.close();
       
        
        System.out.println("Finished");
    }
}
