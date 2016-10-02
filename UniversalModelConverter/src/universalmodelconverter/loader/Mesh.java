/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universalmodelconverter.loader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author Warren
 */
public class Mesh {

    public Mesh(FloatBuffer vbodata, IntBuffer inddata) {
        this.vbodata = vbodata;
        this.inddata = inddata;
    }
    public FloatBuffer vbodata;
    public IntBuffer inddata;
    public Material m;
}
