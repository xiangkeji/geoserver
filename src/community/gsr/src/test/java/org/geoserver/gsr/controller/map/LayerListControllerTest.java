/* (c) 2020 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/* Copyright (c) 2013 - 2017 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gsr.controller.map;

import static org.junit.Assert.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.geoserver.gsr.JsonSchemaTest;
import org.geoserver.gsr.controller.ControllerTest;
import org.junit.Test;

public class LayerListControllerTest extends ControllerTest {
    private String query(String service, String params) {
        return getBaseURL() + service + "/MapServer/layers" + params;
    }

    @Test
    public void testStreamsQuery() throws Exception {
        String result = getAsString(query("cite", "?f=json"));
        assertTrue(
                "Streams output must validate: " + result,
                JsonSchemaTest.validateJSON(result, "/gsr-ms/1.0/allLayersAndTables.json"));
        JSONObject json = JSONObject.fromObject(result);
        assertTrue(json.has("tables"));
        assertTrue(json.has("layers"));
        assertTrue(json.get("tables") instanceof JSONArray);
        assertTrue(json.get("layers") instanceof JSONArray);
        JSONArray tables = json.getJSONArray("tables");
        for (Object object : tables) {
            JSONObject table = (JSONObject) object;
            //            assertFalse("Table " + table + " should not have an extent",
            // table.has("extent"));
        }

        JSONArray layers = json.getJSONArray("layers");
        for (Object object : layers) {
            JSONObject layer = (JSONObject) object;
            //            assertTrue("LayerEntry " + layer + " should have an extent",
            // layer.has("extent"));
        }
    }
}
