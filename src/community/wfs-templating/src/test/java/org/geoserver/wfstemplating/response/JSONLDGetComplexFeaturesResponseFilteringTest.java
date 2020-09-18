/* (c) 2020 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfstemplating.response;

import static org.junit.Assert.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.geoserver.wfstemplating.configuration.TemplateIdentifier;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JSONLDGetComplexFeaturesResponseFilteringTest extends TemplateJSONComplexTestSupport {

    @Test
    public void testJsonLdWithFilterOnIteratingAndComposite() throws Exception {
        // test filter capabilities in json-ld template
        // filter on composite "$filter": "xpath('gml:description') = 'Olivine basalt'"
        // filter on iterating "$filter": "xpath('gsml:ControlledConcept/@id') = 'cc.2'"
        setUpComplex("MappedFeatureIteratingAndCompositeFilter.json", mappedFeature);
        StringBuilder sb =
                new StringBuilder("wfs?request=GetFeature&version=2.0")
                        .append("&TYPENAME=gsml:MappedFeature&outputFormat=")
                        .append("application%2Fld%2Bjson");
        JSONObject result = (JSONObject) getJsonLd(sb.toString());
        Object context = result.get("@context");
        checkContext(context);
        assertNotNull(context);
        JSONArray features = (JSONArray) result.get("features");
        int size = features.size();
        for (int i = 0; i < size; i++) {
            JSONObject feature = features.getJSONObject(i);
            if (i + 1 != size) {
                assertEquals(feature.size(), 5);
                assertNull(feature.get("gsml:GeologicUnit"));
            } else {
                // last feature is the one having the condition matched
                // thus GeologicUnit got encoded.
                assertEquals(feature.size(), 6);
                Object geologicUnit = feature.get("gsml:GeologicUnit");
                assertNotNull(feature.get("gsml:GeologicUnit"));

                JSONArray lithologyAr =
                        (JSONArray)
                                ((JSONObject) geologicUnit)
                                        .getJSONArray("gsml:composition")
                                        .getJSONObject(0)
                                        .getJSONArray("gsml:compositionPart")
                                        .getJSONObject(0)
                                        .get("lithology");
                assertEquals(1, lithologyAr.size());
                JSONObject lithology = lithologyAr.getJSONObject(0);
                // lithology element with cc2.2 is the one matching the filter
                assertEquals("cc.2", lithology.getString("@id"));
                assertEquals("name_2", lithology.getJSONObject("name").getString("value"));
            }
        }
    }

    @Test
    public void testJsonLdWithFilterOnStaticAndDynamic() throws Exception {
        // filter on dynamic:
        // "$filter{xpath('gml:description')='Olivine basalt'},${gml:description}"
        // filter on static
        //  "@codeSpace": "$filter{xpath('../../gml:description')='Olivine
        // basalt'},urn:cgi:classifierScheme:Example:CompositionPartRole"
        setUpComplex("GeologicUnitDynamicStaticFilter.json", geologicUnit);
        StringBuilder sb =
                new StringBuilder("wfs?request=GetFeature&version=2.0")
                        .append("&TYPENAME=gsml:GeologicUnit&outputFormat=")
                        .append("application%2Fld%2Bjson");
        JSONObject result = (JSONObject) getJsonLd(sb.toString());
        Object context = result.get("@context");
        checkContext(context);
        assertNotNull(context);
        JSONArray features = (JSONArray) result.get("features");
        int size = features.size();
        assertEquals(3, size);
        for (int i = 0; i < size; i++) {
            JSONObject geologicUnit = features.getJSONObject(i);
            Object description = geologicUnit.get("description");
            // filter on dynamic
            if (description != null) {
                assertEquals("Olivine basalt", description);
            }
            JSONArray composition = geologicUnit.getJSONArray("gsml:composition");
            for (int j = 0; j < composition.size(); j++) {
                JSONArray compositionPart =
                        (JSONArray) ((JSONObject) composition.get(j)).get("gsml:compositionPart");
                assertTrue(compositionPart.size() > 0);
                JSONObject role = compositionPart.getJSONObject(0).getJSONObject("gsml:role");
                Object codeSpace = role.get("@codeSpace");
                // check filter on the static value
                if (description == null) {
                    assertNull(codeSpace);
                } else {
                    assertNotNull(codeSpace);
                }
            }
        }
    }

    @Test
    public void testJsonLdNotEncodingArrayFilteredOut() throws Exception {
        // filter on dynamic:
        // "$filter{xpath('gml:description')='Olivine basalt'},${gml:description}"
        // filter on static
        //  "@codeSpace": "$filter{xpath('../../gml:description')='Olivine
        // basalt'},urn:cgi:classifierScheme:Example:CompositionPartRole"
        setUpComplex("GeologicUnitDynamicStaticFilter.json", geologicUnit);
        StringBuilder sb =
                new StringBuilder("wfs?request=GetFeature&version=2.0")
                        .append("&TYPENAME=gsml:GeologicUnit&outputFormat=")
                        .append("application%2Fld%2Bjson");
        JSONObject result = (JSONObject) getJsonLd(sb.toString());
        Object context = result.get("@context");
        checkContext(context);
        assertNotNull(context);
        JSONArray features = (JSONArray) result.get("features");
        int size = features.size();
        assertEquals(3, size);
        for (int i = 0; i < size; i++) {
            JSONArray composition = features.getJSONObject(i).getJSONArray("gsml:composition");
            for (int j = 0; j < composition.size(); j++) {
                JSONArray compositionPart =
                        (JSONArray) ((JSONObject) composition.get(j)).get("gsml:compositionPart");
                for (int z = 0; z < compositionPart.size(); z++) {
                    assertNull(compositionPart.getJSONObject(z).get("lithology"));
                }
            }
        }
    }

    @Test
    public void testJsonLdOgcFilterConcatenation() throws Exception {
        // test templates filter concatenation with backward mapping
        // "$filter": "xpath('gml:description') = 'Olivine basalt'",
        setUpComplex("MappedFeatureIteratingAndCompositeFilter.json", mappedFeature);

        String cqlFilter =
                "features.gsml:GeologicUnit.gsml:composition.gsml:compositionPart.gsml:role.value = ";

        // if querying for fictitious component expecting no result because the And condition
        // with the template filter
        JSONArray features = getResultFilterConcatenated(cqlFilter, "'fictitious component'");

        assertEquals(0, features.size());

        // if querying for interbedded component expecting 1 feature as result;
        // this time the result of template filter has this gms:role value.
        JSONArray features2 = getResultFilterConcatenated(cqlFilter, "'interbedded component'");
        assertEquals(1, features2.size());
    }

    private JSONArray getResultFilterConcatenated(String cql_filter, String equalsTo)
            throws Exception {
        StringBuilder sb =
                new StringBuilder("ogc/features/collections/")
                        .append("gsml:MappedFeature")
                        .append("/items?f=application%2Fld%2Bjson")
                        .append("&filter-lang=cql-text")
                        .append("&filter= ")
                        .append(cql_filter)
                        .append(equalsTo);
        JSONObject result = (JSONObject) getJsonLd(sb.toString());
        Object context = result.get("@context");
        checkContext(context);
        assertNotNull(context);
        return result.getJSONArray("features");
    }

    @Override
    protected String getTemplateFileName() {
        return TemplateIdentifier.JSONLD.getFilename();
    }
}
