/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universalmodelconverter.loader.obj;

import universalmodelconverter.util.Vector2f;
import universalmodelconverter.util.Vector3f;

/**
 *
 * @author Warren
 */
public class FaceVertex {
    int index = -1;
    public Vector3f v = null;
    public Vector2f t = null;
    public Vector3f n = null;

    @Override
    public String toString() {
        return v + "|" + n + "|" + t;
    }
}
