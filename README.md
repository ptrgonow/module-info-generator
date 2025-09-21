# module-info-generator
### YAML로 module-info.java를 생성하는 최소 템플릿<br>

### Minimal template to generate module-info.java from YAML
- Java 25 / Gradle 9.1+
- Single source of truth: gradle/module-info.yml
- Generated file: src/main/java/module-info.java (not recommended to commit to VCS)

### ✨ What is this? / 이 프로젝트는?

JPMS 모듈 선언을 직접 손코딩하지 않고, YAML에서 선언적으로 관리하면 Gradle 태스크가 module-info.java를 자동 생성합니다.<br>
리플렉션이 필요한 패키지만 최소 개방(opens) 하도록 가이드합니다.<br>
Instead of hand-writing JPMS descriptors, declare them in a YAML file and let a Gradle task generate module-info.java. It also promotes minimal reflective openness via opens.

### ⚙️ Requirements / 요구사항
JDK 25 (Align both Project SDK and Gradle JVM with 25)<br>
Gradle 9.1+<br>
IntelliJ IDEA 2025.2.2+ (recommended)<br>

### 🧩 Project Layout / 프로젝트 구조

```plain
<repo>/
├─ build.gradle
├─ settings.gradle
├─ gradle/
│  ├─ module-info.gradle      # generator script (uses SnakeYAML)
│  └─ module-info.yml         # the single source of truth
├─ src/main/java/
├─ .gitignore
└─ README.md
```

### .gitignore 권장:

```plain
/.gradle/
/build/
/out/
*.iml
src/main/java/module-info.java
```

### 📦 Quick Start / 빠른 시작
**just build!** & generate module-info.java
./gradlew clean compileJava

### preview generated descriptor
./gradlew printModuleInfo

비활성화: ./gradlew compileJava -PmoduleGen.enabled=false <br>
다른 설정 파일 사용: ./gradlew compileJava -PmoduleGen.cfg=$rootDir/gradle/module-info.yml


### 📝 YAML Schema / YAML 스키마

gradle/module-info.yml 예시:

```yaml
moduleName: com.generator
requires:
  - java.base
  - java.net.http

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

### Notes / 메모
```plantuml
Do NOT put Spring “starter” artifacts in requires (they are not real modules).
starter를 requires에 넣지 마세요(모듈이 아님).

Lombok: 보통 module-info.java에 넣지 않습니다.
IDE 컴파일 인덱싱 이슈가 있을 때만 requires static lombok;를 고려하세요.
Gradle 의존성은 아래처럼 유지:

compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

Lombok is typically not added to module-info.java.
Only if your IDE shows indexing/compilation hints missing, consider requires static lombok;.
Keep Gradle dependencies as:

compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

🛠️ Generated Example / 생성 결과 예시

위 YAML에서 생성되는 module-info.java(예):
```java
module com.generator {

// ----- requires -----
requires java.base;
requires java.net.http;

// ----- requires static -----
requires static lombok;

// ----- exports -----
exports com.example.api;

// ----- opens (reflection) -----
opens com.generator.controller to spring.core, spring.beans, spring.context, spring.web, com.fasterxml.jackson.databind;
}
```

### 🧯 Troubleshooting / 문제 해결

Ambiguous imports (Java 25 모듈 임포트 선언 사용 시)
**타입 네이밍 혼선은 단일 타입 import 또는 FQN으로 해소하세요.**

### 📜 License / 라이선스

MIT
