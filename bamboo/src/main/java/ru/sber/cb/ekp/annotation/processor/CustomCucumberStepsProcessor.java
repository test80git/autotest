package ru.sber.cb.ekp.annotation.processor;

import com.google.auto.service.AutoService;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import ru.sber.cb.ekp.annotation.Description;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.cucumber.java.en.And",
        "io.cucumber.java.en.Given",
        "io.cucumber.java.en.Then",
        "io.cucumber.java.en.When",
        "io.cucumber.java.en.But"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class CustomCucumberStepsProcessor  extends AbstractProcessor {
    private static final Pattern stepPattern = Pattern.compile("(?:Поиск|Проверяю что|Сохраняю|(?:Отправляю|Генерирую).+запрос.+) ([a-zA-Z]+)");
    private final Map<String, String> steps = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) writeToFile();
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getAnnotation(Generated.class) != null) continue;

                var mirrors = element.getAnnotationMirrors();

                for (var mirror : mirrors) {
                    var type = mirror.getAnnotationType();

                    if (!type.toString().equals(annotation.getQualifiedName().toString())) {
                        continue;
                    }

                    for (var entry : mirror.getElementValues().entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("value")) {
                            var value = entry.getValue().getValue();
                            var description = element.getAnnotation(Description.class);
                            var descriptionValue = description != null
                                    ? String.join(", ", description.value())
                                    : null;
                            if (value instanceof String stepExpression) {
                                var sb = new StringBuilder()
                                        .append(annotation.getSimpleName())
                                        .append(" ")
                                        .append(stepExpression);
                                var matcher = stepPattern.matcher(stepExpression);
                                var key = "Undefined";
                                if (matcher.find())
                                    key = matcher.group(1);
                                if (descriptionValue != null)
                                    sb.append(" #").append(descriptionValue);

                                steps.compute(key, (k, v) -> (v == null) ? sb.toString() : v.concat("\n").concat(sb.toString()));
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void writeToFile() {
        try {
            var file = processingEnv.getFiler().createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    "",
                    "custom.json"
            );
            try (var writer = file.openWriter()) {
                writer.write(mapper.writeValueAsString(steps));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
