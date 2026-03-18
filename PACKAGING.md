# Packaging Guide – Intend

This guide explains how to build native installers for **macOS**, **Windows**, and **Linux**.

---

## Prerequisites

| Requirement                    | Minimum Version | Notes                           |
| ------------------------------ | --------------- | ------------------------------- |
| JDK (with `jpackage`)         | 17+             | JDK 14+ includes `jpackage`    |
| Maven                          | 3.8+            | or use the included `mvnw`     |
| **Windows only:** WiX Toolset | 3.x             | Required for `.msi` generation |
| **Linux DEB:** `dpkg-deb`     | any             | Ships with Debian/Ubuntu       |
| **Linux RPM:** `rpmbuild`     | any             | `sudo dnf install rpm-build`   |

---

## Quick Start

```bash
# Build the fat JAR + native installer for the CURRENT platform
./mvnw clean package -DskipTests
```

On macOS this produces a `.dmg`, on Windows a `.msi`, on Linux a `.deb`.

---

## CI/CD – Automated Cross-Platform Packaging

Since `jpackage` cannot cross-compile, a **GitHub Actions workflow** is provided
to build native installers on all 3 platforms automatically.

### Trigger Options

1. **Tag push** – Push a version tag to build + create a GitHub Release:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
   This produces:
   - macOS `.dmg`
   - Windows `.msi` + `.exe`
   - Linux `.deb` + `.rpm`
   
   All artifacts are uploaded to a GitHub Release.

2. **Manual dispatch** – Go to **Actions → Package Intend → Run workflow**.
   Optionally specify a version. Artifacts are available for download on the
   workflow run (no release is created).

### Workflow File

`.github/workflows/package.yml`

### What It Does

| Platform | Runner             | Output                      |
| -------- | ------------------ | --------------------------- |
| macOS    | `macos-latest`     | `Intend-1.0.0.dmg`         |
| Windows  | `windows-latest`   | `Intend-1.0.0.msi`, `.exe` |
| Linux    | `ubuntu-latest`    | `intend_1.0.0.deb`, `.rpm` |

---

## Local Platform-Specific Builds

### macOS (.dmg)

Automatically activated on macOS via the `package-mac` profile.

```bash
./mvnw clean package -DskipTests          # auto-activates on mac
# or explicitly:
./mvnw clean package -DskipTests -Ppackage-mac
```

**Output:** `Intend-1.0.0.dmg` in project root

### Windows (.msi)

Automatically activated on Windows. Requires **WiX Toolset 3.x** on the PATH.

```powershell
mvnw.cmd clean package -DskipTests        # auto-activates on Windows
# or explicitly:
mvnw.cmd clean package -DskipTests -Ppackage-windows
```

**Output:** `Intend-1.0.0.msi` in project root

> **Note:** Windows requires an `.ico` icon file. The CI workflow auto-converts
> `image.png` → `image.ico`. For local builds, provide `image.ico` in the
> project root.

### Linux – Debian/Ubuntu (.deb)

Automatically activated on Linux. Requires `dpkg-deb`.

```bash
./mvnw clean package -DskipTests          # auto-activates on Linux
# or explicitly:
./mvnw clean package -DskipTests -Ppackage-linux-deb
```

**Output:** `intend_1.0.0-1_amd64.deb` in project root

Install: `sudo dpkg -i intend_1.0.0-1_amd64.deb`

### Linux – Fedora/RHEL (.rpm)

Must be explicitly activated. Requires `rpmbuild`.

```bash
./mvnw clean package -DskipTests -Ppackage-linux-rpm
```

**Output:** `intend-1.0.0-1.x86_64.rpm` in project root

Install: `sudo rpm -i intend-1.0.0-1.x86_64.rpm`

---

## Icon Requirements

| Platform | Format  | File          | Notes                              |
| -------- | ------- | ------------- | ---------------------------------- |
| macOS    | `.icns` | `image.icns`  | Apple icon format                  |
| Windows  | `.ico`  | `image.ico`   | Auto-generated in CI from PNG      |
| Linux    | `.png`  | `image.png`   | Standard PNG                       |

---

## User Data Directory

The application stores all user data under a single hidden directory in the
user's home folder:

```
~/.intend/              (macOS / Linux)
C:\Users\<you>\.intend\ (Windows)
```

| File                         | Purpose                                           |
| ---------------------------- | ------------------------------------------------- |
| `history.json`               | Request history (method, URL, body, timestamp)    |
| `saved-requests.json`        | Saved/bookmarked requests                         |
| `intend-config.json`         | Dev/Prod URLs and API keys                        |
| `intend-state.properties`    | Idempotency keys for request deduplication        |

> **Uninstall note:** The native installers do **not** remove `~/.intend/`.
> Delete it manually to remove all stored data.

---

## Profiles Summary

| Profile              | Activation        | Installer Type | Platform              |
| -------------------- | ----------------- | -------------- | --------------------- |
| `package-mac`        | Auto (macOS)      | `.dmg`         | macOS                 |
| `package-windows`    | Auto (Windows)    | `.msi`         | Windows               |
| `package-linux-deb`  | Auto (Linux)      | `.deb`         | Debian / Ubuntu       |
| `package-linux-rpm`  | Manual (`-P`)     | `.rpm`         | Fedora / RHEL / SUSE  |

---

## Troubleshooting

| Symptom                                  | Fix                                                        |
| ---------------------------------------- | ---------------------------------------------------------- |
| `jpackage` not found                     | Ensure JDK 14+ is on PATH (`java -version`)               |
| Windows MSI build fails                  | Install WiX 3.x and add to PATH                           |
| Linux RPM build fails                    | Install `rpm-build`: `sudo dnf install rpm-build`          |
| `InaccessibleObjectException` at runtime | The `--add-opens` JVM options are already configured       |
| App icon not showing                     | Verify `image.icns` (mac) / `image.ico` (win) exists      |
| Windows icon missing `.ico`              | CI auto-converts; locally run icon converter               |
