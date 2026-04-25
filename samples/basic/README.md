# samples/basic

End-to-end demo using the template. The simplest thing that proves the loop works:

1. Server emits a screen with a header, a list of three cards, and a button.
2. Android app fetches it, renders it, handles the button tap.
3. Edit the server's composition, refresh on device, see the change.

## Run

```bash
# terminal 1
cd ../../server && ./gradlew run

# terminal 2
# Open android/ in Android Studio and run.
```

## What this sample shows

- Wiring the Ktor route that serves a remote composition.
- Wiring the Android side to fetch and render it.
- A single round-trip for an interaction (button tap → server-acknowledged action).

## What this sample does not show

- Auth.
- Caching / offline.
- Multi-screen navigation.
- Theming negotiation between server and client.

Those are good next steps once the basic loop feels solid.
