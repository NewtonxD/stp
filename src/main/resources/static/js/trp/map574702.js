//------------------------------------------------------------------------------
//------------FUNCIONES IMPORTANTES---------------------------------------------
//------------------------------------------------------------------------------
function onMediaQueryChange(event) {
    if (event.matches) {
        document.documentElement.style.setProperty("--min-width", "true");
    } else {
        document.documentElement.style.removeProperty("--min-width");
    }
}

//------------------------------------------------------------------------------
function generateClientId() {
    var millis = new Date().getTime();
    var randomNum = Math.floor(Math.random() * 1000000); // Generate a random number with 6 digits
    return millis + '-' + randomNum;
}
//------------------------------------------------------------------------------

function closePdaSSE(idPda) {

    if (ssePda !== null && ssePda !== undefined) {
        ssePda.close();
        $.get(`${SERVER_IP}/p/see/pdaInfo/close?clientId=${clientId}-${idPda}`);
    }
}
//------------------------------------------------------------------------------
function fitBoundsPadding(e) {
    const boxInfoWith = document.querySelector(".leaflet-popup-content-wrapper").offsetWidth;

    const featureGroup = L.featureGroup([e.target]).addTo(map);

    const getPropertyWidth = document.documentElement.style.getPropertyValue("--min-width");

    map.fitBounds(featureGroup.getBounds(), {
        paddingTopLeft: [getPropertyWidth ? -boxInfoWith : 0, 10]
    });
}
//------------------------------------------------------------------------------
function hideAllExcept(specificRouteId, specificParadeId, specificVehicleId) {

    const route = routePolylineMap.get(specificRouteId);
    const parade = markerMap.get(specificParadeId);
    const vehicle = vehicleMap.get(specificVehicleId);

    map.eachLayer(function (layer) {
        if (layer instanceof L.Polyline && layer !== route) {
            map.removeLayer(layer);
        } else if (layer instanceof L.Marker && (layer !== vehicle && layer !== parade)) {
            map.removeLayer(layer);
        }

    });

}
//------------------------------------------------------------------------------
function showAllExcept(specificRouteId, specificParadeId, specificVehicleId) {

    const route = routePolylineMap.get(specificRouteId);
    const vehicle = vehicleMap.get(specificVehicleId);
    const parade = markerMap.get(specificParadeId);

    routePolylineMap.forEach((v, i) => {
        if (v !== route) map.addLayer(v);
    });

    vehicleMap.forEach((v, i) => {
        if (v !== vehicle) map.addLayer(v);
    });

    markerMap.forEach((v, i) => {
        if (v !== parade) map.addLayer(v);
    });

}
//------------------------------------------------------------------------------
const compareToArrays = (a, b) => JSON.stringify(a) === JSON.stringify(b);
//------------------------------------------------------------------------------
function getCenterOfMap() {
    const buttonBackToHome = document.querySelector(".back-to-home");
    buttonBackToHome.classList.remove("hidden");

    buttonBackToHome.addEventListener("click", () => {
        map.flyTo([MY_LAT, MY_LON], zoom);
    });

    map.addEventListener('moveend', () => {
        const {lat: latCenter, lng: lngCenter} = map.getCenter();
        
        const latC = latCenter.toFixed(3) * 1;
        const lngC = lngCenter.toFixed(3) * 1;

        const defaultCoordinate = [+MY_LAT.toFixed(3), +MY_LON.toFixed(3)];

        const centerCoordinate = [latC, lngC];

        if (compareToArrays(centerCoordinate, defaultCoordinate)) {
            buttonBackToHome.classList.add("hidden");
        }
    });
}

//------------------------------------------------------------------------------
function vhlToggleClick(e) {
    let buttonEl = e.target;
    let buttonId = buttonEl.getAttribute('data-customId');
    const [rta, vhl] = buttonId.split('-').slice(1);
    const isActive = buttonEl.classList.contains('active');
    const type = buttonEl.getAttribute('data-type');
    const pdaId = "0";
    const polyline = routePolylineMap.get(rta);
    const vehicle = vehicleMap.get(vhl);
    if (isActive) {

        const newColor = routeColorMap.get(rta).replace(",0.7)", ",1)");
        hideAllExcept(rta, pdaId, vhl);

        buttonEl.style.backgroundColor = newColor;
        if (polyline) {
            polyline.setStyle({color: newColor, weight: 9});
            map.fitBounds(polyline.getBounds());
        }

    } else {

        buttonEl.style.backgroundColor = routeColorMap.get(rta);
        if (polyline) {
            polyline.setStyle({color: routeColorMap.get(rta), weight: 7});
        }
        map.flyTo(vehicle.getLatLng(), 18);
        showAllExcept(rta, pdaId, vhl);

    }
}

//------------------------------------------------------------------------------
function pdaToggleClick(e) {
    let buttonEl = e.target;
    let buttonId = buttonEl.getAttribute('data-customId');
    const [rta, vhl] = buttonId.split('-').slice(1);
    const isActive = buttonEl.classList.contains('active');
    const polyline = routePolylineMap.get(rta);
    const pdaId = buttonEl.getAttribute('data-pdaId');
    const pda = markerMap.get(pdaId);
    if (isActive) {

        const newColor = routeColorMap.get(rta).replace(",0.7)", ",1)");

        hideAllExcept(rta, pdaId, vhl);

        buttonEl.style.backgroundColor = newColor;
        if (polyline) {
            polyline.setStyle({color: newColor, weight: 9});
        }

        const vhlLat = parseFloat(buttonEl.getAttribute('data-vhl-lat'));
        const vhlLon = parseFloat(buttonEl.getAttribute('data-vhl-lon'));
        const vehicleLatLng = [vhlLat, vhlLon];
        map.fitBounds([pda.getLatLng(), vehicleLatLng]);

    } else {

        buttonEl.style.backgroundColor = routeColorMap.get(rta);
        if (polyline) {
            polyline.setStyle({color: routeColorMap.get(rta), weight: 7});
        }
        map.flyTo(pda.getLatLng(), 18);

        showAllExcept(rta, pdaId, vhl);
    }
}

//------------------------------------------------------------------------------
function updateTransportMap( data, first = false) {

    if (!first) removeVehicle(data);


    for (let i = 0; i < data.length; i++) {

        const placa = data[i][0];
        const ruta = data[i][1];
        const lon = data[i][2];
        const lat = data[i][3];
        const orientation = data[i][4];
        const velocidad = data[i][5] * 3.6;

        const color = routeColorMap.get(ruta);

        const popupContent = getVehiclePopup( placa, ruta, lat, lon, orientation, color);


        if (first) {
            const popup = L.popup({
                pane: "fixed",
                className: "popup-fixed test",
                autoPan: false
            }).setContent(popupContent);

            const marker = L.marker([lat, lon], {icon: busIcon, })
                    .bindPopup(popup)
                    .addTo(map);

            vehicleMap.set(placa, marker);
        } else {
            const vhl = vehicleMap.get(placa);

            if (vhl === null || vhl === undefined) {
                const popup = L.popup({
                    pane: "fixed",
                    className: "popup-fixed test",
                    autoPan: false
                }).setContent(popupContent);

                const marker = L.marker([lat, lon], {icon: busIcon, })
                        .bindPopup(popup)
                        .addTo(map);

                vehicleMap.set(placa, marker);

            } else {
                vhl.slideTo([lat, lon], {duration: 2000});
                vhl.setPopupContent(popupContent);
            }
        }
        
    }
}
//------------------------------------------------------------------------------

function removeVehicle(data) {
    const transformedData = data.map(item => item[0]);
    for (let key of vehicleMap.keys()) {
        if (!transformedData.includes(key)) {
            map.removeLayer(vehicleMap.get(key));
            vehicleMap.delete(key);
        }
    }
}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//---------DECLARACION DEL MAPA-------------------------------------------------
//------------------------------------------------------------------------------
var map;

let config = {
    minZoom: 12,
    maxZoom: 18,
    preferCanvas: true,
    attributionControl: false
};

let zoom = 16;

if (map != undefined) map.remove();
map = L.map("map", config).setView([BASE_LAT, BASE_LNG], zoom);

L.tileLayer.fetch(TILE_API_IP).addTo(map);

L.control.scale({imperial: false, }).addTo(map);
//------------------------------------------------------------------------------
//----------PANEL PARA LA INFORMACION DE LOS OBJETOS----------------------------
//------------------------------------------------------------------------------
var pane = map.createPane("fixed", document.getElementById("map"));

const markerMap = new Map();
const routeColorMap = new Map();
const routePolylineMap = new Map();

const vehicleMap = new Map();

const homeSvg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32"><path d="M32 18.451L16 6.031 0 18.451v-5.064L16 .967l16 12.42zM28 18v12h-8v-8h-8v8H4V18l12-9z" /></svg>';
const homeBtnSvg = '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="16" height="16" viewBox="0 0 32 32"><path d="M32 18.451L16 6.031 0 18.451v-5.064L16 .967l16 12.42zM28 18v12h-8v-8h-8v8H4V18l12-9z" /></svg>';
const infoBtnSvg ='<svg xmlns="http://www.w3.org/2000/svg" class="icon" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0,0,256,256" width="16" height="16"><g fill-rule="nonzero" stroke="none" stroke-width="1" stroke-linecap="butt" stroke-linejoin="miter" stroke-miterlimit="10" stroke-dasharray="" stroke-dashoffset="0" font-family="none" font-weight="none" font-size="none" text-anchor="none" style="mix-blend-mode: normal"><g transform="scale(5.12,5.12)"><path d="M25,2c-12.6907,0 -23,10.3093 -23,23c0,12.69071 10.3093,23 23,23c12.69071,0 23,-10.30929 23,-23c0,-12.6907 -10.30929,-23 -23,-23zM25,4c11.60982,0 21,9.39018 21,21c0,11.60982 -9.39018,21 -21,21c-11.60982,0 -21,-9.39018 -21,-21c0,-11.60982 9.39018,-21 21,-21zM25,11c-1.65685,0 -3,1.34315 -3,3c0,1.65685 1.34315,3 3,3c1.65685,0 3,-1.34315 3,-3c0,-1.65685 -1.34315,-3 -3,-3zM21,21v2h1h1v13h-1h-1v2h1h1h4h1h1v-2h-1h-1v-15h-1h-4z"></path></g></g></svg>';
const busIcon = L.icon({
    iconUrl: `${SERVER_IP}/content/css/images/bus-50.png`,
    iconSize: [20, 20], // size of the icon
});

const redMarkerIcon = L.icon({
    iconUrl: `${SERVER_IP}/content/css/images/marker-icon-2x-red.png`,
    iconSize: [25, 41],
    iconAnchor: [12, 41] // size of the icon
});

const invisibleMarkerIcon = L.icon({
    iconUrl: `${SERVER_IP}/content/css/images/invisible.webp`,
    iconSize: [1, 1],
    iconAnchor: [1, 1] // size of the icon
});

const popupInfo = L.popup({
    pane: "fixed",
    className: "popup-fixed test",
    autoPan: false
}).setContent(getInformationPopup());

const markerInfo=L.marker([1,1],{icon:invisibleMarkerIcon}).bindPopup(popupInfo).addTo(map);

const infoButton = L.Control.extend({
    options: {
        position: 'topleft'
    },
    onAdd: function() {
        
        let container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
        
        let button = L.DomUtil.create('a', 'button-information', container);
        button.innerHTML = infoBtnSvg;
        button.href = '#';
        button.title = 'Información';                
        L.DomEvent.on(button, 'click', function(e) {
            e.stopPropagation();
            e.preventDefault();
            if(markerInfo.isPopupOpen()){
                markerInfo.closePopup();
            }else{
                var layerPoint = map.latLngToLayerPoint(map.getBounds().getNorthEast());
                layerPoint.x -= 60; 
                layerPoint.y += 60; 
                var newLatLng = map.layerPointToLatLng(layerPoint);
                markerInfo.setLatLng(newLatLng).openPopup();
            }
        });

        return container;
    }
});

// Add the custom button to the map
map.addControl(new infoButton());


fetch(`${SERVER_IP}/API/trp/getStatic`, { method: 'GET'}).then(response => response.json()).then(res => {
    if (res.rutasLoc !== null && res.rutasLoc !== undefined) {
        const routePointsMap = new Map();

        res.rutasLoc.forEach(loc => {
            if (!routePointsMap.has(loc.rta)) {
                routePointsMap.set(loc.rta, []);
            }
            routePointsMap.get(loc.rta).push([loc.lat, loc.lon]);
        });

        routePointsMap.forEach((coordinates, routeName) => {

            if (coordinates.length > 0) {

                const color = getRandomColor();
                let rutaInfo;
                for (let i = 0; i < res.rutasInfo.length; i++) {

                    if (routeName === res.rutasInfo[i][0]) {
                        rutaInfo = res.rutasInfo[i];
                        i += res.rutasInfo.length;
                    }

                }

                const desde = rutaInfo[1];
                const hasta = rutaInfo[2];
                const distancia_total = rutaInfo[3];
                const vehiculos_activos = rutaInfo[4];

                const contentPopup = getRoutePopup( routeName, desde, hasta, distancia_total, vehiculos_activos);

                const popup = L.popup({
                    pane: "fixed",
                    className: "popup-fixed test",
                    autoPan: false
                }).setContent(contentPopup);

                const p = L.polyline(coordinates, {
                    color: color,
                    weight: 7
                }).bindPopup(popup).addTo(map);

                routePolylineMap.set(routeName, p);
                routeColorMap.set(routeName, color);

            }
            ;

        });
    }

    if (res.vehiculosLoc !== null && res.vehiculosLoc !== undefined) {

        updateTransportMap(res.vehiculosLoc, true);

    }

    if (res.paradas !== null && res.paradas !== undefined) {
        for (let i = 0; i < res.paradas.length; i++) {

            const popupContent = getParadePopup( res.paradas[i].id, res.paradas[i].lat, res.paradas[i].lon, res.paradas[i].dsc);

            const popup = L.popup({
                pane: "fixed",
                className: "popup-fixed test",
                autoPan: false
            }).setContent(popupContent);

            const marker = L.marker([res.paradas[i].lat, res.paradas[i].lon])
                    .bindPopup(popup)
                    .addTo(map);

            const key = `${res.paradas[i].lat},${res.paradas[i].lon}`;
            markerMap.set(key, marker);

        }
    }

}).catch(error => console.error('Error fetching data:', error));

//------------------------------------------------------------------------------
//----------PERMISO DE LOCALIZACION---------------------------------------------
//------------------------------------------------------------------------------
if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function (position) {
        // PERMISO ACEPTADO: MARCADOR EN LOCALIZACION USUARIO + BOTON DE CASITA PARA VOLVER
        MY_LAT = position.coords["latitude"];
        MY_LON = position.coords["longitude"];
        
        const marker = L.marker([MY_LAT, MY_LON], {
            icon: L.divIcon({
                className: "custom-icon-marker",
                iconSize: L.point(20, 20),
                html: homeSvg
            })
        }).addTo(map);

        const popup = L.popup({
            pane: "fixed",
            className: "popup-fixed test",
            autoPan: false
        }).setContent(getMyLocationPopup(true));

        marker.bindPopup(popup).on("click", fitBoundsPadding);
        map.setView([MY_LAT, MY_LON], zoom);


        const customControl = L.Control.extend({
            options: {position: "topleft"},
            onAdd: function () {
                const btn = L.DomUtil.create("a");
                btn.title = "Inicio";
                btn.innerHTML = homeBtnSvg;
                btn.className += "leaflet-bar leaflet-control back-to-home hidden";
                return btn;
            },
        });
            
        map.addControl(new customControl());
        map.on("moveend", getCenterOfMap);
    },
    function (error) {
        // LOCALIZACION POR DEFECTO AL RECHAZAR PERMISOS
        const marker = L.marker([BASE_LAT, BASE_LNG]).addTo(map);

        const popup = L.popup({
            pane: "fixed",
            className: "popup-fixed test",
            autoPan: false,
        }).setContent(getMyLocationPopup(false));

        marker.bindPopup(popup).on("click", fitBoundsPadding);
        map.setView([BASE_LAT, BASE_LNG], zoom);
    },
    {
        enableHighAccuracy: true
    });
}

map.on('popupopen', function (event) {
    const popupNode = event.popup._contentNode.querySelector('div[data-id]');
    const id = popupNode.getAttribute('data-id');
    const type = popupNode.getAttribute('data-type');
    
    if(type!=='info')
        map.flyTo(event.popup.getLatLng(), 18);

    let data = {id: id, type: type};

    if (type === "myloc") {
        data["lon"] = popupNode.getAttribute('data-lon');
        data["lat"] = popupNode.getAttribute('data-lat');
    }

    if (type === "pda") {
        const lon = popupNode.getAttribute('data-lon');
        const lat = popupNode.getAttribute('data-lat');
        let idPda = `${lat},${lon}`;
        const marker = markerMap.get(idPda);

        if (marker) {
            marker.setIcon(redMarkerIcon);
        }
    }

    // Example of calling another endpoint
    if (type === "myloc" || type === "pda")fetch(`${SERVER_IP}/API/trp/getInfoObject`, {method: 'POST', headers: {"Content-Type": "application/json"}, body: JSON.stringify(data)}).then(response => response.json())
    .then(res => {

        if (res.locInfo && res.locInfo.length > 0) {
            const locInfo = res.locInfo[0];
            const locId = locInfo[0];
            const locName = locInfo[1];
            const locLat = locInfo[3];
            const locLon = locInfo[2];
            const locDistance = locInfo[4];

            const newContent = getNearestParadePopup(locId,locLat,locLon,locName,locDistance);

            // Append the new content to the existing popup content
            popupNode.innerHTML += newContent;

            // Add event listener to the link
            document.getElementById("center-link-pd" + locId).addEventListener('click', function (e) {
                e.preventDefault();
                const lat = parseFloat(this.getAttribute('data-lat'));
                const lon = parseFloat(this.getAttribute('data-lon'));
                map.flyTo([lat, lon], 18); // Adjust the zoom level as needed
                //
                // Find the marker using the stored reference and open its popup
                const marker = markerMap.get(`${lat},${lon}`);
                if (marker) {
                    marker.openPopup();
                }
            });

        } else if (res.pdaInfo && res.pdaInfo.length > 0) {

            const lon = popupNode.getAttribute('data-lon');
            const lat = popupNode.getAttribute('data-lat');
            const idPda = `${lat},${lon}`;

            for (let i = 0; i < res.pdaInfo.length; i++) {
                const data = res.pdaInfo[i];
                const vhl = data[1];
                const rta = data[0];
                const velocidad = data[2];
                const distancia = data[3];
                const vhlLat = data[4];
                const vhlLon = data[5];
                let minutos = Math.floor(((distancia / velocidad) - 3) / 60);
                minutos = (minutos<=0) ? 0 : minutos;
                const color = routeColorMap.get(rta);
                const idElement = `custom-${rta}-${vhl}`.replace(' ', '').replace(/[`~!@#$%^&*()_|+\=?;:'",.<>\{\}\[\]\\\/]/gi, '');

                const newContent = getParadeInfoPopup( idPda, rta, vhl, vhlLat, vhlLon, idElement, color, minutos);

                popupNode.innerHTML += newContent;

            }

            ssePda = new EventSource(`${SERVER_IP}/p/see/pdaInfo?clientId=${clientId}-${id}`, {withCredentials: false});

            ssePda.onmessage = function (event) {
                const dataa = JSON.parse(event.data);
                for (let i = 0; i < dataa.length; i++) {
                    const data = dataa[i];
                    const vhl = data[1];
                    const rta = data[0];
                    const velocidad = data[2];
                    const distancia = data[3];
                    const vhlLat = data[4];
                    const vhlLon = data[5];
                    let minutos = Math.floor(((distancia / velocidad) - 3) / 60);
                    if (minutos <= 0)
                        minutos = 0;
                    const color = routeColorMap.get(rta);
                    const idElement = `custom-${rta}-${vhl}`.replace(' ', '').replace(/[`~!@#$%^&*()_|+\=?;:'",.<>\{\}\[\]\\\/]/gi, '');
                    $('#' + idElement).html(`Ruta: ${rta}&nbsp....&nbsp<b>${vhl !== null ? minutos + " min" : "n/a"}</b>`);
                    $('#' + idElement).attr("data-vhl-lat", `"${vhlLat}"`);
                    $('#' + idElement).attr("data-vhl-lon", `"${vhlLon}"`);
                }
            };

        }
            
    }).catch(error => console.error('Error fetching details:', error));

});

map.on('popupclose', function (event) {
    const popupNode = event.popup._contentNode.querySelector('div[data-id]');
    const id = popupNode.getAttribute('data-id');
    const type = popupNode.getAttribute('data-type');

    if (type === "pda" || type === "vhl") {
        const lat = popupNode.getAttribute('data-lat');
        const lon = popupNode.getAttribute('data-lon');

        if (type === "pda") {
            closePdaSSE(id);
            const marker = markerMap.get(`${lat},${lon}`);

            if (marker) {
                marker.setIcon(new L.Icon.Default()); // Revert to default marker icon
            }
        }

        document.querySelectorAll('.custom-toggle-button,.custom-vhl-toggle-button').forEach(button => {
            const buttonId = button.getAttribute('data-customId');
            const [rta, vhl] = buttonId.split('-').slice(1);

            // Revert polyline color
            const polyline = routePolylineMap.get(rta);
            if (polyline) {
                polyline.setStyle({color: routeColorMap.get(rta),
                    weight: 7});
            }

            const pdaId = type === "pda" ? popupNode.getAttribute('data-pdaId') : "0";
            showAllExcept(rta, pdaId, vhl);

        });
    }
    if(type==='info') markerInfo.setLatLng([1,1]);
});

//------------------------------------------------------------------------------
//-----------SSE ---------------------------------------------------------------
//------------------------------------------------------------------------------

var clientId = "";
var errorToastTimeout = null;
var sse = null;
var ssePda = null;

clientId = generateClientId();
sse = new EventSource(`${SERVER_IP}/p/see/trpInfo?clientId=${clientId}`, {withCredentials: false});

sse.onmessage = function (event) {
    const data = JSON.parse(event.data);
    updateTransportMap(data);
};

sse.onerror = function (event) {
    if (!errorToastTimeout) {
        showToast('Conexión inestable. Verifica tu Internet y refresca la plataforma.', 'warning');
        errorToastTimeout = setTimeout(() => {
            errorToastTimeout = null;
        }, ERROR_TOAST_INTERVAL);
    }
};


//------------------------------------------------------------------------------
//-----------MEDIA QUERY--------------------------------------------------------
//------------------------------------------------------------------------------
const mediaQueryList = window.matchMedia("(min-width: 700px)");

mediaQueryList.addEventListener('change', (event) => onMediaQueryChange(event));

onMediaQueryChange(mediaQueryList);
