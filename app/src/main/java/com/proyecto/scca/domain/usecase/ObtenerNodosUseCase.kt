package com.proyecto.scca.domain.usecase

import com.proyecto.scca.domain.model.Nodo
import com.proyecto.scca.domain.model.Rol
import com.proyecto.scca.domain.repository.NodoRepository
import javax.inject.Inject

class ObtenerNodosUseCase
    @Inject
    constructor(
        private val nodoRepository: NodoRepository,
    ) {
        /**
         * Si el rol es CLIENTE → mis-nodos.
         * Cualquier otro rol → listado paginado de nodos activos.
         */
        suspend operator fun invoke(rol: Rol): Result<List<Nodo>> {
            return if (rol == Rol.CLIENTE) {
                nodoRepository.listarMisNodos()
            } else {
                nodoRepository.listarNodosPaginado(
                    activo = true,
                    pagina = 0,
                    tamanio = 100,
                ).map { it.contenido }
            }
        }
    }
