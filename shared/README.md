# shared/

Optional Kotlin module for types shared between `server/` and `android/`.

Use this for things like:

- Screen ids and route keys
- Action enums (e.g. `nav://home`, `analytics://tap`)
- Versioning constants for the remote protocol

If you don't need shared types yet, leave this folder empty. Don't force it.

## Bootstrap (first time)

```bash
gradle init --type kotlin-library
```

Then wire it as a dependency in both `server/` and `android/`.
