package com.conectebem.backend.controller;

import com.conectebem.backend.dto.OportunidadeResponseDTO;
import com.conectebem.backend.service.OportunidadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o contrato HTTP da listagem pública de oportunidades: paginação,
 * cabeçalho de total e escolha do filtro enviado pelo cliente.
 */
class OportunidadeControllerTest {

    private final OportunidadeService oportunidadeService = mock(OportunidadeService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void prepararMockMvc() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OportunidadeController(oportunidadeService))
                .build();
    }

    @Test
    void listaLimitaPaginacaoEInformaTotalNoCabecalho() throws Exception {
        when(oportunidadeService.listar(any(Pageable.class)))
                .thenAnswer(invocacao -> pagina(invocacao.getArgument(0), List.of(), 250));

        mockMvc.perform(get("/oportunidades")
                        .param("page", "-3")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "250"))
                .andExpect(jsonPath("$").isEmpty());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(oportunidadeService).listar(pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void filtraPorCidadeQuandoElaForOUnicoFiltroInformado() throws Exception {
        when(oportunidadeService.filtrarPorCidade(eq("Porto Alegre"), any(Pageable.class)))
                .thenAnswer(invocacao -> pagina(invocacao.getArgument(1), List.of(oportunidade()), 1));

        mockMvc.perform(get("/oportunidades").param("cidade", "Porto Alegre"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].titulo").value("Mutirão de limpeza"));

        verify(oportunidadeService).filtrarPorCidade(eq("Porto Alegre"), any(Pageable.class));
        verify(oportunidadeService, never()).listar(any(Pageable.class));
    }

    @Test
    void combinacaoDeFiltrosMantemListagemSemFiltroEnquantoNaoHaSuporteCombinado() throws Exception {
        when(oportunidadeService.listar(any(Pageable.class)))
                .thenAnswer(invocacao -> pagina(invocacao.getArgument(0), List.of(oportunidade()), 1));

        mockMvc.perform(get("/oportunidades")
                        .param("cidade", "Porto Alegre")
                        .param("status", "ABERTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(oportunidadeService).listar(any(Pageable.class));
        verify(oportunidadeService, never()).filtrarPorCidade(any(), any(Pageable.class));
        verify(oportunidadeService, never()).filtrarPorStatus(any(), any(Pageable.class));
    }

    private Page<OportunidadeResponseDTO> pagina(
            Pageable pageable,
            List<OportunidadeResponseDTO> conteudo,
            long total
    ) {
        return new PageImpl<>(conteudo, pageable, total);
    }

    private OportunidadeResponseDTO oportunidade() {
        return new OportunidadeResponseDTO(
                1, 2, "ONG Exemplo", 3, "Meio ambiente", "Mutirão de limpeza",
                "Limpeza do parque", LocalDate.of(2026, 12, 1), LocalTime.of(9, 0),
                "Porto Alegre", "RS", 10, "ABERTA", LocalDateTime.of(2026, 11, 1, 10, 0),
                2, 8
        );
    }
}
