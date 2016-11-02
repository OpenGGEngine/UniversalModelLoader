/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opengg.external.modelloader.loaders.threeds;

import java.io.File;
import java.io.FileInputStream;

public class ThreeDSLoader implements Logger{
    
 private File file = new File("C:/res/core/corei7.3ds");

  //  @Test
 
    public void testComplexModel() throws Exception {
        FileInputStream fis = new FileInputStream(file);
        MapReader reader = new MapReader(fis.getChannel());
        Parser parser = new Parser(reader);
        parser.setLogger(this);
        Model model = parser.parseFile();
        debugModel(model);
   //     assertNotNull(model);
    }

    private void debugModel(Model model) {
        for (ModelObject object : model.objects) {
            if (object.vertices != null)
                maxMin(object.vertices);
        }

        System.out.println();
        System.out.printf("global x [%.2f, %.2f]\n",g_x_min, g_x_max);
        System.out.printf("global y [%.2f, %.2f]\n",g_y_min, g_y_max);
        System.out.printf("global z [%.2f, %.2f]\n",g_z_min, g_z_max);

    }

    float g_x_max = Float.MIN_VALUE;
    float g_x_min = Float.MAX_VALUE;
    float g_y_max = Float.MIN_VALUE;
    float g_y_min = Float.MAX_VALUE;
    float g_z_max = Float.MIN_VALUE;
    float g_z_min = Float.MAX_VALUE;


    private void maxMin(float[] v) {
        float x_max = Float.MIN_VALUE;
        float x_min = Float.MAX_VALUE;
        float y_max = Float.MIN_VALUE;
        float y_min = Float.MAX_VALUE;
        float z_max = Float.MIN_VALUE;
        float z_min = Float.MAX_VALUE;

        for (int i=0; i*3 < v.length; i+=3) {
            float x = v[i*3 + 0];
            float y = v[i*3 + 1];
            float z = v[i*3 + 2];
            x_max = Math.max(x_max, x);
            x_min = Math.min(x_min, x);
            y_max = Math.max(y_max, y);
            y_min = Math.min(y_min, y);
            z_max = Math.max(z_max, z);
            z_min = Math.min(z_min, z);
        }
        System.out.printf("x [%.2f, %.2f]\n",x_min, x_max);
        System.out.printf("y [%.2f, %.2f]\n", y_min, y_max);
        System.out.printf("z [%.2f, %.2f]\n", z_min, z_max);

        g_x_max = Math.max(g_x_max, x_max);
        g_x_min = Math.min(g_x_min, x_min);
        g_y_max = Math.max(g_y_max, y_max);
        g_y_min = Math.min(g_y_min, y_min);
        g_z_max = Math.max(g_z_max, z_max);
        g_z_min = Math.min(g_z_min, z_min);
    }

    public void log(String s) {
        System.out.println(s);
    }
}
