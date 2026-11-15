# Local map setup — issue #18

This implementation uses JXMapViewer2 (LGPL-3.0) for Swing navigation and Mapbox Static Tiles for street imagery.

1. Sign in to https://console.mapbox.com/ and find Access tokens. Ask the team whether an account/token already exists before creating one.
2. Obtain a public token with access to the street style. Follow the account's current pricing and usage limits.
3. In IntelliJ, reload the Maven project after opening pom.xml.
4. Open Run > Edit Configurations > Main. Under Modify options enable Environment variables if hidden.
5. Add MAPBOX_ACCESS_TOKEN with your public token as its value. Keep the configuration local; do not store it as a project file or commit the token.
6. Run app.Main. The map starts near the University of Toronto. Drag to pan, scroll to zoom, or use the + / − buttons.

Without a token the home screen displays setup instructions. Invalid tokens or network failures can leave map tiles blank; check the account and network connection. Never paste logs containing authenticated tile URLs into an issue.

## Verification

Run `mvn test` using the agreed JDK (locally verified with Java 17). Tests run headlessly and do not request live map tiles.
Tests cover home composition, missing credentials, tile coordinate/zoom mapping, token encoding, drag gestures, wheel zoom, and button zoom limits.
Manually check live imagery, attribution links, drag, wheel/buttons, resize, and close after supplying your token. Live imagery has not been verified without credentials.

## Scope and coordination

No pin creation, storage, filtering, or account logic is included. HomeView arranges the injected map component; Main handles configuration and startup; MapPanel owns presentation interactions; MapboxTileFactoryInfo adapts tile URLs.
The JUnit/Surefire additions overlap issue #9 and should be coordinated before opening the PR. Maven still targets Java 11 pending the team's Java version decision in #9.
Do not close #18 until the live map and remaining manual checks are verified.

Sources: https://docs.mapbox.com/api/maps/static-tiles/ and https://github.com/msteiger/jxmapviewer2

The bundled Mapbox logo was rasterized from the official Mapbox.js 3.3.1 CSS logo asset: https://api.mapbox.com/mapbox.js/v3.3.1/mapbox.css . Keep it and the linked attribution visible.
