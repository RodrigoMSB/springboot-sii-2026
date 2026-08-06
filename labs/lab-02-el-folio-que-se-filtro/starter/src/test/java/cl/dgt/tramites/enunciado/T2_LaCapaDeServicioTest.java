package cl.dgt.tramites.enunciado;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * TODO_2 · La lógica vive en una capa de servicio, no en el controlador.
 *
 * <p>No se prueba "que exista una clase llamada FichaService" (eso lo falsea cualquiera).
 * Se prueban dos propiedades estructurales: el controlador de la ficha depende de una
 * abstracción {@code FichaService}, y no habla directamente con el repositorio saltándose la
 * capa. Un controlador que llama al repositorio es un controlador que, tarde o temprano,
 * mete lógica de negocio entre medio.
 */
class T2_LaCapaDeServicioTest {

    private static final JavaClasses PRODUCCION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("cl.dgt.tramites");

    @Test
    @DisplayName("FichaController depende del contrato FichaService")
    void elControladorUsaLaCapaDeServicio() {
        classes()
                .that().haveSimpleName("FichaController")
                .should().dependOnClassesThat().haveSimpleName("FichaService")
                .because("La lógica de armar la ficha vive en un servicio, no en el controlador.")
                .check(PRODUCCION);
    }

    @Test
    @DisplayName("El controlador de la ficha NO habla directo con el repositorio")
    void elControladorNoSaltaLaCapa() {
        noClasses()
                .that().haveSimpleName("FichaController")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.repository..")
                .because("Saltarse la capa de servicio es cómo la lógica de negocio termina en la web.")
                .check(PRODUCCION);
    }
}
