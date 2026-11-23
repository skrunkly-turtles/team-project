# Map setup — #18

Uses JXMapViewer2 (LGPL-3.0) for Swing navigation and Mapbox Static Tiles for street imagery.

## Run locally

1. Get a public token from [Mapbox](https://console.mapbox.com/).
2. Reload the Maven project in IntelliJ.
3. Open **Run > Edit Configurations > Main** and set `MAPBOX_ACCESS_TOKEN` under Environment variables.
4. Keep **Store as project file** unchecked and keep the token out of commits.
5. Run `app.Main`, then create an account or log in to open the home map.

The map starts near U of T. Drag to pan, scroll to zoom, or use the bottom-right + / − buttons.
Missing tokens show setup instructions. If tiles stay blank, check the token and network connection.

## Map workflow

`HomeView → MapController → DisplayMapInteractor → MapPresenter → MapViewModel → MapPanel`

- `Main` starts the application; `AppBuilder` wires account navigation, map dependencies, and the tile provider.
- `use_case/display_map` prepares the initial location and zoom through input/output boundaries.
- `interface_adapter/map` handles requests, presentation, and observable display state.
- `view` handles Swing rendering, navigation, and tile URL adaptation.

Zoom increases with detail in the view model; `MapPanel` converts it to JXMapViewer's reversed zoom scale.

## Tests

Run `mvn test` with Java 17. The combined 48 tests pass on Java 17. Map tests run headlessly without live tile requests and cover initialization, adapters, missing tokens, tile URLs, dragging, zooming, and control positioning after resize. Integration tests cover signup/login → home map → profile → map → logout with temporary account files.
Check live imagery, attribution links, navigation, resizing, and window closing manually when changing the UI.

## References

- [Mapbox Static Tiles API](https://docs.mapbox.com/api/maps/static-tiles/)
- [JXMapViewer2](https://github.com/msteiger/jxmapviewer2)
- The bundled logo comes from the [Mapbox.js CSS asset](https://api.mapbox.com/mapbox.js/v3.3.1/mapbox.css). Keep the logo and attribution links visible.
