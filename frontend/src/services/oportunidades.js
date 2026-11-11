import { hojeNoBrasil } from './idade.js';

export function motivoIndisponibilidade(oportunidade, hoje = hojeNoBrasil()) {
  if (oportunidade.status !== 'ABERTA') return 'Inscrições encerradas';
  if (oportunidade.data && oportunidade.data < hoje) return 'Oportunidade já realizada';
  if ((oportunidade.vagasDisponiveis ?? oportunidade.vagas) <= 0) return 'Vagas esgotadas';
  return '';
}
