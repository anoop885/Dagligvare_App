# Dagligvare App

En Android-app for å sammenligne matpriser og finne tilbud på tvers av norske dagligvarekjeder. Dette er et hobbyprosjekt som er kodet alene og er ikke ment for kommersiell bruk. Appen er under aktiv utvikling og ikke ferdigstilt.

---

## Om prosjektet

Appen henter prisinformasjon fra norske butikkjeder via [Kassalapp API](https://kassal.app/api), som samler daglig oppdaterte priser fra de fleste norske dagligvarekjeder — blant annet Kiwi, Rema 1000, Meny, Spar, Joker, Bunnpris og Coop.

---

## Funksjonalitet

### Implementert
- Produktsøk på tvers av alle butikker
- Prissammenligning per produkt — alle butikker sortert fra billigst til dyrest
- Tilbudsdeteksjon basert på prishistorikk de siste 30 dagene 
- Handlekurv med mengdekontroll, gruppering per butikk og totalsum per butikk
- Handlekurven lagres lokalt og overlever app-restart
- Produkter uten prisoppdatering de siste 30 dagene filtreres bort

### Planlagt
- Tilbudsvarsler for favorittprodukt
- Lage tilbudsside som viser de største tilbudene for øyeblikket
- Kategorifiltrering på tilbudssiden
- Forslag til hvilken butikk som er billigst totalt for handlekurven

---

## Teknologi

| Komponent | Teknologi |
|---|---|
| Språk | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arkitektur | MVVM + Clean Architecture |
| Dependency Injection | Koin |
| Nettverkskall | Retrofit + OkHttp |
| Bilder | Coil (med SVG-støtte) |
| Lokal lagring | DataStore Preferences |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 36 |

---

## Prosjektstruktur

```
app/src/main/java/no/uio/ifi/in2000/dagligvareapp/
├── data/
│   ├── local/          # CartStorage — DataStore for handlekurv
│   ├── remote/         # KassalappApi (Retrofit), NetworkModule, DTOer
│   └── repository/     # ProductRepositoryImpl — API-kall og datamapping
├── di/                 # Koin-moduler
├── domain/
│   ├── model/          # CartItem, Deal, PriceComparison, Product, StorePrice
│   └── repository/     # ProductRepository (interface)
├── presentation/
│   ├── cart/           # CartScreen + CartViewModel
│   ├── deals/          # DealsScreen + DealsViewModel
│   ├── navigation/     # AppNavigation, bottom nav
│   └── search/         # SearchScreen + SearchViewModel + komponenter
└── ui/theme/           # Farger, typografi, tema
```

---

## Kom i gang

### Krav
- Android Studio (siste stabile versjon)
- Android SDK API 24 eller nyere
- Gratis API-nøkkel fra [kassal.app](https://kassal.app/api)

### Oppsett

1. Klone repoet:
   ```bash
   git clone https://github.com/anoop885/Dagligvare_App.git
   ```

2. Åpne prosjektet i Android Studio

3. Opprett filen `local.properties` i roten av prosjektet og legg til:
   ```
   KASSAL_API_KEY=din_api_nøkkel_her
   ```
   Gratis API-nøkkel får du ved å registrere deg på [kassal.app](https://kassal.app/api).

4. Bygg og kjør appen på en emulator eller fysisk enhet

`local.properties` er lagt til i `.gitignore` og skal ikke committes.
