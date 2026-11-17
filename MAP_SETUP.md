# Map setup — #18

Uses JXMapViewer2 (LGPL-3.0) for Swing navigation and Mapbox Static Tiles for street imagery.

## Run locally

1. Get a public token from [Mapbox](https://console.mapbox.com/).
2. Reload the Maven project in IntelliJ.
3. Open **Run > Edit Configurations > Main** and set `MAPBOX_ACCESS_TOKEN` under Environment variables.
4. Keep **Store as project file** unchecked and keep the token out of commits.
5. Run `app.Main`.

The map starts near U of T. Drag to pan, scroll to zoom, or use the bottom-right + / − buttons.
Missing tokens show setup instructions. If tiles stay blank, check the token and network connection.

## Map workflow

`HomeView → MapController → DisplayMapInteractor → MapPresenter → MapViewModel → MapPanel`

- `Main` wires dependencies and configures the tile provider.
- `use_case/display_map` prepares the initial location and zoom through input/output boundaries.
- `interface_adapter/map` handles requests, presentation, and observable display state.
- `view` handles Swing rendering, navigation, and tile URL adaptation.

Zoom increases with detail in the view model; `MapPanel` converts it to JXMapViewer's reversed zoom scale.

## Tests

Run `mvn test` with Java 17. The 24 tests run headlessly without live tile requests and cover map initialization, adapter behavior, missing tokens, tile URLs, dragging, zooming, and control positioning after resize.
Check live imagery, attribution links, navigation, resizing, and window closing manually when changing the UI.

## References

- [Mapbox Static Tiles API](https://docs.mapbox.com/api/maps/static-tiles/)
- [JXMapViewer2](https://github.com/msteiger/jxmapviewer2)
- The bundled logo comes from the [Mapbox.js CSS asset](https://api.mapbox.com/mapbox.js/v3.3.1/mapbox.css). Keep the logo and attribution links visible.

## Pin details preview — #21

On `john-21-pin-details`, Add Pin → choose a location → Continue opens the details preview beside the map. The real flow will insert Taysen’s passage selection before this form. Create Pin remains disabled until that handoff and #23 saving are connected.

Thoughts and a PNG/JPEG attachment are optional; choose an audience explicitly. Back retains the draft while revising the location. Cancel clears it. Images are previewed locally (up to 10 MB / 20 million pixels) without uploading or copying files. Those are local preview limits; persistent image storage remains a team decision.

The draft audience is temporary state; mapping it to the shared Pin representation and enforcing read permissions remain integration work. No entity or pin-storage implementation is changed. Implementation, tests, and documentation were created with AI assistance.
