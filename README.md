# criterion-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fcriterion-kotlin-blue.svg)](https://github.com/KotlinMania/criterion-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/criterion-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/criterion-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/criterion-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/criterion-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`bheisler/criterion.rs`](https://github.com/bheisler/criterion.rs) (crate `criterion`).

**Original Project:** This port is based on [`bheisler/criterion.rs`](https://github.com/bheisler/criterion.rs). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `bheisler/criterion.rs`

> The text below is reproduced from [`https://github.com/bheisler/criterion.rs`](https://github.com/bheisler/criterion.rs). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs where appropriate so they continue to resolve from this repository.

<h1 align="center">Criterion.<span></span>rs</h1>

<div align="center">Statistics-driven Microbenchmarking in Rust</div>

Due to Brook being missing for extended periods of time, active development of Criterion has moved to a new [Criterion-rs](https://github.com/criterion-rs) organization. Please direct your new PRs and issues to the new [criterion.rs repo](https://github.com/criterion-rs/criterion.rs).

Please provide some time for us to catch up and clean the large amount of pending PRs from this repo. Thank you!

- Berkus
- Lemmih

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:criterion-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [SWIFT.md](SWIFT.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same license terms as the upstream [`bheisler/criterion.rs`](https://github.com/bheisler/criterion.rs). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the criterion authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`bheisler/criterion.rs`](https://github.com/bheisler/criterion.rs) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
