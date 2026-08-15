package cl.dgt.tramites.infrastructure.legacy;

import cl.dgt.tramites.application.ObservacionInternaVista;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee las observaciones internas de un contribuyente. Lo escribió un practicante hace dos años,
 * funciona, y nadie lo ha vuelto a mirar.
 *
 * <p><strong>Funciona.</strong> Esa es la parte incómoda: si le pides las observaciones de
 * Valentina, te las da. En el camino feliz no hay nada que delate lo que está mal.
 *
 * <p>Tiene cuatro defectos, y conviene contarlos porque el laboratorio los va a matar a los
 * cuatro de un golpe. Están marcados abajo como PECADO 1 a 4.
 */
@Repository
@Profile("dev")
public class ReporteInternoLegacyDao {

    private final DataSource dataSource;

    public ReporteInternoLegacyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ObservacionInternaVista> observacionesDe(String rut) {
        List<ObservacionInternaVista> resultado = new ArrayList<>();

        // PECADO 1 · El rut llega del request y se PEGA a la consulta. Para el motor, lo que
        //            llega deja de ser un dato y pasa a ser parte de la instrucción.
        String sql = "SELECT c.rut, o.texto, o.autor, o.creada_en "
                   + "FROM observacion_interna o "
                   + "JOIN contribuyente c ON c.id = o.contribuyente_id "
                   + "WHERE c.rut = '" + rut + "'";

        try {
            Connection cx = dataSource.getConnection();
            Statement st = cx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            // PECADO 2 · El mapeo a mano, columna por columna y por número de orden. El día que
            //            alguien reordene el SELECT, esto compila igual y devuelve cualquier cosa.
            while (rs.next()) {
                resultado.add(new ObservacionInternaVista(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getTimestamp(4).toLocalDateTime()));
            }

            // PECADO 3 · Se cierran aquí, al final del camino feliz. Si algo de arriba lanza,
            //            estas líneas no se ejecutan y la conexión se queda tomada para siempre.
            //            Sin `finally`, sin try-with-resources: una fuga por cada error.
            rs.close();
            st.close();
            cx.close();

        } catch (SQLException e) {
            // PECADO 4 · El error no existe. La base puede estar caída, la consulta puede ser
            //            inválida, y quien llama recibe una lista vacía indistinguible de
            //            «este contribuyente no tiene observaciones».
        }

        return resultado;
    }
}
