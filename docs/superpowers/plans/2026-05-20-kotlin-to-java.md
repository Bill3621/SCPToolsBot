# Kotlin-to-Java Conversion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the entire SCPToolsBot project from Kotlin to Java, replacing all Kotlin-specific libraries with Java equivalents.

**Architecture:** The project is a Discord bot (JDA) for SCP: Secret Laboratory server management. It uses Ktor for HTTP, Exposed for database access, kotlinx-serialization for config/translation parsing, and Kotlin coroutines for async tasks. All will be replaced with Java equivalents: Java HttpClient (or Ktor Java API), plain JDBC, Jackson (with SnakeYAML), and ScheduledExecutorService/CompletableFuture.

**Tech Stack:** Java 22, JDA 5.x, Jackson + SnakeYAML, JDBC, Java HttpClient, Logback, Gradle (Groovy DSL), Shadow plugin

---

## Library Replacements

| Kotlin Library | Java Replacement |
|---|---|
| `kotlinx-serialization` + `@Serializable` | Jackson (`@JsonProperty`, `ObjectMapper`) |
| `kaml` (Kotlin YAML) | Jackson + SnakeYAML (`YAMLFactory`) |
| `jda-ktx` (Kotlin JDA extensions) | Plain JDA API |
| `kotlinx-coroutines` | `ScheduledExecutorService` + `CompletableFuture` |
| `config-lite` | Custom Jackson-based config loader |
| `exposed` (Kotlin SQL DSL) | Plain JDBC (`PreparedStatement`, `ResultSet`) |
| `ktor-client` (Kotlin HTTP) | Java `HttpClient` |
| `ktor-server-netty` | Java `HttpServer` or embedded Netty with Java API |
| Kotlin `data class` | Java `record` |
| Kotlin `companion object` | Java `static` members |
| Kotlin `enumValues<T>()` | Java `T.values()` |
| Kotlin `when` | Java `switch` / `if-else` |
| Kotlin `?.let` / `?:` | Java `if (x != null)` / ternary |
| Kotlin string templates | `String.format()` or concatenation |
| `secretlab-kotlin` | Keep as dependency (JVM library callable from Java) |
| `updater` | Keep as dependency (JVM library callable from Java) |

## Key Conversion Patterns

### Data Classes → Records
```java
// Kotlin: data class Settings(val token: String, ...)
// Java:
public record Settings(
    @JsonProperty("token") String token,
    ...
) {}
```

### Enums (straightforward)
```java
// Kotlin: enum class TicketType { GENERAL, REPORT, ... }
// Java:
public enum TicketType { GENERAL, REPORT, ... }
```

### JDA-KTX → Plain JDA
```java
// Kotlin: api.listener<SlashCommandInteractionEvent> { event -> ... }
// Java: Override onSlashCommandInteraction in ListenerAdapter

// Kotlin: light(config.settings.token, enableCoroutines = true) { ... }
// Java: JDABuilder.createLight(token, intents).setActivity(...).build()

// Kotlin: Embed { color = 0xE74D3C; title = "..." }
// Java: new EmbedBuilder().setColor(0xE74D3C).setTitle("...").build()

// Kotlin: event.reply_("", listOf(embed))
// Java: event.replyEmbeds(embed).queue()
```

### Coroutines → ScheduledExecutorService
```java
// Kotlin: coroutineScope.launch { while(isActive) { task(); delay(period) } }
// Java: scheduler.scheduleAtFixedRate(() -> task(), 0, periodMillis, TimeUnit.MILLISECONDS)
```

### Exposed → JDBC
```java
// Kotlin: transaction(database) { SchemaUtils.create(TicketTable.Tickets) }
// Java: try (Statement stmt = connection.createStatement()) { stmt.execute("CREATE TABLE IF NOT EXISTS ...") }

// Kotlin: TicketTable.select { ... }
// Java: try (PreparedStatement ps = connection.prepareStatement("SELECT ... WHERE ...")) { ... }
```

### Config Loading
```java
// Kotlin: ConfigLite.load<Settings>(fileName)
// Java: new ObjectMapper(new YAMLFactory()).readValue(new File(path), Settings.class)
```

### Ktor Client → Java HttpClient
```java
// Kotlin: HttpClient(CIO).get("url")
// Java: HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create("url")).GET().build(), BodyHandlers.ofString())
```

---

## Task Breakdown (Dependency Order)

### Task 1: Build System
**Files:**
- Replace: `build.gradle.kts` → `build.gradle`
- Replace: `settings.gradle.kts` → `settings.gradle`
- Delete: `src/main/kotlin/` directory after conversion

- [ ] Step 1: Create `build.gradle` with Java plugin, same dependencies minus Kotlin-specific ones
- [ ] Step 2: Create `settings.gradle`
- [ ] Step 3: Delete old Kotlin DSL files

### Task 2: Enums (14 files, no dependencies)
**Files:** All files in `enums/` directories across packages
- [ ] Convert all enum files

### Task 3: Data Classes / Records (25+ files)
**Files:** All files in `data/` directories, plus `CommandList.kt`, `LaunchConfiguration.kt`, `LaunchArguments.kt`
- [ ] Convert to Java records with Jackson annotations

### Task 4: Utility Classes (8 files)
**Files:** ColorTool, DurationParser, GlobalVariables, CoroutineScopes→ExecutorScopes, Timer→ScheduledTimer, ConfigPaths, RollPerSessionTriggeringPolicy, DCColor enum
- [ ] Convert utility classes

### Task 5: Database Layer (14 files)
**Files:** DatabaseManager, XPDatabaseHandler, all table classes in `tables/`
- [ ] Rewrite all Exposed table operations as plain JDBC

### Task 6: Configuration Layer (5 files)
**Files:** ConfigurationManager, ConfigFileHandler, TranslationFileHandler, ConfigPaths, and config loading
- [ ] Replace ConfigLite with Jackson-based loading
- [ ] Replace kaml with Jackson+SnakeYAML

### Task 7: API Layer (4 files)
**Files:** Discord API client, WebServerManager
- [ ] Replace Ktor client with Java HttpClient
- [ ] Replace Ktor server with Java HttpServer or com.sun.net.httpserver

### Task 8: Updates Layer (5 files)
**Files:** UpdateManager, UpdateHandler, UpdatesFileHandler, Updates data, Tag data
- [ ] Convert, keeping updater/secretlab-kotlin dependencies

### Task 9: Bot Core - Permissions (5 files)
**Files:** PermissionManager, PermissionMessageHandler, permission enums
- [ ] Convert permission checking logic

### Task 10: Bot Core - Status (7 files)
**Files:** StatusManager, Status handlers, Status data
- [ ] Convert status bot management

### Task 11: Bot Core - Tickets (7 files)
**Files:** TicketManager, Ticket handlers, Ticket data/enums
- [ ] Convert ticket management

### Task 12: Bot Core - Application (4 files)
**Files:** ApplicationManager, ApplicationMessageHandler, ApplicationType data, MessageType enum
- [ ] Convert application management

### Task 13: Bot Core - Notice of Departure (4 files)
**Files:** NoticeOfDepartureManager, handlers, ActionId enum
- [ ] Convert notice of departure management

### Task 14: Bot Core - Regulars (9 files)
**Files:** RegularsManager, handlers, data classes
- [ ] Convert regulars management

### Task 15: Bot Core - Verify (1 file)
**Files:** VerifyMessageHandler
- [ ] Convert verify message handler

### Task 16: Bot Core - Modals (4 files)
**Files:** GlobalTemplateModals, TicketTemplateModals, NoticeOfDepartureTemplateModals, ApplicationTemplateModals
- [ ] Convert modal builders

### Task 17: Commands (16 files)
**Files:** CommandManager, CommandListener, StatusCommandListener, all command handlers
- [ ] Convert command registration and handling

### Task 18: Event Listeners (16 files)
**Files:** ButtonListener, ModalListener, StringSelectListener, EntitySelectListener, all button/modal/menu handlers
- [ ] Convert event listeners from JDA-KTX to plain JDA ListenerAdapter

### Task 19: Bot Manager + Main (2 files)
**Files:** BotManager, Main
- [ ] Convert entry point and bot initialization

### Task 20: Cleanup
- [ ] Delete all `.kt` files and `src/main/kotlin/` directory
- [ ] Verify build compiles
- [ ] Run tests if available
