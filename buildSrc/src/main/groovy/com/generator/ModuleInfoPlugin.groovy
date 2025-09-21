package com.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.tasks.compile.JavaCompile
import org.yaml.snakeyaml.Yaml

class ModuleInfoPlugin implements Plugin<Project> {
    @Override
    void apply(Project p) {

        // 토글: -PmoduleGen.enabled=false
        def enabledProp = p.findProperty('moduleGen.enabled')
        boolean enabled = (enabledProp == null) ? true : enabledProp.toString().toBoolean()

        // 설정 파일: 기본 gradle/module-info.yml, -PmoduleGen.cfg 로 오버라이드
        String cfgPath = (p.findProperty('moduleGen.cfg') ?: "${p.rootDir}/gradle/module-info.yml").toString()
        File cfgFile = p.file(cfgPath)
        if (!cfgFile.exists()) {
            throw new GradleException("module-info 설정 파일이 없습니다: ${cfgFile}")
        }

        Map cfg = new Yaml().load(cfgFile.newReader('UTF-8')) as Map ?: [:]

        String moduleName = (cfg.moduleName ?: '').toString().trim()
        if (!moduleName) throw new GradleException("moduleName이 비어 있습니다: ${cfgFile}")

        List<String> requires       = (cfg.requires       instanceof List) ? (cfg.requires       as List).collect { it?.toString()?.trim() }.findAll { it } : []
        List<String> requiresStatic = (cfg.requiresStatic instanceof List) ? (cfg.requiresStatic as List).collect { it?.toString()?.trim() }.findAll { it } : []
        List<String> exports        = (cfg.exports        instanceof List) ? (cfg.exports        as List).collect { it?.toString()?.trim() }.findAll { it } : []

        Map<String, List<String>> opensRules = [:]
        if (cfg.opensRules instanceof Map) {
            (cfg.opensRules as Map).each { k, v ->
                def pkg = k?.toString()?.trim()
                if (!pkg) return
                if (v instanceof List) {
                    opensRules[pkg] = (v as List).collect { it?.toString()?.trim() }.findAll { it }
                } else {
                    throw new GradleException("opensRules.${k} 값은 배열(List)이어야 합니다. ${cfgFile}")
                }
            }
        }

        def gen = p.tasks.register('generateModuleInfo') {
            onlyIf { enabled }
            group = 'module-gen'
            description = "Generate src/main/java/module-info.java from ${cfgFile}"

            inputs.file(cfgFile)
            outputs.file("${p.projectDir}/src/main/java/module-info.java")

            doFirst {
                File out = p.file('src/main/java/module-info.java')
                if (out.exists()) {
                    p.logger.lifecycle("[generateModuleInfo] Deleting old descriptor: ${out}")
                    out.delete()
                }
            }

            doLast {
                if (requires.any { it.contains('starter') }) {
                    throw new GradleException("`requires`에 starter는 사용할 수 없습니다: ${requires}")
                }
                if (requires.any { it == 'lombok' || it == 'org.projectlombok' }) {
                    throw new GradleException("Lombok은 requires에 넣지 마세요. 필요 시 requiresStatic에 'lombok'")
                }

                File out = p.file('src/main/java/module-info.java')
                out.parentFile.mkdirs()

                String requiresBlock       = requires.collect { "  requires ${it};" }.join('\n')
                String requiresStaticBlock = requiresStatic.collect { "  requires static ${it};" }.join('\n')
                String exportsBlock        = exports.collect { "  exports ${it};" }.join('\n')
                String opensBlock          = opensRules && !opensRules.isEmpty()
                        ? opensRules.collect { pkg, targets -> "  opens ${pkg} to ${targets.join(', ')};" }.join('\n')
                        : ""

                def sections = []
                if (requiresBlock)       sections << "  // ----- requires -----\n${requiresBlock}"
                if (requiresStaticBlock) sections << "  // ----- requires static -----\n${requiresStaticBlock}"
                if (exportsBlock)        sections << "  // ----- exports -----\n${exportsBlock}"
                if (opensBlock)          sections << "  // ----- opens (reflection) -----\n${opensBlock}"

                String content = "module ${moduleName} {\n\n" + sections.join("\n\n") + "\n}\n"
                out.setText(content, 'UTF-8')
                p.logger.lifecycle("[generateModuleInfo] Wrote: ${out}")
            }
        }

        // compileJava 전에 생성
        p.tasks.named('compileJava', JavaCompile).configure { dependsOn(gen) }

        // 편의: 내용 미리보기 태스크
        p.tasks.register('printModuleInfo') {
            group = 'verification'
            dependsOn(gen)
            doLast {
                def f = p.file('src/main/java/module-info.java')
                println f.exists() ? f.text : 'module-info.java not found'
            }
        }
    }
}
