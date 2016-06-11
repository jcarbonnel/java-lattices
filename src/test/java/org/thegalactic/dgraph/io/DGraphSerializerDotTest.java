package org.thegalactic.dgraph.io;

/*
 * DGraphSerializerDotTest.java
 *
 * Copyright: 2010-2015 Karell Bertet, France
 * Copyright: 2015-2016 The Galactic Organization, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.thegalactic.dgraph.DGraph;

/**
 * Test the org.thegalactic.dgraph.io.DGraphSerializerDot class.
 */
public class DGraphSerializerDotTest {
    /**
     * Test getInstance.
     */
    @Test
    public void testGetInstance() {
        DGraphSerializerDot serializer = DGraphSerializerDot.getInstance();
        assertEquals(serializer, DGraphSerializerDot.getInstance());
    }

    /**
     * Test read.
     */
    @Test
    public void testWrite() {
        try {
            File file = File.createTempFile("junit", ".dot");
            String filename = file.getPath();
            DGraph graph = new DGraph();
            graph.save(filename);
            List<String> list = Files.readAllLines(Paths.get(filename), Charset.forName("UTF-8"));
            assertEquals(list.get(0), "digraph G {");
            assertEquals(list.get(1), "Graph [rankdir=BT]");
            assertEquals(list.get(2), "}");
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

