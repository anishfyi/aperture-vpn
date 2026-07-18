# Launcher icon

Aperture VPN uses an **adaptive icon** only:

- Foreground vector: `app/src/main/res/drawable/ic_launcher_foreground.xml`
  (geometric dragon head with aperture-eye cutout)
- Background color: `app/src/main/res/values/colors.xml` → `ic_launcher_background`
- Adaptive XML: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

There are **no** density-specific raster mipmaps (`mipmap-hdpi/ic_launcher.webp`,
`ic_launcher.png`, etc.) in this repo. Android 8+ (minSdk 24) resolves the
adaptive icon from the vector + color above.

## If you add raster mipmaps later

Do **not** hand-edit PNG/WebP launcher binaries. Regenerate them from the vector
sources (or from `docs/favicon.svg` / `docs/logo.svg`) with Android Studio’s
Image Asset Studio, or a scripted export (e.g. `rsvg-convert` / Inkscape) into:

| Density folder     | Typical size |
|--------------------|--------------|
| `mipmap-mdpi`      | 48×48        |
| `mipmap-hdpi`      | 72×72        |
| `mipmap-xhdpi`     | 96×96        |
| `mipmap-xxhdpi`    | 144×144      |
| `mipmap-xxxhdpi`   | 192×192      |

Keep `ic_launcher.xml` / `ic_launcher_foreground.xml` as the source of truth;
rasters are a derived artifact for any legacy or store tooling that still
expects them.

## Web / docs counterparts

| File              | Role                                      |
|-------------------|-------------------------------------------|
| `docs/favicon.svg`| Site favicon (dragon mark)                |
| `docs/logo.svg`   | README lockup (dragon + APRTR wordmark)   |
| `docs/index.html` | Inline dragon mark next to the brand name |
