package net.kilink.verse;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Utils {

    private Utils() {}

    public static MethodSpec equalsMethod(TypeSpec.Builder builder) {
        Objects.requireNonNull(builder, "builder == null");
        return equalsMethod(builder.build());
    }

    public static MethodSpec equalsMethod(TypeSpec type) {
        Objects.requireNonNull(type, "type == null");
        MethodSpec.Builder method = MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(Object.class, "obj");

        method.addStatement("if (this == obj) return true");
        method.beginControlFlow("if (obj instanceof $N)", type);

        method.addStatement("$N that = ($N) obj", type, type);

        List<CodeBlock> comparisons = new ArrayList<>();

        for (FieldSpec field : type.fieldSpecs) {
            final CodeBlock comparison;
            if (field.type.isPrimitive()) {
                comparison = CodeBlock.builder()
                        .add("this.$N == that.$N", field, field)
                        .build();
            } else {
                comparison = CodeBlock.builder()
                        .add("$T.equals(this.$N, that.$N)", Objects.class, field, field)
                        .build();
            }
            comparisons.add(comparison);
        }

        method.addStatement("return $L", CodeBlock.join(comparisons, "\n&& "));
        method.endControlFlow();
        method.addStatement("return false");

        return method.build();
    }

    public static MethodSpec hashCodeMethod(TypeSpec.Builder builder) {
        Objects.requireNonNull(builder, "builder == null");
        return hashCodeMethod(builder.build());
    }

    public static MethodSpec hashCodeMethod(TypeSpec type) {
        Objects.requireNonNull(type, "type == null");
        MethodSpec.Builder method = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class);

        List<CodeBlock> fields = type.fieldSpecs.stream()
                .map(field -> CodeBlock.of("this.$N", field))
                .collect(Collectors.toList());

        method.addStatement("return $T.hash($L)", Objects.class, CodeBlock.join(fields, ", "));

        return method.build();
    }

    public static MethodSpec toStringMethod(TypeSpec.Builder builder) {
        Objects.requireNonNull(builder, "builder == null");
        return toStringMethod(builder.build());
    }

    public static MethodSpec toStringMethod(TypeSpec type) {
        Objects.requireNonNull(type, "type == null");
        MethodSpec.Builder method = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);

        if (type.fieldSpecs.isEmpty()) {
            method.addStatement("return %N{}", type);
            return method.build();
        }

        method.addStatement("$T sb = new $T($S)",
                StringBuilder.class,
                StringBuilder.class,
                CodeBlock.of("$N{", type));

        String nextSeparator = "";
        for (FieldSpec field : type.fieldSpecs) {
            if (!nextSeparator.isEmpty()) {
                method.addStatement("sb.append($S)", ", ");
            }
            nextSeparator = ", ";
            method.addStatement("sb.append($S)", CodeBlock.of("$N=", field));
            if (field.type instanceof ArrayTypeName) {
                if (((ArrayTypeName) field.type).componentType.isPrimitive()) {
                    method.addStatement("sb.append($T.toString(this.$N))", Arrays.class, field);
                } else {
                    method.addStatement("sb.append($T.deepToString(this.$N))", Arrays.class, field);
                }
            } else {
                method.addStatement("sb.append(this.$N)", field);
            }
        }

        method.addStatement("return sb.append('}').toString()");

        return method.build();
    }
}
