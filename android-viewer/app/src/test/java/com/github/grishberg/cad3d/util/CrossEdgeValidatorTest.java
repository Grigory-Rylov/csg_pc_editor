package com.github.grishberg.cad3d.util;

import static org.junit.Assert.*;

import eu.printingin3d.javascad.utils.CrossEdgeValidator;
import org.junit.Test;

import eu.printingin3d.javascad.coords.V3d;

public class CrossEdgeValidatorTest {
    @Test
    public void testCross(){
        V3d p = new V3d(10, 0, 0);
        V3d p0 = new V3d(1, 0, 0);
        V3d p1 = new V3d(12, 0, 0);

        assertTrue(CrossEdgeValidator.isPointBetween(p, p0, p1));
    }

    @Test
    public void testTail(){
        V3d p = new V3d(12, 0, 0);
        V3d p0 = new V3d(1, 0, 0);
        V3d p1 = new V3d(12, 0, 0);

        assertFalse(CrossEdgeValidator.isPointBetween(p, p0, p1));
    }


    @Test
    public void testStart(){
        V3d p = new V3d(1, 0, 0);
        V3d p0 = new V3d(1, 0, 0);
        V3d p1 = new V3d(12, 0, 0);

        assertFalse(CrossEdgeValidator.isPointBetween(p, p0, p1));
    }

    @Test
    public void testOut(){
        V3d p = new V3d(0, 0, 0);
        V3d p0 = new V3d(1, 0, 0);
        V3d p1 = new V3d(12, 0, 0);

        assertFalse(CrossEdgeValidator.isPointBetween(p, p0, p1));
    }
}
