# module-info-generator

**YAML로 `module-info.java`를 생성하는 최소 템플릿**  
**Minimal template to generate `module-info.java` from YAML**

- **Java 25 / Gradle 9.1+**
- **Single source of truth**: `gradle/module-info.yml`
- Generated file: `src/main/java/module-info.java` *(VCS 커밋 비권장)*

---

### ✨ What is this? / 이 프로젝트는?

**KO**  
JPMS 모듈 선언을 직접 손코딩하지 않고, YAML에서 선언적으로 관리하면 Gradle 태스크가 `module-info.java`를 자동 생성합니다.
<br>또한 리플렉션이 필요한 패키지만 **최소 개방(opens)** 하도록 유도합니다.
<br>
**EN**  
Instead of hand-writing JPMS descriptors, declare them in a YAML file and let a Gradle task generate `module-info.java`.
<br>It also promotes **minimal reflective openness** via `opens`.

---

### ⚙️ Requirements / 요구사항

- JDK **25** (Project SDK & Gradle JVM 모두 25로 정렬 권장)
- Gradle **9.1+**
- IntelliJ IDEA **2025.2.2+** (recommended)

---

### 🧩 Project Layout / 프로젝트 구조

```text
<repo>/
├─ build.gradle
├─ settings.gradle
├─ gradle/
│  └─ module-info.yml         # the single source of truth
├─ buildSrc/
│  ├─ build.gradle            # declares plugin id 'com.generator.module-info'
│  └─ src/main/groovy/com/generator/ModuleInfoPlugin.groovy
├─ src/main/java/
├─ .gitignore
└─ README.md
```

> **주의**: 예전 방식의 외부 스크립트 `gradle/module-info.gradle`은 사용하지 않습니다. 모든 로직은 `buildSrc` 플러그인에 포함됩니다.

---

### .gitignore 권장

```text
/.gradle/
/build/
/out/
*.iml
src/main/java/module-info.java
```

---

### 📦 Quick Start / 빠른 시작

**Build & generate `module-info.java`**
```bash
./gradlew clean compileJava
```

**Preview generated descriptor**
```bash
./gradlew printModuleInfo
```

비활성화:
```bash
./gradlew compileJava -PmoduleGen.enabled=false
```

다른 설정 파일 사용:
```bash
./gradlew compileJava -PmoduleGen.cfg=$rootDir/gradle/module-info.yml
```

---

### 📝 YAML Schema / YAML 스키마

`gradle/module-info.yml` 예시:

```yaml
moduleName: com.generator

requires:
  - java.base
  - java.net.http
  # - io.github.cdimascio.dotenv.java

# Optional: compile-time only (e.g., IDE help with Lombok)
requiresStatic:
  - lombok

exports:
  - com.example.api

opensRules:
  com.example.config:
    - spring.core
    - spring.beans
    - spring.context
    - spring.web
    - com.fasterxml.jackson.databind
```

---

### Notes / 메모

- **Spring “starter” 아티팩트는 `requires`에 넣지 마세요.** (JPMS 모듈이 아님)
- **Lombok**은 보통 `module-info.java`에 넣지 않습니다. IDE 인덱싱 이슈가 있을 때만  
  `requires static lombok;`을 고려하세요. Gradle 의존성은 아래처럼 유지:
  ```groovy
  compileOnly 'org.projectlombok:lombok'
  annotationProcessor 'org.projectlombok:lombok'
  ```
- **dotenv-java**를 `requires`에 넣으려면 **정식 모듈명**을 사용하고, **의존성도 추가**해야 합니다.  
  모듈명: `io.github.cdimascio.dotenv.java`  
  Gradle:
  ```groovy
  implementation 'io.github.cdimascio:dotenv-java:3.0.2'
  ```
- Java 25 모듈 임포트 선언 사용 시 **동명이인 타입 충돌**은 **단일 타입 import** 혹은 **FQN**으로 해소하세요.

---

### 🛠️ Generated Example / 생성 결과 예시

```java
module com.generator {

  // ----- requires -----
  requires java.base;
  requires java.net.http;
  // requires io.github.cdimascio.dotenv.java;

  // ----- requires static -----
  requires static lombok;

  // ----- exports -----
  exports com.example.api;

  // ----- opens (reflection) -----
  opens com.example.config to spring.core, spring.beans, spring.context, spring.web, com.fasterxml.jackson.databind;
}
```

---

### 🧯 Troubleshooting / 문제 해결

- **module not found**
    - `module-info.yml`의 모듈명이 실제 JAR 모듈명과 일치하는지(`jar --describe-module`) 확인하고,
    - Gradle 의존성이 `implementation`(or `api`)에 추가되어 있는지 점검하세요.

- **Ambiguous imports** (Java 25 module import declarations)
    - 단일 타입 import 또는 FQN 사용으로 해결.

- **Gradle 10 대비 경고**
    - `visible` 사용 흔적이 있으면 `canBeResolved / canBeConsumed`로 역할을 분리하세요.
      (Gradle Problems Report에서 위치 확인)

---

### 📜 License / 라이선스

MIT
