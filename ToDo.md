# NeoForge Mod Setup Checklist

## Rename the Project

- [ ] **Edit `gradle.properties`**
  Set `mod_id`, `mod_name`, `mod_version`, `mod_group_id`, and `mod_license` to your mod's info.

- [ ] **Rename the Java package folder**
  Move `src/main/java/com/example/examplemod` to match your new `mod_group_id`.

- [ ] **Rename the main mod class**
  Rename `ExampleMod.java` to something like `ReplayMod.java`.

- [ ] **Update the main class file**
  - Fix the `package ...` declaration at the top to match the new folder.
  - Update `@Mod("yourmodid")` to match your new `mod_id`.

- [ ] **Fill in mod metadata**
  In the `neoforge.mods.toml` template, set `authors` and `description`.

## Verify It Works

- [ ] **Reload the Gradle project**
  Click the refresh/elephant icon in IntelliJ's Gradle panel.

- [ ] **Run `gradlew runClientAuth (if it isn't there, run this in the terminal: chmod +x gradlew and then ./gradlew runClientAuth)`**
  Confirm Minecraft launches and your renamed mod shows up in the Mods menu.
