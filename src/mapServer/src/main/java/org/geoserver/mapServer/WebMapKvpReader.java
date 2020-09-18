package org.geoserver.mapServer;

import org.geoserver.ows.KvpRequestReader;

import java.util.Map;

public class WebMapKvpReader extends KvpRequestReader {

    public WebMapKvpReader(){
        super(WebTileRequest.class);
    }

    public Object createRequest() throws Exception{
        return new WebTileRequest();
    }

    public Object read (Object request, Map kvp , Map rawKvp) throws Exception{
        WebTileRequest ret = (WebTileRequest) request;
        ret.setMapLayer(kvp.get("layer").toString());
        ret.setMapStyle(kvp.get("style").toString());
        ret.setMapTileMatrixset(kvp.get("TileMatrixset").toString());
        ret.setMapFormat(kvp.get("Format").toString());
        ret.setMapTileMatrix(kvp.get("TileMatrix").toString());
        ret.setMapTileRow(kvp.get("TileRow").toString());
        ret.setMapTileCol(kvp.get("TileCol").toString());
        ret.setMapTk(kvp.get("Tk").toString());

        return ret;
    }

}
