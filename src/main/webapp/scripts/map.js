/**
* external GIS service funtions
*/
var map;

function getUrlParameter( name ) {
            name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
            var regexS = "[\\?&]"+name+"=([^&#]*)";
            var regex = new RegExp( regexS );
            var results = regex.exec( window.location.href );
            if( results == null )
                return "";
            else
                return results[1];
}

function initialize() {
          var obj = {
                  extent: new esri.geometry.Extent ($extX1, $extY1, $extX2, $extY2, new esri.SpatialReference({wkid:4326})),
                  showInfoWindowOnClick:true
          };

          map = new esri.Map("map", obj);
          var layer = new esri.layers.ArcGISTiledMapServiceLayer("http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer");
          map.addLayer(layer);

          dojo.connect(map, "onLoad", projectToWebMercator);
}
function addPointToMap(longitude, latitude) {
            var point = new esri.geometry.Point($longitude, $latitude, new esri.SpatialReference({ wkid: 4326 }));
            var symbol = new esri.symbol.SimpleMarkerSymbol(esri.symbol.SimpleMarkerSymbol.STYLE_SQUARE, 12,
                new esri.symbol.SimpleLineSymbol(esri.symbol.SimpleLineSymbol.STYLE_SOLID, new
                dojo.Color([0,0,0]), 3), new dojo.Color([0,0,0,0.5]));
            var graphic = new esri.Graphic(point, symbol);
            map.graphics.add(graphic);
}

        /**
         * Go through all coordinates and set points to the map
         */
function projectToWebMercator(evt) {
             map.graphics.clear();

            //var i=0;
            //for (i=0;i<=3;i++)
            //{
            //}
            addPointToMap($longitude, $latitude);

}

function loadMap() {
        dojo.require("esri.map");
        dojo.require("esri.tasks.geometry");

        var longitude = null;
        $longitude=getUrlParameter("longitude");

        var latitude = null;
        $latitude= getUrlParameter("latitude");

        var extX1= null,
            extY1 = null,
            extX2 = null,
            extY2 = null;

        $extX1=parseFloat($longitude)-0.5;
        $extX2=parseFloat($longitude)+0.5;
        $extY1=parseFloat($latitude)-0.5;
        $extY2=parseFloat($latitude)+0.5;


        var pos = null;

        dojo.addOnLoad(initialize);

}
