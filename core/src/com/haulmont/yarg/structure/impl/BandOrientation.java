/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 31.05.2010 17:08:29
 *
 * $Id: Orientation.java 1890 2010-06-01 07:31:16Z degtyarjov $
 */
package com.haulmont.yarg.structure.impl;

public enum BandOrientation {
    HORIZONTAL("H"),
    VERTICAL("V");

    private BandOrientation(String id) {
        this.id = id;
    }

    public final String id;

    public static BandOrientation fromId(String id) {
        for (BandOrientation orientation : values()) {
            if (orientation.id.equals(id)) {
                return orientation;
            }
        }

        return null;
    }
}
