import { hojeNoBrasil } from './idade.js';

export function motivoIndisponibilidade(oportunidade, hoje = hojeNoBrasil()) {
  if (oportunidade.status !== 'ABERTA') return 'Inscrições encerradas';
  if (oportunidade.data && oportunidade.data < hoje) return 'Oportunidade já realizada';
  if ((oportunidade.vagasDisponiveis ?? oportunidade.vagas) <= 0) return 'Vagas esgotadas';
  return '';
}

export function candidaturaCancelada(candidatura) {
  return candidatura.statusInscricao === 'CANCELADA' || candidatura.statusOportunidade === 'CANCELADA';
}

export function pertenceAoFiltro(candidatura, filtro) {
  if (filtro === 'todas') return true;
  if (filtro === 'canceladas') return candidaturaCancelada(candidatura);
  if (candidaturaCancelada(candidatura)) return false;
  if (filtro === 'finalizadas') return candidatura.statusOportunidade === 'ENCERRADA';
  if (filtro === 'ativas') return candidatura.statusOportunidade === 'ABERTA';
  return true;
}
