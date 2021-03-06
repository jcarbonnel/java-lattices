package org.thegalactic.context.io;

/*
 * ContextSerializerBurmeister.java
 *
 * Copyright: 2010-2015 Karell Bertet, France
 * Copyright: 2015-2016 The Galactic Organization, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.TreeSet;

import org.thegalactic.context.Context;
import org.thegalactic.io.Reader;
import org.thegalactic.io.Writer;

/**
 * This class defines the way for reading a context from a text file.
 *
 * ![ContextSerializerBurmeister](ContextSerializerBurmeister.png)
 *
 * @uml ContextSerializerBurmeister.png
 * !include
 * resources/org/thegalactic/context/io/ContextSerializerBurmeister.iuml
 * !include resources/org/thegalactic/io/Reader.iuml
 * !include resources/org/thegalactic/io/Writer.iuml
 *
 * hide members
 * show ContextSerializerBurmeister members
 * class ContextSerializerBurmeister #LightCyan
 * title ContextSerializerBurmeister UML graph
 */
public final class ContextSerializerBurmeister implements Reader<Context>, Writer<Context> {

    /**
     * String extension.
     */
    private static final String EXTENSION = "cxt";

    /**
     * Burmeister header.
     */
    private static final String HEADER = "B";

    /**
     * The singleton instance.
     */
    private static final ContextSerializerBurmeister INSTANCE = new ContextSerializerBurmeister();

    /**
     * Return the singleton instance of this class.
     *
     * @return the singleton instance
     */
    public static ContextSerializerBurmeister getInstance() {
        return INSTANCE;
    }

    /**
     * Register this class for reading and writing .cxt files.
     */
    public static void register() {
        ContextIOFactory.getInstance().registerReader(ContextSerializerBurmeister.getInstance(), EXTENSION);
        ContextIOFactory.getInstance().registerWriter(ContextSerializerBurmeister.getInstance(), EXTENSION);
    }

    /**
     * This class is not designed to be publicly instantiated.
     */
    private ContextSerializerBurmeister() {
    }

    /**
     * Read a context from a file.
     *
     * The Burmeister cxt format file is respected :
     *
     * The file format is structured as follows:
     *
     * The first line consists of a single "B"
     * The second line contains the name of the context (note that this is
     * ignored)
     * The third and fourth line consist of the object and attribute count,
     * respectively
     * after that, all objects and all attributes are listed, each on a separate
     * line
     * finally, the context is given as a combination of . and X, each row on a
     * separate line.
     *
     * ~~~
     * B
     * Example
     * 2
     * 2
     * a
     * b
     * 1
     * 2
     * .X
     * XX
     * ~~~
     *
     * @param context a context to read
     * @param file    a file
     *
     * @throws IOException When an IOException occurs
     */
    public void read(final Context context, final BufferedReader file) throws IOException {
        // str corresponds to the string "B". First line (Unused).
        String str = file.readLine();

        // Detect Burmeister magic header
        if (!HEADER.equals(str)) {
            throw new IOException("Burmeister magic header not found");
        }

        // Second line (Unused in the code).
        file.readLine();

        try {
            // number of observations. Third line.
            final int nbObs = Integer.parseInt(file.readLine());

            // number of attributes. Fourth line.
            final int nbAtt = Integer.parseInt(file.readLine());

            // Now reading observations
            // Observations names must be recorded for the reading context phase
            String[] obsNames = new String[nbObs];
            for (int i = 0; i < nbObs; i++) {
                obsNames[i] = this.readNextLine(file);
                context.addToObservations(obsNames[i]);
            }

            // Now reading attributes
            // Attributes names must be recorded for the reading context phase
            String[] attNames = new String[nbAtt];
            for (int i = 0; i < nbAtt; i++) {
                attNames[i] = this.readNextLine(file);
                context.addToAttributes(attNames[i]);
            }

            // Now reading context
            for (int i = 0; i < nbObs; i++) {
                str = this.readNextLine(file);
                for (int j = 0; j < nbAtt; j++) {
                    if (str.charAt(j) == 'X') {
                        context.addExtentIntent(obsNames[i], attNames[j]);
                    }
                }
            }
            context.setBitSets();
        } catch (NumberFormatException ex) {
            throw new IOException(ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Return the next non-empty line.
     *
     * @param file a file
     *
     * @return the next non-empty line
     *
     * @throws IOException When an IOException occurs
     */
    private String readNextLine(final BufferedReader file) throws IOException {
        String str;
        do {
            str = file.readLine();
        } while ("".equals(str));
        return str;
    }

    /**
     * Write a context to a file.
     *
     * The Burmeister cxt format file is respected :
     *
     * The file format is structured as follows:
     *
     * The first line consists of a single "B"
     * The second line contains the name of the context (note that this is
     * ignored)
     * The third and fourth line consist of the object and attribute count,
     * respectively.
     * The fifth line is empty.
     * After that, all objects and all attributes are listed, each on a separate
     * line
     * finally, the context is given as a combination of . and X, each row on a
     * separate line.
     *
     * ~~~
     * B
     * Example
     * 2
     * 2
     *
     * a
     * b
     * 1
     * 2
     * .X
     * XX
     * ~~~
     *
     * @param context a context to write
     * @param file    a file
     *
     * @throws IOException When an IOException occurs
     */
    @Override
    public void write(final Context context, final BufferedWriter file) throws IOException {
        // Magic header
        file.write(HEADER);
        file.newLine();

        // Empty name
        file.newLine();

        final TreeSet<Comparable> attributes = context.getAttributes();
        final TreeSet<Comparable> observations = context.getObservations();

        // Observation and attributes size
        file.write(String.valueOf(observations.size()));
        file.newLine();
        file.write(String.valueOf(attributes.size()));
        file.newLine();

        // Observations
        for (final Comparable observation : observations) {
            file.write(observation.toString());
            file.newLine();
        }

        // Attributes
        for (final Comparable attribute : attributes) {
            file.write(attribute.toString());
            file.newLine();
        }

        // Extent/Intent
        final StringBuilder builder = new StringBuilder();
        for (final Comparable observation : observations) {
            builder.setLength(0);
            for (final Comparable attribute : attributes) {
                if (context.getIntent(observation).contains(attribute)) {
                    builder.append('X');
                } else {
                    builder.append('.');
                }
            }
            file.write(builder.toString());
            file.newLine();
        }
    }
}
