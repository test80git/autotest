package ru.sber.cb.ekp.annotation.processor;

import com.google.auto.service.AutoService;
import io.cucumber.core.internal.com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import jakarta.persistence.Entity;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeName;
import org.springframework.javapoet.TypeSpec;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.cb.ekp.annotation.Description;
import ru.sber.cb.ekp.annotation.Step;
import ru.sber.cb.ekp.annotation.StepEntity;
import ru.sber.cb.ekp.annotation.StepTransManager;
import ru.sber.cb.ekp.annotation.Steps;
import ru.sber.cb.ekp.annotation.dto.DtoRag;
import ru.sber.cb.ekp.annotation.dto.EntityRag;
import ru.sber.cb.ekp.annotation.dto.Rag;
import ru.sber.cb.ekp.annotation.dto.ServiceRag;
import ru.sber.cb.ekp.annotation.dto.TreeRag;
import ru.sber.cb.ekp.utils.Caster;
import ru.sber.cb.ekp.utils.Comparator;
import ru.sber.cb.ekp.utils.Context;
import ru.sber.cb.ekp.utils.FieldHandler;
import ru.sber.cb.ekp.utils.PredicateContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@AutoService(Processor.class)
@SupportedAnnotationTypes("ru.sber.cb.ekp.annotation.Steps")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class StepsProcessor extends AbstractProcessor {

    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Rag> rag = new HashMap<>();
    private final Pattern methodQuery = Pattern.compile("(?<type>find|delete)(?<ent>\\w+?)By(?<expr>\\w+)");
    private final Pattern arrayQuery = Pattern.compile("(?<field>\\w+)\\[(?<type>\\w)]");
    private final Pattern fieldArrayPat = Pattern.compile("(?<field>\\w+)(?<type>\\[\\w])?");

    private final AnnotationSpec generated = AnnotationSpec.builder(Generated.class)
            .addMember("value", "\"$N\"", StepsProcessor.class.getCanonicalName())
            .build();

    List<TypeMirror> primitives;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.primitives = Stream.of(Long.class, Integer.class, BigDecimal.class, Timestamp.class, String.class, Boolean.class, LocalDate.class, Double.class, Float.class)
                .map(clazz -> elementUtils.getTypeElement(clazz.getCanonicalName()).asType()).toList();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writeToFile(rag, "rag.json", new TypeReference<Map<String, Rag>>() {
            });
        }
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                TypeElement typeElement = (TypeElement) element;

                var repos = processRepos(typeElement);
                processEntities(typeElement, repos);
                processDaos(typeElement);
            }
        }
        return true;
    }

    private <T> void writeToFile(Object object, String filename, TypeReference<T> tr) {
        try {
            var file = processingEnv.getFiler().createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    "",
                    filename
            );
            try (var writer = file.openWriter()) {
                writer.write(mapper.writerFor(tr).writeValueAsString(object));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processEntities(TypeElement typeElement, Map<String, Element> repos) {
        for (StepEntity stepEntity : Arrays.stream(typeElement.getAnnotation(Steps.class).entityTunes()).toList()) {
            PackageElement packageElement = elementUtils.getPackageElement(stepEntity.packagePath());

            Set<TypeElement> entities = findEntitiesInPackage(packageElement);

            Map<String, List<EntityPathNode>> pathMap = new HashMap<>();

            for (TypeElement typeEntity : entities) {
                Step step = typeEntity.getAnnotation(Step.class);
                if (step != null && step.root()) {
                    String entName = typeEntity.asType().toString();
                    var ent = fillEntityPathes(typeEntity, new HashSet<>(), step.onlyExplicitlyIncluded());
                    pathMap.put(entName, ent);
                }
            }

            TypeMirror superClass = typeElement.asType();
            generateEntitySteps(elementUtils.getPackageOf(typeElement).toString() + ".entity", stepEntity.entityManager(), stepEntity.transManager(), superClass, pathMap, repos);
            generateTrees(pathMap, EntityRag::new);
        }
    }

    private void generateTrees(Map<String, ? extends List<? extends PathNode>> pathMap, Supplier<TreeRag> treeRagSupplier) {
        for (var entry : pathMap.entrySet()) {
            var entityTree = new StringBuilder();
            var keyElement = elementUtils.getTypeElement(entry.getKey());
            var className = keyElement.getSimpleName().toString();

            var keyElementDescription = keyElement.getAnnotation(Description.class);
            var keyElementDescriptionValue = keyElementDescription != null
                    ? String.join(", ", keyElementDescription.value())
                    : "";
            if (!keyElementDescriptionValue.isBlank()) {
                entityTree.append(MessageFormat.format("{0} #{1}",
                        className,
                        String.join(", ", keyElementDescriptionValue)
                ));
            } else {
                entityTree.append(MessageFormat.format("{0}",
                        className
                ));
            }
            for (var pathNode : entry.getValue()) {
                var typeElement = elementUtils.getTypeElement(pathNode.getToType().toString());
                typeElement = typeElement == null
                        ? elementUtils.getTypeElement(((DeclaredType) pathNode.getToType()).getTypeArguments().get(0).toString())
                        : typeElement;
                var variableDescription = pathNode.getTo().getAnnotation(Description.class);
                variableDescription = variableDescription == null
                        ? typeElement.getAnnotation(Description.class)
                        : variableDescription;
                var variableDescriptionValue = variableDescription != null
                        ? String.join(", ", variableDescription.value())
                        : null;
                if (variableDescriptionValue != null) {
                    entityTree.append(MessageFormat.format("\n{0}.{1} ({2}) ({3})",
                            className,
                            pathNode.getPath(),
                            typeElement.getSimpleName(),
                            String.join(",", variableDescriptionValue)
                    ));
                } else {
                    entityTree.append(MessageFormat.format("\n{0}.{1} ({2})",
                            className,
                            pathNode.getPath(),
                            typeElement.getSimpleName()
                    ));
                }
            }

            rag.compute(className, (k, v) -> {
                if (v == null) {
                    var treeRag = treeRagSupplier.get();
                    treeRag.setDescription(keyElementDescriptionValue);
                    treeRag.setTree(entityTree.toString());
                    return treeRag;
                } else if (v instanceof TreeRag treeRag) {
                    treeRag.setDescription(keyElementDescriptionValue);
                    treeRag.setTree(entityTree.toString());
                    return treeRag;
                }
                throw new RuntimeException(k);
            });
        }
    }

    private List<EntityPathNode> fillEntityPathes(TypeElement entityType, Set<String> visited, boolean onlyExplicitlyIncluded) {
        List<EntityPathNode> pathNodes = new ArrayList<>();
        String entityClass = entityType.asType().toString();

        visited.add(entityClass);

        if (entityType.getSuperclass().getKind() != TypeKind.NONE) {
            TypeElement superClass = elementUtils.getTypeElement(entityType.getSuperclass().toString());
            if (superClass.getAnnotation(Entity.class) != null) {
                Step step = superClass.getAnnotation(Step.class);
                pathNodes.addAll(fillEntityPathes(
                        superClass,
                        new HashSet<>(visited),
                        step != null && step.onlyExplicitlyIncluded()
                ));
            }
        }

        for (Element element : entityType.getEnclosedElements().stream()
                .filter(element -> element.getKind().isField()).toList()) {
            if (onlyExplicitlyIncluded && element.getAnnotation(Step.Include.class) != null
                || !onlyExplicitlyIncluded && element.getAnnotation(Step.Exclude.class) == null
            ) {
                VariableElement variableElement = (VariableElement) element;

                // Проверка на дубликаты полей из base класса
                if (pathNodes.stream().anyMatch(pn -> pn.getFrom().getSimpleName().equals(variableElement.getSimpleName())))
                    continue;


                TypeMirror fieldType = variableElement.asType();
                if (isPrimitive(fieldType)) {
                    pathNodes.add(new EntityPathNode(variableElement, variableElement, fieldType, variableElement.toString()));
                } else {
                    TypeElement retType = elementUtils.getTypeElement(variableElement.asType().toString());
                    if (retType == null || retType.getAnnotation(Entity.class) == null) continue;
                    if (visited.contains(retType.toString())) continue;

                    pathNodes.add(new EntityPathNode(variableElement, variableElement, fieldType, variableElement.toString()));

                    var step = retType.getAnnotation(Step.class);
                    var child = fillEntityPathes(retType, new HashSet<>(visited), step != null && step.onlyExplicitlyIncluded());
                    for (EntityPathNode pathNode : child) {
                        pathNodes.add(new EntityPathNode(
                                variableElement,
                                pathNode.getTo(),
                                pathNode.getToType(),
                                variableElement + "." + pathNode.getPath()
                        ));
                    }
                }
            }
        }
        return pathNodes;
    }

    private Set<TypeElement> findEntitiesInPackage(PackageElement packageElement) {
        Set<TypeElement> entities = new HashSet<>();
        for (Element element : packageElement.getEnclosedElements()) {
            if (element instanceof TypeElement typeElement) {
                if (typeElement.getKind().isClass() && typeElement.getAnnotation(Entity.class) != null) {
                    entities.add(typeElement);
                }
            }
        }
        return entities;
    }

    private String getterMethod(String var) {
        String res = var.length() > 1 && Character.isLowerCase(var.charAt(1))
                ? var.substring(0, 1).toUpperCase() + var.substring(1)
                : var;
        String result = "get" + res + "()";
        Matcher matcher = arrayQuery.matcher(var);
        if (matcher.matches()) {
            switch (matcher.group("type")) {
//                case "a": return result.replace("[a]", "") + "[" + matcher.group("field") + "Idx]";
                case "l":
                    return result.replace("[l]", "") + ".get(" + matcher.group("field") + "Idx)";
            }
        }
        return result;
    }

    private void getterMethodChecker(CodeBlock.Builder codeBlock, String origin, List<String> vars, int i) {
        while (i < vars.size()) {
            Matcher matcher = fieldArrayPat.matcher(vars.get(i));
            if (matcher.matches()) {
                String var = matcher.group("field");
                String res = var.length() > 1 && Character.isLowerCase(var.charAt(1))
                        ? var.substring(0, 1).toUpperCase() + var.substring(1)
                        : var;
                origin += ".get" + res + "()";
                if (matcher.group("type") != null) {
                    codeBlock
                            .beginControlFlow("$T.checkListField($N, $N, ($N) ->",
                                    FieldHandler.class, var + "Idx", origin, var)
                            .add("return ");

                    getterMethodChecker(codeBlock, var, vars, i + 1);

                    codeBlock.endControlFlow(")");
                    return;
                }
            }
            i++;
        }
        codeBlock.addStatement("$T.safeCompare(equalsType, expected.orElse(null), $N)",
                Comparator.class, origin);
    }

    private String setterMethod(String var, String value) {
        String result = var.length() > 1 && Character.isLowerCase(var.charAt(1))
                ? var.substring(0, 1).toUpperCase() + var.substring(1)
                : var;

        //        String result = var.substring(0, 1).toUpperCase() + var.substring(1);
        Matcher matcher = arrayQuery.matcher(var);
        if (matcher.matches()) {
            switch (matcher.group("type")) {
                //                case "a": return "get" + result.replace("[a]", "") + "[" + matcher.group("field") + "Idx] = " + value;
                case "l":
                    return "get" + result.replace("[l]", "") + "().set(" + matcher.group("field") + "Idx, " + value + ")";
            }
        }
        return "set" + result + "(" + value + ")";
    }

    private void generateEntitySteps(String packageName, String entityManager, String transManager, TypeMirror superClass, Map<String, List<EntityPathNode>> pathMap, Map<String, Element> repos) {
        for (var key : pathMap.keySet()) {
            TypeElement keyElement = elementUtils.getTypeElement(key);
            String className = keyElement.getSimpleName().toString();
            String generatedClassName = className + "Steps";
            TypeSpec.Builder stepsBuilder = TypeSpec.classBuilder(generatedClassName)
                    .addAnnotation(generated)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(superClass);

            for (var pathNode : pathMap.get(key)) {
                stepsBuilder.addMethod(generateSetContext(keyElement, pathNode));
                stepsBuilder.addMethod(generateCheckWithContext(keyElement, pathNode));
                stepsBuilder.addMethod(generateWriteFromContext(keyElement, pathNode));
                if (!isPrimitive(pathNode.getTo().asType())) {
                    stepsBuilder.addMethod(generateEntityUpdateFromDB(keyElement, entityManager, pathNode));
                }
                if (repos.containsKey(pathNode.getTo().asType().toString())) {
                    stepsBuilder.addMethod(generateSaveToDB(keyElement, pathNode, (VariableElement) repos.get(pathNode.getTo().asType().toString())));
                }
            }

            stepsBuilder.addMethod(generateSetContext(keyElement));
            stepsBuilder.addMethod(generateCheckWithContext(keyElement));
            stepsBuilder.addMethod(generateWriteFromContext(keyElement));
            stepsBuilder.addMethod(generateEntityUpdateFromDB(keyElement, entityManager));

            if (repos.containsKey(key)) {
                stepsBuilder.addMethod(generateSaveToDB(keyElement, (VariableElement) repos.get(key)));
            }

            // Создаем пакет
            JavaFile javaFile = JavaFile.builder(packageName, stepsBuilder.build())
                    .indent("    ")
                    .skipJavaLangImports(true)
                    .build();

            // Генерируем файл
            FileObject fileObject = null;
            try {
                fileObject = processingEnv.getFiler().createSourceFile(javaFile.packageName + "." + generatedClassName);
                try (Writer writer = fileObject.openWriter()) {
                    javaFile.writeTo(writer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private MethodSpec generateSetContext(TypeElement keyElement, PathNode pathNode) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("""
                        $T origin = $T.getContext($S, $T.class).orElse(null);
                        $T value = null;
                        try {
                            value = origin.$N;
                        } catch($T exception) {

                        } finally {
                            $T.setContext(key, value);
                            io.qameta.allure.Allure.addAttachment(key, value != null ? value.toString() : "null");
                        }
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType(),
                pathNode.getToType(),
                Arrays.stream(pathNode.getPath().split("\\.")).map(this::getterMethod).collect(Collectors.joining(".")),
                NullPointerException.class,
                Context.class
        );

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Сохраняю $N в {context_key}\"", keyElement.getSimpleName() + "." +
                                                                       pathNode.getPath()
                                                                               .replace("[a]", "[{int}]")
                                                                               .replace("[l]", "[{int}]"))
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("setContext" + keyElement.getSimpleName() + "_" +
                                                              pathNode.getPath()
                                                                      .replace(".", "_")
                                                                      .replace("[a]", "")
                                                                      .replace("[l]", ""))
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        Matcher matcher = arrayQuery.matcher(pathNode.getPath());
        while (matcher.find()) builder.addParameter(TypeName.get(Integer.class), matcher.group("field") + "Idx");

        builder.addParameter(TypeName.get(String.class), "key");

        return builder.build();
    }

    private MethodSpec generateSetContext(TypeElement keyElement) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("""
                        $T value = $T.getContext($S, $T.class).orElse(null);
                        $T.setContext(key, value);
                        io.qameta.allure.Allure.addAttachment(key, value != null ? value.toString() : "null");
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType(),
                Context.class
        );

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Сохраняю $N в {context_key}\"", keyElement.getSimpleName())
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("setContext" + keyElement.getSimpleName())
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        builder.addParameter(TypeName.get(String.class), "key");

        return builder.build();
    }

    private MethodSpec generateSaveToDB(TypeElement keyElement, VariableElement repo) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("""
                        $T origin = $T.getContext($S, $T.class).orElse(null);
                        $N.saveAndFlush(origin);
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType(),
                repo.getSimpleName()
        );

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Сохраняю $N в БД\"", keyElement.getSimpleName())
                .build();

        AnnotationSpec transAnnotation = AnnotationSpec.builder(Transactional.class)
                .addMember("value", "$S", repo.getAnnotation(StepTransManager.class).value())
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("saveToDB" + keyElement.getSimpleName())
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addAnnotation(transAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        return builder.build();
    }

    private MethodSpec generateSaveToDB(TypeElement keyElement, PathNode pathNode, VariableElement repo) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("""
                        $T origin = $T.getContext($S, $T.class).orElse(null);
                        var value = origin.$N;
                        $N.saveAndFlush(value);
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType(),
                Arrays.stream(pathNode.getPath().split("\\.")).map(this::getterMethod).collect(Collectors.joining(".")),
                repo.getSimpleName()
        );

        String clearedPath = pathNode.getPath()
                .replace("[a]", "")
                .replace("[l]", "");

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Сохраняю $N в БД\"", keyElement.getSimpleName() + "." + clearedPath)
                .build();

        AnnotationSpec transAnnotation = AnnotationSpec.builder(Transactional.class)
                .addMember("value", "$S", repo.getAnnotation(StepTransManager.class).value())
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("saveToDB" + keyElement.getSimpleName() + "_" + clearedPath.replace(".", "_"))
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addAnnotation(transAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        Matcher matcher = arrayQuery.matcher(pathNode.getPath());
        while (matcher.find()) builder.addParameter(TypeName.get(Integer.class), matcher.group("field") + "Idx");

        return builder.build();
    }

    private MethodSpec generateEntityUpdateFromDB(TypeElement keyElement, String entityManager, EntityPathNode pathNode) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        var list = Arrays.stream(pathNode.getPath().split("\\.")).toList();
        codeBlock.add("""
                        $T origin = $T.getContext($S, $T.class).orElseThrow();
                        $T value = $N.find($T.class, origin.$N.getId());
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType(),
                pathNode.getToType(), entityManager, pathNode.getTo().asType(), list.stream().map(this::getterMethod).collect(Collectors.joining("."))
        );
        if (list.isEmpty()) {
            codeBlock.add("""
                            $T.setContext($S, value);
                            io.qameta.allure.Allure.addAttachment($S, value != null ? value.toString() : "null");
                            """,
                    Context.class, keyElement.getSimpleName(),
                    keyElement.getSimpleName()
            );
        } else {
            codeBlock.add("""
                            origin$N$N.$N;
                            io.qameta.allure.Allure.addAttachment($S, value != null ? value.toString() : "null");
                            """,
                    list.size() > 1 ? "." : "",
                    list.stream().limit(list.size() - 1).map(this::getterMethod).collect(Collectors.joining(".")),
                    setterMethod(list.get(list.size() - 1), "value"),
                    pathNode.getPath()
            );
        }

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Обновляю $N из БД\"", keyElement.getSimpleName() + "." + pathNode.getPath())
                .build();

        return MethodSpec.methodBuilder("refresh" + keyElement.getSimpleName() + "_" + pathNode.getPath().replace(".", "_"))
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build())
                .build();
    }

    private MethodSpec generateEntityUpdateFromDB(TypeElement keyElement, String entityManager) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("""
                        $T origin = $T.getContext($S, $T.class).orElseThrow();
                        $T value = $N.find($T.class, origin.getId());
                        $T.setContext($S, value);
                        io.qameta.allure.Allure.addAttachment($S, value != null ? value.toString() : "null");
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType(),
                keyElement.asType(), entityManager, keyElement.asType(),
                Context.class, keyElement.getSimpleName(),
                keyElement.getSimpleName()
        );

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Обновляю $N из БД\"", keyElement.getSimpleName())
                .build();

        return MethodSpec.methodBuilder("refresh" + keyElement.getSimpleName())
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build())
                .build();
    }

    private Map<String, Element> processRepos(TypeElement typeElement) {
        String packageName = elementUtils.getPackageOf(typeElement).toString();
        TypeMirror superClass = typeElement.asType();
        Map<String, Element> ret = new HashMap<>();
        for (Element field : typeElement.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) continue;
            DeclaredType type = (DeclaredType) field.asType();
            TypeElement fieldClass = (TypeElement) type.asElement();

            if (!fieldClass.getKind().isInterface()) continue;

            if (fieldClass.getInterfaces().size() != 1) continue;

            TypeMirror jpaInt = fieldClass.getInterfaces().get(0);
            if (!jpaInt.toString().startsWith("org.springframework.data.jpa.repository.JpaRepository")) continue;

            DeclaredType declaredJpaInt = (DeclaredType) jpaInt;
            if (declaredJpaInt.getTypeArguments().size() != 2) continue;

            ret.put(declaredJpaInt.getTypeArguments().get(0).toString(), field);
            //            TypeMirror entityType = declaredJpaInt.getTypeArguments().get(0);
            //            String repName = field.getSimpleName().toString();

            List<ExecutableElement> repoSteps = new ArrayList<>();

            for (Element query : fieldClass.getEnclosedElements()) {
                Matcher matcher = methodQuery.matcher(query.getSimpleName().toString());
                if (matcher.matches()) {
                    repoSteps.add((ExecutableElement) query);
                }
            }
            try {
                if (!repoSteps.isEmpty()) {
                    generateReposSteps(packageName + ".repository", superClass, field, repoSteps);
                    generateReposDoc(repoSteps);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return ret;
    }

    private void generateReposDoc(List<ExecutableElement> queries) {
        for (ExecutableElement query : queries) {
            var methodName = query.getSimpleName().toString();
            var returnType = query.getReturnType();
            var parameters = query.getParameters();

            var matcher = methodQuery.matcher(methodName);
            if (matcher.matches()) {
                var description = query.getAnnotation(Description.class);
                var descriptionValue = description != null
                        ? String.join(", ", description.value())
                        : null;
                var sb = new StringBuilder();
                var forceDt = returnType.toString().startsWith("java.util.stream.Stream") && parameters.isEmpty();
                switch (matcher.group("type")) {
                    case "find":
                        if (forceDt) {
                            sb.append(MessageFormat.format("And Поиск {0} по условию {1}+", matcher.group("ent"),
                                    matcher.group("expr")));
                            sb.append("\n");
                        }
                        sb.append(MessageFormat.format("And Поиск {0} по условию {1}", matcher.group("ent"),
                                matcher.group("expr")));
                        break;
                    case "delete":
                        sb.append(MessageFormat.format("And Удаляю {0} по условию {1}", matcher.group("ent"),
                                matcher.group("expr")));

                        break;
                    default:
                }
                if (descriptionValue != null)
                    sb.append(" #").append(descriptionValue);
                if (!parameters.isEmpty() || forceDt) {
                    for (var parameter : parameters) {
                        var type = parameter.asType();
                        String name = getName(type);
                        sb.append("\n");
                        sb.append(MessageFormat.format(
                                        "  | {0} | '{context}' | #{1}",
                                        parameter.getSimpleName(),
                                        name
                                )
                        );
                    }
                }

                rag.compute(matcher.group("ent"), (k, v) -> {
                    if (v == null) {
                        var entity = new EntityRag();
                        entity.setRepos(sb.toString());
                        return entity;
                    } else {
                        if (v instanceof EntityRag entity) {
                            entity.setRepos(entity.getRepos().concat("\n").concat(sb.toString()));
                            return entity;
                        } else {
                            throw new RuntimeException(k);
                        }
                    }
                });
            }
        }
    }

    private String getName(TypeMirror type) {
        if (type instanceof PrimitiveType pt) {
            return pt.toString();
        } else if (type instanceof DeclaredType dt) {
            var collectionClass = (TypeElement) dt.asElement();
            var collectionClassName = collectionClass.getSimpleName().toString();

            var typeArguments = dt.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                var params = new StringBuilder("<");
                for (int i = 0; i < typeArguments.size(); i++) {
                    var arg = typeArguments.get(i);
                    if (arg instanceof TypeVariable || arg instanceof WildcardType) {
                        params.append(arg);
                    } else {
                        var argTypeElementName = getName(arg);
                        params.append(argTypeElementName);
                    }
                    if (i < typeArguments.size() - 1) {
                        params.append(", ");
                    }
                }
                params.append(">");
                return collectionClassName + params;
            } else {
                return collectionClassName;
            }
        }
        return elementUtils.getTypeElement(type.toString()).getSimpleName().toString();
    }

    private void generateReposSteps(String packageName, TypeMirror superClass, Element repo, List<ExecutableElement> queries) throws IOException {
        TypeElement repoClass = (TypeElement) ((DeclaredType) repo.asType()).asElement();
        String generatedClassName = repoClass.getSimpleName() + "Steps";

        // Добавляем аннотацию @Slf4j
        AnnotationSpec logAnnotation = AnnotationSpec.builder(ClassName.get("lombok.extern.slf4j", "Slf4j"))
                .build();

        TypeSpec.Builder stepsBuilder = TypeSpec.classBuilder(generatedClassName)
                .addAnnotation(generated)
                .addModifiers(Modifier.PUBLIC)
                .superclass(superClass)
                .addAnnotation(logAnnotation); // добавляем аннотацию к классу

        for (ExecutableElement query : queries) {
            String methodName = query.getSimpleName().toString();
            TypeMirror returnType = query.getReturnType();

            List<? extends VariableElement> parameters = query.getParameters();

            Matcher matcher = methodQuery.matcher(methodName);
            if (matcher.matches()) {
                switch (matcher.group("type")) {
                    case "find":
                        stepsBuilder.addMethod(generateReposFind(repo, methodName, returnType, parameters, matcher, false));
                        if (returnType.toString().startsWith("java.util.stream.Stream") && parameters.isEmpty()) {
                            stepsBuilder.addMethod(generateReposFind(repo, methodName, returnType, parameters, matcher, true));
                        }
                        break;
                    case "delete":
                        stepsBuilder.addMethod(generateReposDelete(repo, methodName, parameters, matcher));
                        break;
                    default:
                }
            }
        }

        JavaFile javaFile = JavaFile.builder(packageName, stepsBuilder.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build();

        FileObject fileObject = processingEnv.getFiler().createSourceFile(javaFile.packageName + "." + generatedClassName);
        try (Writer writer = fileObject.openWriter()) {
            javaFile.writeTo(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MethodSpec generateReposFind(Element repo, String methodName, TypeMirror returnType, List<? extends VariableElement> parameters, Matcher matcher, boolean forceDt) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        ClassName log = ClassName.get("lombok.extern.slf4j", "Slf4j");

        if (!parameters.isEmpty() || forceDt) {
            codeBlock.addStatement("$T<$T, $T> dt = dataTable.asMap($T.class, $T.class)",
                    Map.class, String.class, String.class, String.class, String.class);
        }

        if (returnType.toString().startsWith("java.util.stream.Stream")) {
            if (returnType instanceof DeclaredType declaredType) {
                TypeMirror classMirror = declaredType.getTypeArguments().get(0);
                codeBlock.addStatement("$T target = null"
                        , classMirror);
            }

            codeBlock.add("""
                            try (var stream = $N.$L(
                            """,
                    repo.getSimpleName().toString(), methodName
            );

            for (int i = 0; i < parameters.size(); i++) {
                VariableElement field = parameters.get(i);
                TypeMirror fieldType = field.asType();

                if (fieldType.toString().contains("Collection") || fieldType.toString().contains("List")) {
                    codeBlock.add("""
                                        $T.getListFromContext(dt.get($S))$N
                                    """,
                            Context.class,
                            field.getSimpleName().toString(),
                            i + 1 < parameters.size() ? "," : ""
                    );
                } else {
                    codeBlock.add("""
                                        $T.cast($T.getSmartContext(dt.get($S)).orElseThrow(), $T.class)$N
                                    """,
                            Caster.class, Context.class, field.getSimpleName().toString(), field.asType(),
                            i + 1 < parameters.size() ? "," : ""
                    );
                }
            }

            if (!parameters.isEmpty() || forceDt) {
                codeBlock.add("""
                                )) {
                                    target = stream.filter(cred -> {
                                        $T.setContext($S, cred);
                                        return dt.keySet().stream()
                                                .filter(dtkey -> dtkey.startsWith("$$"))
                                                .allMatch(predicate -> {
                                                    return $T.check(predicate, $T.getSmartContext(dt.get(predicate)).orElseThrow());
                                                });
                                    }).findAny().orElse(null);
                                    log.info("Найден {}: {}", "$L", target);
                                }
                                """,
                        Context.class, matcher.group("ent"),
                        PredicateContext.class, Context.class,
                        matcher.group("ent"));
            } else {
                codeBlock.add("""
                                )) {
                                    target = stream.findAny().orElse(null);
                                    log.info("Найден {}: {}", "$L", target);
                                }
                                """,
                        matcher.group("ent"));
            }
        } else if (returnType.toString().startsWith("java.util.Optional")) {
            codeBlock.add("""
                            var target = $N.$L(
                            """,
                    repo.getSimpleName().toString(), methodName
            );

            for (int i = 0; i < parameters.size(); i++) {
                VariableElement field = parameters.get(i);
                codeBlock.add("""
                                    $T.cast($T.getSmartContext(dt.get($S)).orElseThrow(), $T.class)$N
                                """,
                        Caster.class, Context.class, field.getSimpleName().toString(), field.asType(),
                        i + 1 < parameters.size() ? "," : ""
                );
            }
            codeBlock.add("""
                            ).orElse(null);
                            log.info("Найден {}: {}", "$L", target);
                            """,
                    matcher.group("ent")
            );
        } else {
            codeBlock.add("""
                            var target = $N.$L(
                            """,
                    repo.getSimpleName().toString(), methodName
            );

            for (int i = 0; i < parameters.size(); i++) {
                VariableElement field = parameters.get(i);
                codeBlock.add("""
                                    $T.cast($T.getSmartContext(dt.get($S)).orElseThrow(), $T.class)$N
                                """,
                        Caster.class, Context.class, field.getSimpleName().toString(), field.asType(),
                        i + 1 < parameters.size() ? "," : ""
                );
            }
            codeBlock.add("""
                            );
                            log.info("Найден {}: {}", "$L", target);
                            """,
                    matcher.group("ent")
            );
        }

        codeBlock.add("""
                        io.qameta.allure.Allure.addAttachment($S, target != null ? target.toString() : "null");
                        $T.setContext($S, target);
                        """,
                matcher.group("ent"),
                Context.class, matcher.group("ent"));

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Поиск $N по условию $N" + (forceDt ? "+\"" : "\""), matcher.group("ent"),
                        matcher.group("expr"))
                .build();

        AnnotationSpec transAnnotation = AnnotationSpec.builder(Transactional.class)
                .addMember("value", "$S", repo.getAnnotation(StepTransManager.class).value())
                .addMember("readOnly", "$L", true)
                .build();

        MethodSpec.Builder stepMethod = MethodSpec.methodBuilder(methodName)
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        if (!parameters.isEmpty() || forceDt) {
            stepMethod.addParameter(TypeName.get(DataTable.class), "dataTable");
        }

        if (returnType.toString().startsWith("java.util.stream.Stream")) {
            stepMethod.addAnnotation(transAnnotation);
        }
        return stepMethod.build();
    }

    private MethodSpec generateReposDelete(Element repo, String methodName, List<? extends VariableElement> parameters, Matcher matcher) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if (!parameters.isEmpty()) {
            codeBlock.add("""
                            $T<$T, $T> dt = dataTable.asMap($T.class, $T.class);
                            log.debug("Удаление {} по парметрам: {}", "$L", dt);
                            """,
                    Map.class, String.class, String.class, String.class, String.class,
                    matcher.group("ent")
            );
        } else {
            codeBlock.add("""
                            log.debug("Удаление всех записей {}");
                            """,
                    matcher.group("ent")
            );
        }

        codeBlock.add("""
                        $N.$L(
                        """,
                repo.getSimpleName().toString(), methodName
        );

        for (int i = 0; i < parameters.size(); i++) {
            VariableElement field = parameters.get(i);
            codeBlock.add("""
                                $T.cast($T.getSmartContext(dt.get($S)).orElseThrow(), $T.class)$N
                            """,
                    Caster.class, Context.class, field.getSimpleName().toString(), field.asType(),
                    i + 1 < parameters.size() ? "," : ""
            );
        }

        codeBlock.add(");\n");

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Удаляю $N по условию $N\"", matcher.group("ent"), matcher.group("expr"))
                .build();

        AnnotationSpec transAnnotation = AnnotationSpec.builder(Transactional.class)
                .addMember("value", "$S", repo.getAnnotation(StepTransManager.class).value())
                .build();

        MethodSpec.Builder stepMethod = MethodSpec.methodBuilder(methodName)
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addAnnotation(transAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        if (!parameters.isEmpty()) {
            stepMethod.addParameter(TypeName.get(DataTable.class), "dataTable");
        }

        return stepMethod.build();
    }

    private void processDaos(TypeElement typeElement) {
        String packageName = elementUtils.getPackageOf(typeElement).toString();
        TypeMirror superClass = typeElement.asType();
        for (Element field : typeElement.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) continue;
            DeclaredType type = (DeclaredType) field.asType();
            TypeElement fieldClass = (TypeElement) type.asElement();

            if (!fieldClass.getKind().isClass()) continue;

            if (fieldClass.getInterfaces().size() != 1) continue;

            TypeMirror apiInt = fieldClass.getInterfaces().get(0);
            if (!apiInt.toString().startsWith("ru.sber.cb.ekp.base.IRestApiDao")) continue;

            DeclaredType declaredApiInt = (DeclaredType) apiInt;
            if (declaredApiInt.getTypeArguments().size() != 3) continue;

            TypeMirror rqDtoType = declaredApiInt.getTypeArguments().get(0);
            TypeMirror rsDtoType = declaredApiInt.getTypeArguments().get(1);
            TypeMirror rqGenType = declaredApiInt.getTypeArguments().get(2);
            //            TypeMirror entityType = declaredJpaInt.getTypeArguments().get(0);
            //            String repName = field.getSimpleName().toString();
            var servicePackage = elementUtils.getPackageOf(fieldClass);
            var description = servicePackage.getAnnotation(Description.class);
            var descriptionValue = description != null
                    ? String.join(", ", description.value())
                    : null;
            var servicePackageName = servicePackage.getSimpleName().toString();
            if (!isPrimitive(rqDtoType)) {
                var rqDtoTypeElement = (TypeElement) typeUtils.asElement(rqDtoType);
                processDto(packageName, superClass, rqDtoTypeElement);
                rag.compute(servicePackageName, (k, v) -> {
                    if (v == null) {
                        var service = new ServiceRag();
                        service.setDescription(descriptionValue);
                        service.setRq(rqDtoTypeElement.getSimpleName().toString());
                        return service;
                    } else if (v instanceof ServiceRag service) {
                        service.setRq(rqDtoTypeElement.getSimpleName().toString());
                        return service;
                    }
                    throw new RuntimeException(k);
                });
            }
            if (!isPrimitive(rsDtoType)) {
                var rsDtoTypeElement = (TypeElement) typeUtils.asElement(rsDtoType);
                processDto(packageName, superClass, rsDtoTypeElement);
                rag.compute(servicePackageName, (k, v) -> {
                    if (v == null) {
                        var service = new ServiceRag();
                        service.setRs(rsDtoTypeElement.getSimpleName().toString());
                        return service;
                    } else if (v instanceof ServiceRag service) {
                        service.setRs(rsDtoTypeElement.getSimpleName().toString());
                        return service;
                    }
                    throw new RuntimeException(k);
                });
            }
        }
    }

    private void processDto(String packageName, TypeMirror superClass, TypeElement dto) {
        var pathMap = new HashMap<String, List<PathNode>>();
        pathMap.put(dto.getQualifiedName().toString(), fillDtoPathes(dto));
        if (!pathMap.get(dto.getQualifiedName().toString()).isEmpty()) {
            generateDtoSteps(packageName + ".dto", superClass, pathMap);
            generateTrees(pathMap, DtoRag::new);
        }
    }

    private List<PathNode> fillDtoPathes(TypeElement dto) {
        List<PathNode> pathNodes = new ArrayList<>();
        for (Element element : dto.getEnclosedElements().stream()
                .filter(element -> element.getKind().isField()).toList()) {
            VariableElement variableElement = (VariableElement) element;
            TypeMirror fieldType = variableElement.asType();

            String arraySuffix = "";
            if (fieldType.toString().startsWith("java.util.List")) {
                DeclaredType dtype = (DeclaredType) fieldType;
                fieldType = dtype.getTypeArguments().get(0);
                arraySuffix = "[l]";
            }

            if (isPrimitive(fieldType) || !arraySuffix.isEmpty()) {
                pathNodes.add(new PathNode(variableElement, fieldType, variableElement + arraySuffix));
            }

            if (!isPrimitive(fieldType)) {
                pathNodes.add(new PathNode(variableElement, variableElement.asType(), variableElement.toString()));
                TypeElement retType = (TypeElement) typeUtils.asElement(fieldType);
                for (var pathNode : fillDtoPathes(retType)) {
                    pathNodes.add(new PathNode(pathNode.getTo(), pathNode.getToType(), variableElement + arraySuffix + "." + pathNode.getPath()));
                }
            }
        }
        return pathNodes;
    }

    private void generateDtoSteps(String packageName, TypeMirror superClass, Map<String, List<PathNode>> pathMap) {
        for (var key : pathMap.keySet()) {
            TypeElement keyElement = elementUtils.getTypeElement(key);
            String generatedClassName = keyElement.getSimpleName() + "Steps";
            TypeSpec.Builder stepsBuilder = TypeSpec.classBuilder(generatedClassName)
                    .addAnnotation(generated)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(superClass);

            for (var pathNode : pathMap.get(key)) {
                stepsBuilder.addMethod(generateSetContext(keyElement, pathNode));
                stepsBuilder.addMethod(generateCheckWithContext(keyElement, pathNode));
                stepsBuilder.addMethod(generateWriteFromContext(keyElement, pathNode));
            }

            // Создаем пакет
            JavaFile javaFile = JavaFile.builder(packageName, stepsBuilder.build())
                    .indent("    ")
                    .skipJavaLangImports(true)
                    .build();

            // Генерируем файл
            FileObject fileObject = null;
            try {
                fileObject = processingEnv.getFiler().createSourceFile(javaFile.packageName + "." + generatedClassName);
                try (Writer writer = fileObject.openWriter()) {
                    javaFile.writeTo(writer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private MethodSpec generateCheckWithContext(TypeElement keyElement, PathNode pathNode) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock
                .addStatement("$T origin = $T.getContext($S, $T.class).orElse(null)",
                        keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType())
                .beginControlFlow("try")
                .add("boolean check = ");

        getterMethodChecker(codeBlock, "origin", List.of(pathNode.getPath().split("\\.")), 0);
        codeBlock
                .addStatement("org.junit.Assert.assertTrue(check)")
                .endControlFlow()
                .beginControlFlow("catch($T exception)", NullPointerException.class)
                .addStatement("org.junit.Assert.fail(exception.getMessage())")
                .endControlFlow();

        AnnotationSpec thenAnnotation = AnnotationSpec.builder(Then.class)
                .addMember("value", "\"Проверяю что $N {comparator} {context}\"", keyElement.getSimpleName() + "." +
                                                                                  pathNode.getPath()
                                                                                          .replace("[a]", "[{arr_idx}]")
                                                                                          .replace("[l]", "[{arr_idx}]"))
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("checkWithContext" + keyElement.getSimpleName() + "_" +
                                                              pathNode.getPath()
                                                                      .replace(".", "_")
                                                                      .replace("[a]", "")
                                                                      .replace("[l]", ""))
                .addAnnotation(generated)
                .addAnnotation(thenAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        Matcher matcher = arrayQuery.matcher(pathNode.getPath());
        while (matcher.find()) builder.addParameter(TypeName.get(String.class), matcher.group("field") + "Idx");

        return builder
                .addParameter(TypeName.get(Comparator.ComparatorEnum.class), "equalsType")
                .addParameter(ParameterizedTypeName.get(Optional.class, Object.class), "expected")
                .build();
    }

    private MethodSpec generateCheckWithContext(TypeElement keyElement) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("""
                        $T target = $T.getContext($S, $T.class).orElse(null);
                        try {
                            $T.compare(equalsType, expected.orElse(null), target);
                        } catch($T exception) {
                            org.junit.Assert.fail(exception.getMessage());
                        }
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType(),
                Comparator.class,
                NullPointerException.class
        );

        AnnotationSpec thenAnnotation = AnnotationSpec.builder(Then.class)
                .addMember("value", "\"Проверяю что $N {comparator} {context}\"", keyElement.getSimpleName())
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("checkWithContext" + keyElement.getSimpleName())
                .addAnnotation(generated)
                .addAnnotation(thenAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        return builder
                .addParameter(TypeName.get(Comparator.ComparatorEnum.class), "equalsType")
                .addParameter(ParameterizedTypeName.get(Optional.class, Object.class), "expected")
                .build();
    }

    private MethodSpec generateWriteFromContext(TypeElement keyElement, PathNode pathNode) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        var list = Arrays.stream(pathNode.getPath().split("\\.")).toList();
        codeBlock.add("""
                        $T origin = $T.getContext($S, $T.class).orElseThrow();
                        try {
                        """,
                keyElement.asType(), Context.class, keyElement.getSimpleName(), keyElement.asType()
        );

        if (list.isEmpty()) {
            codeBlock.add("""
                                $T newValue = object.isPresent() ? ($T) object.orElseThrow() : null;
                                $T.setContext($S, newValue);
                                io.qameta.allure.Allure.addAttachment($S, newValue != null ? newValue.toString() : "null");
                            """,
                    keyElement.asType(), keyElement.asType(),
                    Context.class, keyElement.getSimpleName(),
                    keyElement.getSimpleName()
            );
        } else {
            // Проверяем нужно ли преобразовывать в String
            if (pathNode.getToType().toString().equals("java.lang.String")) {
                codeBlock.add("""
                                    $T newValue = object.isPresent() ? $T.cast(object.get(), $T.class) : null;
                                    origin$N$N.$N;
                                    io.qameta.allure.Allure.addAttachment($S, newValue != null ? newValue.toString() : "null");
                                """,
                        pathNode.getToType(), Caster.class, pathNode.getToType(),
                        list.size() > 1 ? "." : "",
                        list.stream().limit(list.size() - 1).map(this::getterMethod).collect(Collectors.joining(".")),
                        setterMethod(list.get(list.size() - 1), "newValue"),
                        pathNode.getPath()
                );
            } else {
                codeBlock.add("""

                                    $T newValue = object.isPresent() ? ($T) object.orElseThrow() : null;
                                    origin$N$N.$N;
                                    io.qameta.allure.Allure.addAttachment($S, newValue != null ? newValue.toString() : "null");
                                """,
                        pathNode.getToType(), pathNode.getToType(),
                        list.size() > 1 ? "." : "",
                        list.stream().limit(list.size() - 1).map(this::getterMethod).collect(Collectors.joining(".")),
                        setterMethod(list.get(list.size() - 1), "newValue"),
                        pathNode.getPath()
                );
            }
        }
        codeBlock.add("""
                        } catch($T exception) {
                            org.junit.Assert.fail(exception.getMessage());
                        }
                        """,
                Exception.class
        );

        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Записываю {context} в $N\"", keyElement.getSimpleName() + "." +
                                                                    pathNode.getPath()
                                                                            .replace("[a]", "[{int}]")
                                                                            .replace("[l]", "[{int}]"))
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("writeFromContext" + keyElement.getSimpleName() + "_" +
                                                              pathNode.getPath()
                                                                      .replace(".", "_")
                                                                      .replace("[a]", "")
                                                                      .replace("[l]", ""))
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        builder.addParameter(ParameterizedTypeName.get(Optional.class, Object.class), "object");

        Matcher matcher = arrayQuery.matcher(pathNode.getPath());
        while (matcher.find()) builder.addParameter(TypeName.get(Integer.class), matcher.group("field") + "Idx");

        return builder.build();
    }

    private MethodSpec generateWriteFromContext(TypeElement keyElement) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.add("""
                        try {
                            $T newValue = object.isPresent() ? ($T) object.orElseThrow() : null;
                            $T.setContext($S, newValue);
                            io.qameta.allure.Allure.addAttachment($S, newValue != null ? newValue.toString() : "null");
                        } catch($T exception) {
                            org.junit.Assert.fail(exception.getMessage());
                        }
                        """,
                keyElement.asType(), keyElement.asType(),
                Context.class, keyElement.getSimpleName(),
                keyElement.getSimpleName(),
                Exception.class
        );


        AnnotationSpec addAnnotation = AnnotationSpec.builder(And.class)
                .addMember("value", "\"Записываю {context} в $N\"", keyElement.getSimpleName())
                .build();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("writeFromContext" + keyElement.getSimpleName())
                .addAnnotation(generated)
                .addAnnotation(addAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(void.class))
                .addCode(codeBlock.build());

        builder.addParameter(ParameterizedTypeName.get(Optional.class, Object.class), "object");

        return builder.build();
    }

    private boolean isPrimitive(TypeMirror typeMirror) {
        if (typeMirror.getKind().isPrimitive()) return true;

        for (TypeMirror primitive : primitives) {
            if (typeUtils.isSameType(typeMirror, primitive)) return true;
        }

        return false;
    }

    private TypeMirror boxed(TypeMirror typeMirror) {
        if (typeMirror.getKind().isPrimitive()) {
            PrimitiveType primitiveType = (PrimitiveType) typeMirror;
            return typeUtils.boxedClass(primitiveType).asType();
        }

        return typeMirror;
    }

}

