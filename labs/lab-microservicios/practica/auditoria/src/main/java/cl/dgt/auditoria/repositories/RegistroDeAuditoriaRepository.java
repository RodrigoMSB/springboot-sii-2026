package cl.dgt.auditoria.repositories;

import cl.dgt.auditoria.entities.RegistroDeAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroDeAuditoriaRepository extends JpaRepository<RegistroDeAuditoria, Long> {
}
