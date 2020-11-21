package net.kilink.verse

import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec

import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun testEqualsMethod() {
        val clazz = TypeSpec.classBuilder("Foo")
                .addField(TypeName.LONG, "foo")
                .addField(TypeName.BOOLEAN, "bar")
                .addField(ClassName.get(String::class.java), "baz")
        val equalsMethod = Utils.equalsMethod(clazz)

        assertThat(equalsMethod.toString()).isEqualTo(
                """ 
                |@java.lang.Override
                |public boolean equals(java.lang.Object obj) {
                |  if (this == obj) return true;
                |  if (obj instanceof Foo) {
                |    Foo that = (Foo) obj;
                |    return this.foo == that.foo
                |        && this.bar == that.bar
                |        && java.util.Objects.equals(this.baz, that.baz);
                |  }
                |  return false;
                |}
                |""".trimMargin())

        clazz.addMethod(equalsMethod)

        val javaFile = JavaFile.builder("", clazz.build()).build()
        val result = javac().compile(javaFile.toJavaFileObject())
        assertThat(result).succeededWithoutWarnings()
    }

    @Test
    fun testHashCodeMethod() {
        val clazz = TypeSpec.classBuilder("Foo")
                .addField(TypeName.LONG, "foo")
                .addField(TypeName.BOOLEAN, "bar")
                .addField(ClassName.get(String::class.java), "baz")
        val hashCodeMethod = Utils.hashCodeMethod(clazz)

        assertThat(hashCodeMethod.toString()).isEqualTo(
                """
                |@java.lang.Override
                |public int hashCode() {
                |  return java.util.Objects.hash(this.foo, this.bar, this.baz);
                |}
                |""".trimMargin())

        clazz.addMethod(hashCodeMethod)

        val javaFile = JavaFile.builder("", clazz.build()).build()
        val result = javac().compile(javaFile.toJavaFileObject())
        assertThat(result).succeededWithoutWarnings()
    }

    @Test
    fun testToStringMethod() {
        val clazz = TypeSpec.classBuilder("Foo")
                .addField(TypeName.LONG, "foo")
                .addField(TypeName.BOOLEAN, "bar")
                .addField(ArrayTypeName.of(TypeName.BYTE), "data")
                .addField(ArrayTypeName.of(String::class.java), "names")
                .addField(ClassName.get(String::class.java), "baz")

        val toString = Utils.toStringMethod(clazz)

        assertThat(toString.toString()).isEqualTo(
                """
                |@java.lang.Override
                |public java.lang.String toString() {
                |  java.lang.StringBuilder sb = new java.lang.StringBuilder("Foo{");
                |  sb.append("foo=");
                |  sb.append(this.foo);
                |  sb.append(", ");
                |  sb.append("bar=");
                |  sb.append(this.bar);
                |  sb.append(", ");
                |  sb.append("data=");
                |  sb.append(java.util.Arrays.toString(this.data));
                |  sb.append(", ");
                |  sb.append("names=");
                |  sb.append(java.util.Arrays.deepToString(this.names));
                |  sb.append(", ");
                |  sb.append("baz=");
                |  sb.append(this.baz);
                |  return sb.append('}').toString();
                |}
                |""".trimMargin())

        clazz.addMethod(toString)

        val javaFile = JavaFile.builder("", clazz.build()).build()
        val result = javac().compile(javaFile.toJavaFileObject())
        assertThat(result).succeededWithoutWarnings()
    }
}