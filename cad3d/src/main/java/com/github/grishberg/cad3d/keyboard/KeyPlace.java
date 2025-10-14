package com.github.grishberg.cad3d.keyboard;

import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig;
import eu.printingin3d.javascad.coords.Angles3d;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.models.Abstract3dModel;

public class KeyPlace {

    private final KeyboardConfig cfg;
    private final double capTopHeight;
    private final double mountWidth;
    private double mountHeight;

    private double columnRadius;
    private double rowRadius;


    public KeyPlace(KeyboardConfig cfg) {
        this.cfg = cfg;
        capTopHeight = cfg.getPlateThickness() + cfg.getSaProfileKeyHeight();
        mountWidth = cfg.getKeyswitchWidth() + cfg.getHorizontalExtraSpace();
        mountHeight = cfg.getKeyswitchWidth() + cfg.getVerticalExtraSpace();

        columnRadius =
            ((mountWidth + cfg.getExtraHeight()) / 2.0) /
                sin(toRadians(cfg.getColumnCurvature()) / 2.0) + capTopHeight;
        rowRadius =
            ((mountHeight + cfg.getExtraWidth()) / 2.0) /
                sin(toRadians(cfg.getRowCurvature()) / 2.0) + capTopHeight;

    }

    public Abstract3dModel place(int column, int row, Abstract3dModel obj) {
        return place(column, row, obj, new V3d(0, 0, 0));
    }

    public Abstract3dModel place(int column, int row, Abstract3dModel obj, V3d offset) {
        V3d keyOffset = cfg.getColumnOffsetProvider().getOffset(column);

        return obj.move(offset)
            .move(0, 0, -rowRadius)
            .rotate(Angles3d.xOnly(calculateXAngle(row)))
            .move(0, 0, rowRadius)
            .move(0, 0, -columnRadius)
            .rotate(Angles3d.yOnly(calculateYAngle(column)))
            .move(0, 0, columnRadius)
            .move(keyOffset.x, keyOffset.y, keyOffset.z)
            .rotate(Angles3d.zOnly(cfg.getZAngleProvider().getZAngle(column)))
            .rotate(Angles3d.yOnly(cfg.getTentingAngle()))
            .move(0, 0, cfg.getPlateZOffset());
    }

    public V3d calculateCoordinates(int column, int row) {
        return calculateCoordinates(column, row, new V3d(0, 0, 0));
    }

    public V3d coords(int column, int row, double x, double y, double z) {
        return calculateCoordinates(column, row, new V3d(x, y, z));
    }

    public V3d calculateCoordinates(
        int column,
        int row,
        V3d initialPoint
    ) {
        double zOffset = cfg.getPlateZOffset();
        double zAngle = cfg.getZAngleProvider().getZAngle(column);
        V3d offset = cfg.getColumnOffsetProvider().getOffset(column);
        double dx = offset.getX();
        double dy = offset.getY();
        double dz = offset.getZ();
        double zRad1 = columnRadius;
        double zRad2 = rowRadius;
        double x = initialPoint.getX();
        double y = initialPoint.getY();
        double z = initialPoint.getZ();

        // Convert angles from degrees to radians because Java trigonometric functions use radians
        double zAngleRad = Math.toRadians(zAngle);
        double yAngleRad = Math.toRadians(calculateYAngle(column));
        double xAngleRad = Math.toRadians(calculateXAngle(row));
        double tentingAngleRad = Math.toRadians(cfg.getTentingAngle());

        // 1) move(0,0,-rowRadius)
        z -= zRad2;

        // 2) rotate X by +xAngle
        double yAfterX = y * Math.cos(xAngleRad) - z * Math.sin(xAngleRad);
        double zAfterX = y * Math.sin(xAngleRad) + z * Math.cos(xAngleRad);
        y = yAfterX;
        z = zAfterX;

        // 3) move back by rowRadius
        z += zRad2;

        // 4) move(0,0,-columnRadius)
        z -= zRad1;

        // 5) rotate Y by +yAngle
        double xAfterY = x * Math.cos(yAngleRad) + z * Math.sin(yAngleRad);
        double zAfterY = -x * Math.sin(yAngleRad) + z * Math.cos(yAngleRad);
        x = xAfterY;
        z = zAfterY;

        // 6) move back by columnRadius
        z += zRad1;

        // 7) move by key offset
        x += dx;
        y += dy;
        z += dz;

        // 8) rotate Z by zAngle
        double xAfterZ = x * Math.cos(zAngleRad) - y * Math.sin(zAngleRad);
        double yAfterZ = x * Math.sin(zAngleRad) + y * Math.cos(zAngleRad);
        x = xAfterZ;
        y = yAfterZ;

        // 9) rotate Y by tentingAngle
        double xAfterTenting = x * Math.cos(tentingAngleRad) + z * Math.sin(tentingAngleRad);
        double zAfterTenting = -x * Math.sin(tentingAngleRad) + z * Math.cos(tentingAngleRad);
        x = xAfterTenting;
        z = zAfterTenting;

        // 10) move by plateZOffset
        z += zOffset;

        return new V3d(x, y, z);
    }


    private double calculateYAngle(int column) {
        return cfg.getColumnCurvature() * (cfg.getCenterCol() - column);
    }

    private double calculateXAngle(int row) {
        return cfg.getRowCurvature() * (cfg.getCenterRow() - row);
    }

    public V3d calculatePlacePoint(
        int column,
        int row,
        PlacePointType pointType
    ) {
        return calculatePlacePoint(column, row, pointType, 0, 0);
    }

    public V3d calculatePlacePoint(
        int column,
        int row,
        PlacePointType pointType,
        double xOffset,
        double yOffset
    ){
        return calculatePlacePoint(column, row, pointType, xOffset, yOffset, 0);
    }

    public V3d calculatePlacePoint(
        int column,
        int row,
        PlacePointType pointType,
        double xOffset,
        double yOffset,
        double zOffset
    ) {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        switch (pointType) {
            case BackLeftTop:
                x = cfg.getKeyPlaceHolderWidth() / -2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / 2.0 + yOffset;
                z = cfg.getKeyPlaceHolderHeight() + zOffset;
                break;
            case BackRightTop:
                x = cfg.getKeyPlaceHolderWidth() / 2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / 2.0 + yOffset;
                z = cfg.getKeyPlaceHolderHeight() + zOffset;
                break;
            case FrontLeftTop:
                x = cfg.getKeyPlaceHolderWidth() / -2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / -2.0 + yOffset;
                z = cfg.getKeyPlaceHolderHeight() + zOffset;
                break;
            case FrontRightTop:
                x = cfg.getKeyPlaceHolderWidth() / 2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / -2.0 + yOffset;
                z = cfg.getKeyPlaceHolderHeight() + zOffset;
                break;
            case BackLeftBottom:
                x = cfg.getKeyPlaceHolderWidth() / -2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / 2.0 + yOffset;
                z = zOffset;
                break;
            case BackRightBottom:
                x = cfg.getKeyPlaceHolderWidth() / 2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / 2.0 + yOffset;
                z = zOffset;
                break;
            case FrontLeftBottom:
                x = cfg.getKeyPlaceHolderWidth() / -2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / -2.0 + yOffset;
                z = zOffset;
                break;
            case FrontRightBottom:
                x = cfg.getKeyPlaceHolderWidth() / 2.0 + xOffset;
                y = cfg.getKeyPlaceHolderDepth() / -2.0 + yOffset;
                z = zOffset;
                break;
        }

        V3d initialPoint3d = new V3d(x, y, z);
        return calculateCoordinates(column, row, initialPoint3d);
    }

}
