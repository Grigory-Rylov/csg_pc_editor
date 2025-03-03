package com.github.grishberg.cad3d.keyboard;

import static com.github.grishberg.cad3d.keyboard.KeyPlaceholder.placeHolderLeft;
import static com.github.grishberg.cad3d.keyboard.KeyPlaceholder.placeHolderRight;
import static com.github.grishberg.cad3d.keyboard.Utils.hull;
import static com.github.grishberg.cad3d.keyboard.Utils.union;

import eu.printingin3d.javascad.models.Abstract3dModel;
import java.util.ArrayList;

public class ThumbConnections {

    private final ThumbKeyPlace thumbKeyPlace;

    private final ArrayList<Abstract3dModel> models = new ArrayList<>();


    public ThumbConnections(ThumbKeyPlace thumbKeyPlace) {
        this.thumbKeyPlace = thumbKeyPlace;
    }

    public Abstract3dModel buildThumbPlaceConnections() {
        models.clear();

        addHull(
            thumbKeyPlace.placeR(placeHolderLeft()),
            thumbKeyPlace.placeM(placeHolderRight())
        );

        addHull(
            thumbKeyPlace.placeM(placeHolderLeft()),
            thumbKeyPlace.placeL(placeHolderRight())
        );

        return union(models);
    }

    private void addHull(Abstract3dModel... children) {
        models.add(hull(children));
    }

}
