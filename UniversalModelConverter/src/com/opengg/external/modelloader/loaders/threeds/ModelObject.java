package com.opengg.external.modelloader.loaders.threeds;

/**
 *
 */
public class ModelObject {
    private String name;
    public float[] vertices;
    public short[] polygons;
    public float[] textureCoordinates;

    public ModelObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
